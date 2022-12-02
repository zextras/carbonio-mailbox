// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.fb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxListener;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.calendar.IcalXmlStrMap;
import com.zimbra.cs.service.mail.ToXML;

public abstract class FreeBusyProvider {

    public static class Listener extends MailboxListener {

        @Override
        public void notify(ChangeNotification notification) {
            FreeBusyProvider.mailboxChanged(notification.mailboxAccount.getId(), notification.mods.changedTypes);
        }

        private static final Set<Type> TYPES = EnumSet.of(MailItem.Type.APPOINTMENT);

        @Override
        public Set<Type> registerForItemTypes() {
            return TYPES;
        }
    }

    public static class Request {

        public Request(Account req, String em, long s, long e, int f) {
            this(req, em, s, e, f, -1);
        }

        public Request(Account req, String em, long s, long e, int f, int hops) {
            requestor = req; email = em;
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(s);
            cal.set(Calendar.SECOND, 0);
            start = cal.getTimeInMillis();
            cal.setTimeInMillis(e);
            cal.set(Calendar.SECOND, 0);
            end = cal.getTimeInMillis();
            folder = f;
            hopcount = hops;
        }
        Account requestor;
        String email;
        long start;
        long end;
        int folder;
        Object data;
        int hopcount;

        public Account getRequestor() {
            return requestor;
        }

        public String getEmail() {
            return email;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public int getFolder() {
            return folder;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object obj) {
            data = obj;
        }

        public int getHopcount() {
            return hopcount;
        }

        public void incrementHopcount() {
            if (hopcount < 0 )
                hopcount = 0;
            else
                hopcount += 1;
        }

        public static long offsetInterval(long time, int intervalInMins) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(time);
            int min = cal.get(Calendar.MINUTE);
            int off = min % intervalInMins;
            cal.set(Calendar.MINUTE, min - off);
            return cal.getTimeInMillis();
        }
    }
    @SuppressWarnings("serial")
    public static class FreeBusyUserNotFoundException extends Exception {
        public FreeBusyUserNotFoundException() {
            // empty constructor
        }
    }


    public abstract FreeBusyProvider getInstance();
    public abstract String getName();

    // free/busy lookup from 3rd party system
    public abstract void addFreeBusyRequest(Request req) throws FreeBusyUserNotFoundException;
    public abstract List<FreeBusy> getResults();

    // propagation of Zimbra users free/busy to 3rd party system
    public abstract boolean registerForMailboxChanges();
    public abstract boolean registerForMailboxChanges(String accountId);
    public abstract Set<MailItem.Type> registerForItemTypes();
    public abstract boolean handleMailboxChange(String accountId);
    public abstract long cachedFreeBusyStartTime();
    public abstract long cachedFreeBusyStartTime(String accountId);
    public abstract long cachedFreeBusyEndTime();
    public abstract long cachedFreeBusyEndTime(String accountId);
    public abstract String foreignPrincipalPrefix();

    public static void register(FreeBusyProvider p) {
        synchronized (sPROVIDERS) {
            sPROVIDERS.add(p);
        }
    }

    private static FreeBusySyncQueue startConsumerThread(FreeBusyProvider p) {
        String name = p.getName();
        FreeBusySyncQueue queue = sPUSHQUEUES.get(name);
        if (queue != null) {
            ZimbraLog.fb.warn("free/busy provider "+name+" has been already registered.");
        }
        queue = new FreeBusySyncQueue(p);
        sPUSHQUEUES.put(name, queue);
        new Thread(queue).start();
        return queue;
    }

    public static void mailboxChanged(String accountId) {
        mailboxChanged(accountId, EnumSet.of(MailItem.Type.APPOINTMENT));
    }

    public static void mailboxChanged(String accountId, Set<MailItem.Type> changedType) {
        for (FreeBusyProvider prov : sPROVIDERS)
            if (prov.registerForMailboxChanges(accountId) && !Collections.disjoint(changedType, prov.registerForItemTypes())) {
                FreeBusySyncQueue queue = sPUSHQUEUES.get(prov.getName());
                if (queue == null)
                    queue = startConsumerThread(prov);
                synchronized (queue) {
                    if (queue.contains(accountId))
                        continue;
                    queue.addLast(accountId);
                    try {
                        queue.writeToDisk();
                    } catch (IOException e) {
                        ZimbraLog.fb.error("can't write to the queue "+queue.getFilename());
                    }
                    queue.notify();
                }
            }
    }

    public void addResults(Element response) {
        for (FreeBusy fb : getResults())
            ToXML.encodeFreeBusy(response, fb);
    }

    public static List<FreeBusy> getRemoteFreeBusy(Account requestor, List<String> remoteIds, long start, long end, int folder, int hopcount) {
        Set<FreeBusyProvider> providers = getProviders();
        ArrayList<FreeBusy> ret = new ArrayList<FreeBusy>();
        for (String emailAddr : remoteIds) {
            Request req = new Request(requestor, emailAddr, start, end, folder, hopcount);
            boolean succeed = false;
            for (FreeBusyProvider prov : providers) {
                try {
                    prov.addFreeBusyRequest(req);
                    succeed = true;
                } catch (FreeBusyUserNotFoundException e) {
                }
            }
            if (!succeed) {
                ZimbraLog.fb.error("can't find free/busy provider for user "+emailAddr);
                ret.add(FreeBusy.nodataFreeBusy(emailAddr, start, end));
            }
        }

        // there could be duplicate results from different providers.
        // construct the map of results.
        Map<String, ArrayList<FreeBusy>> freebusyMap = new HashMap<String, ArrayList<FreeBusy>>();
        for (FreeBusyProvider prov : providers) {
            for (FreeBusy fb : prov.getResults()) {
                ArrayList<FreeBusy> freebusyList = freebusyMap.get(fb.getName());
                if (freebusyList == null) {
                    freebusyList = new ArrayList<FreeBusy>();
                    freebusyMap.put(fb.getName(), freebusyList);
                }
                freebusyList.add(fb);
            }
        }
        // filter the duplicate and take one freebusy result for each user.
        for (Map.Entry<String, ArrayList<FreeBusy>> entry : freebusyMap.entrySet()) {
            ArrayList<FreeBusy> freebusyList = entry.getValue();
            FreeBusy freebusy = null;
            for (FreeBusy fb : freebusyList) {
                if (freebusy == null) {
                    freebusy = fb;
                } else {
                    // check if fb is better response than freebusy.
                    if (!fb.getBusiest().equals(IcalXmlStrMap.FBTYPE_FREE))
                        freebusy = fb;
                }
            }
            ret.add(freebusy);
        }
        return ret;
    }

    public static void getRemoteFreeBusy(Account requestor, Element response, List<String> remoteIds, long start, long end, int folder, int hopcount) {
        for (FreeBusy fb : getRemoteFreeBusy(requestor, remoteIds, start, end, folder, hopcount)) {
            ToXML.encodeFreeBusy(response, fb);
        }
    }

    protected FreeBusy getFreeBusy(String accountId, int folderId) throws ServiceException {
        Account account = Provisioning.getInstance().getAccountById(accountId);
        if (account == null || !Provisioning.onLocalServer(account))
            return null;
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(accountId);
        if (mbox == null)
            return null;
        return mbox.getFreeBusy(null, cachedFreeBusyStartTime(accountId), cachedFreeBusyEndTime(accountId), folderId);
    }

    protected String getEmailAddress(String accountId) {
        Account acct = null;
        try {
            acct = Provisioning.getInstance().get(Key.AccountBy.id, accountId);
        } catch (ServiceException e) {
        }
        if (acct == null)
            return null;
        return acct.getName();
    }

    protected List<FreeBusy> getEmptyList(ArrayList<Request> req) {
        ArrayList<FreeBusy> ret = new ArrayList<FreeBusy>();
        for (Request r : req)
            ret.add(FreeBusy.nodataFreeBusy(r.email, r.start, r.end));
        return ret;
    }

    public FreeBusySyncQueue getSyncQueue() {
        return sPUSHQUEUES.get(getName());
    }
    public static FreeBusyProvider getProvider(String name) {
        for (FreeBusyProvider p : sPROVIDERS)
            if (p.getName().equals(name))
                return p;
        return null;
    }
    public static Set<FreeBusyProvider> getProviders() {
        HashSet<FreeBusyProvider> ret = new HashSet<FreeBusyProvider>();
        for (FreeBusyProvider p : sPROVIDERS)
            ret.add(p.getInstance());
        return ret;
    }
    private static HashSet<FreeBusyProvider> sPROVIDERS;
    private static HashMap<String,FreeBusySyncQueue> sPUSHQUEUES;

    static {
        sPROVIDERS = new HashSet<FreeBusyProvider>();
        sPUSHQUEUES = new HashMap<String,FreeBusySyncQueue>();
        new ExchangeFreeBusyProvider();  // load the class
        new ExchangeEWSFreeBusyProvider();
    }

    public String getQueueFilename() {
        return LC.freebusy_queue_directory.value() + "queue-" + getName();
    }

    @SuppressWarnings("serial")
    public static class FreeBusySyncQueue extends LinkedList<String> implements Runnable {

        FreeBusySyncQueue(FreeBusyProvider prov) {
            mProvider = prov;
            mLastFailed = 0;
            mShutdown = false;
            mFilename = prov.getQueueFilename();
            File f = new File(mFilename);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            try {
                readFromDisk();
            } catch (IOException e) {
                ZimbraLog.fb.error("error reading from the queue", e);
            }
        }

        @Override
        public void run() {
            Thread.currentThread().setName(mProvider.getName() + " Free/Busy Sync Queue");
            while (!mShutdown) {
                try {
                    String acctId = null;
                    synchronized (this) {
                        if (size() > 0) {
                            // wait for some interval when we detect a failure
                            // such that we don't spin loop and keep hammering a down server.
                            long now = System.currentTimeMillis();
                            long retryInterval = DEFAULT_RETRY_INTERVAL;
                            try {
                                retryInterval = Provisioning.getInstance().getLocalServer().getFreebusyPropagationRetryInterval();
                            } catch (Exception e) {
                            }
                            if (now < mLastFailed + retryInterval) {
                                wait(retryInterval);
                                continue;
                            }
                            acctId = getFirst();
                        } else
                            wait();

                    }
                    if (acctId == null)
                        continue;

                    boolean success = mProvider.handleMailboxChange(acctId);

                    synchronized (this) {
                        removeFirst();
                    }
                    if (!success) {
                        synchronized (this) {
                            addLast(acctId);
                        }
                        mLastFailed = System.currentTimeMillis();
                    }
                    writeToDisk();

                } catch (Exception e) {
                    mLastFailed = System.currentTimeMillis();
                    ZimbraLog.fb.error("error while syncing freebusy for "+mProvider.getName(), e);
                }
            }
        }
        public void shutdown() {
            mShutdown = true;
        }

        private boolean mShutdown;
        private long mLastFailed;
        private static final int DEFAULT_RETRY_INTERVAL = 60 * 1000; // 1m
        private static final int MAX_FILE_SIZE = 1024000;  // for sanity check
        private String mFilename;
        private FreeBusyProvider mProvider;

        public synchronized void writeToDisk() throws IOException {
            StringBuilder buf = new StringBuilder(Integer.toString(size()+1));
            for (String id : this)
                buf.append("\n").append(id);
            if (buf.length() > MAX_FILE_SIZE) {
                ZimbraLog.fb.error("The free/busy replication queue is too large. #elem="+size());
                return;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(mFilename);
                out.write(buf.toString().getBytes());
                out.getFD().sync();
            } finally {
                if (out != null)
                    out.close();
            }
        }

        public synchronized void readFromDisk() throws IOException {
            File f = new File(mFilename);
            if (!f.exists())
                f.createNewFile();
            long len = f.length();
            if (len > MAX_FILE_SIZE) {
                ZimbraLog.fb.error("The free/busy replication queue is too large: "+mFilename+" ("+len+")");
                return;
            }
            FileInputStream in = null;
            String[] tokens = null;
            try {
                in = new FileInputStream(f);
                byte[] buf = ByteUtil.readInput(in, (int)len, MAX_FILE_SIZE);
                tokens = new String(buf, "UTF-8").split("\n");
            } finally {
                if (in != null)
                    in.close();
            }
            if (tokens.length < 2)
                return;
            int numTokens = Integer.parseInt(tokens[0]);
            if (numTokens != tokens.length) {
                ZimbraLog.fb.error("The free/busy replication queue is inconsistent: "
                        +"numTokens="+numTokens+", actual="+tokens.length);
                return;
            }
            clear();
            Collections.addAll(this, tokens);
            removeFirst();
        }

        public String getFilename() {
            return mFilename;
        }
    }
}
