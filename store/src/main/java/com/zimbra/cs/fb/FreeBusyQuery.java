// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.fb;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ToXML;
import com.zimbra.soap.ZimbraSoapContext;

public class FreeBusyQuery {
    public static final int CALENDAR_FOLDER_ALL = -1;
	
	private Account mRequestor;
	private long mStart;
	private long mEnd;
	private String mExApptUid;
	
	private HashMap<String,Account> mTargets;
	private HashMap<String, Integer> mTargetFolder;  // optional calendar folder id for each target
	
	// needed for proxying to another mailbox server
	private HttpServletRequest mReq;
	private ZimbraSoapContext mCtxt;
	
	public FreeBusyQuery(HttpServletRequest httpReq, ZimbraSoapContext zsc, Account requestor, long start, long end, String exApptUid) {
		this(httpReq, requestor, start, end, exApptUid);
		mCtxt = zsc;
	}
	
	public FreeBusyQuery(HttpServletRequest httpReq, Account requestor, long start, long end, String exApptUid) {
		mReq = httpReq;
		mRequestor = requestor;
		mStart = start;
		mEnd = end;
		mExApptUid = exApptUid;
		mTargets = new HashMap<String,Account>();
		mTargetFolder = new HashMap<String, Integer>();
	}
	
	public void addAccountId(String accountId, int calFolderId) {
		addUser(accountId, getAccountFromId(accountId), calFolderId);
	}
	
	public void addEmailAddress(String emailAddr, int calFolderId) {
		addUser(emailAddr, getAccountFromName(emailAddr), calFolderId);
	}
	
	public void addId(String id, int calFolderId) {
		addUser(id, getAccountFromUid(id), calFolderId);
	}
	
	private void addUser(String id, Account acct, int calFolderId) {
		mTargets.put(id, acct);
		mTargetFolder.put(id, calFolderId);
	}
	
    private Account getAccountFromUid(String uid) {
    	Provisioning prov = Provisioning.getInstance();
    	Account acct = null;
    	try {
    		if (Provisioning.isUUID(uid))
    			acct = prov.get(AccountBy.id, uid);
    		else
    			acct = prov.get(AccountBy.name, uid);
    	} catch (ServiceException e) {
    		acct = null;
    	}
    	return acct;
    }
    private Account getAccountFromId(String id) {
    	try {
    		return Provisioning.getInstance().get(AccountBy.id, id);
    	} catch (ServiceException e) {
    	}
    	return null;
    }
    private Account getAccountFromName(String name) {
    	try {
    		return Provisioning.getInstance().get(AccountBy.name, name);
    	} catch (ServiceException e) {
    	}
    	return null;
    }
    
    private void prepareRequests(ArrayList<FreeBusy> local, RemoteFreeBusyProvider remote, ArrayList<String> external) {
    	for (String id : mTargets.keySet()) {
    		Account acct = mTargets.get(id);
    		try {
    		    if (acct == null || acct.isAccountExternal() ||
    		        acct.getBooleanAttr(Provisioning.A_zimbraFreebusyLocalMailboxNotActive, false)) {
    		        external.add(id);
    		        continue;
    		    }
    		    int folder = mTargetFolder.get(id);
    		
        		if (Provisioning.onLocalServer(acct)) {
        		    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
        		    OperationContext octxt = null;
        		    if (mCtxt != null && mCtxt.getAuthToken() != null)
        		        octxt = new OperationContext(mCtxt.getAuthToken());
        		    else if (mRequestor != null)
        		        octxt = new OperationContext(mRequestor);
        		    else
        		        octxt = new OperationContext(GuestAccount.ANONYMOUS_ACCT);
                    Appointment exAppt = null;
                    if (mExApptUid != null) {
                        CalendarItem ci = mbox.getCalendarItemByUid(octxt, mExApptUid);
                        if (ci instanceof Appointment)
                            exAppt = (Appointment) ci;
                    }
        		    local.add(mbox.getFreeBusy(octxt, id, mStart, mEnd, folder, exAppt));
        		} else {
        			remote.addFreeBusyRequest(mRequestor, acct, id, mStart, mEnd, folder);
        		}
    		} catch (ServiceException e) {
                ZimbraLog.fb.error("cannot get free/busy for "+id, e);
    		}
    	}
    }
    
    private int getHopcount() {        
        int hopcount = -1;
        String hopcountStr = mReq.getHeader("X-ZIMBRA-FREEBUSY-HOPCOUNT");
        if (hopcountStr != null) {
            try {
                hopcount = Integer.parseInt(hopcountStr);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return hopcount;
    }
    public Collection<FreeBusy> getResults() {
    	RemoteFreeBusyProvider remote = new RemoteFreeBusyProvider(mReq, mCtxt, mStart, mEnd, mExApptUid);
    	ArrayList<String> external = new ArrayList<String>();
    	ArrayList<FreeBusy> fbList = new ArrayList<FreeBusy>();
    	prepareRequests(fbList, remote, external);

    	fbList.addAll(remote.getResults());
    	if (external.size() > 0)
    	    fbList.addAll(FreeBusyProvider.getRemoteFreeBusy(mRequestor, external, mStart, mEnd, CALENDAR_FOLDER_ALL, getHopcount()));	
    	return fbList;
    }
    
	public void getResults(Element response) {
    	RemoteFreeBusyProvider remote = new RemoteFreeBusyProvider(mReq, mCtxt, mStart, mEnd, mExApptUid);
    	ArrayList<String> external = new ArrayList<String>();
    	ArrayList<FreeBusy> fbList = new ArrayList<FreeBusy>();
    	prepareRequests(fbList, remote, external);

    	for (FreeBusy fb : fbList)
        	ToXML.encodeFreeBusy(response, fb);
    	remote.addResults(response);
    	if (external.size() > 0)
    		FreeBusyProvider.getRemoteFreeBusy(mRequestor, response, external, mStart, mEnd, CALENDAR_FOLDER_ALL, getHopcount());	
	}
}
