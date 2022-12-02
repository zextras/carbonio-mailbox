// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.zimbra.common.account.Key;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ContactAutoComplete.ContactEntry;

public class ContactRankings {
    private static final String CONFIG_KEY_CONTACT_RANKINGS = "CONTACT_RANKINGS";
    private static final String KEY_NAME = "n";
    private static final String KEY_RANKING = "r";
    private static final String KEY_LAST_ACCESSED = "t";

    private int mTableSize;
    private String mAccountId;
    private TreeMap<String,TreeSet<ContactEntry>> mEntryMap;
    private HashMap<String,ContactEntry> mEntries;
    public ContactRankings(String accountId) throws ServiceException {
        mAccountId = accountId;
        mEntryMap = new TreeMap<String,TreeSet<ContactEntry>>();
        mEntries = new HashMap<String,ContactEntry>();
        mTableSize = Provisioning.getInstance().get(Key.AccountBy.id, mAccountId).getIntAttr(Provisioning.A_zimbraContactRankingTableSize, 40);
        if (!LC.contact_ranking_enabled.booleanValue())
            return;
        readFromDatabase();
    }
    public static void reset(String accountId) throws ServiceException {
        if (!LC.contact_ranking_enabled.booleanValue())
            return;
        ContactRankings rankings = new ContactRankings(accountId);
        rankings.mEntryMap.clear();
        rankings.mEntries.clear();
        rankings.writeToDatabase();
    }
    public static void remove(String accountId, String email) throws ServiceException {
        if (!LC.contact_ranking_enabled.booleanValue())
            return;
        ContactRankings rankings = new ContactRankings(accountId);
        ContactEntry entry = rankings.mEntries.get(email.toLowerCase());
        if (entry != null)
            rankings.remove(entry);
        rankings.writeToDatabase();
    }

    public static void increment(String accountId, Collection<? extends Address> addrs) throws ServiceException {
        if (!LC.contact_ranking_enabled.booleanValue())
            return;
        ContactRankings rankings = new ContactRankings(accountId);
        for (Address addr : addrs)
            if (addr instanceof InternetAddress) {
                InternetAddress address = (InternetAddress)addr;
                rankings.increment(address.getAddress(), address.getPersonal());
            }

        rankings.writeToDatabase();
    }

    public static void increment(String accountId, Address[] addrs) throws ServiceException {
        HashSet<Address> addrSet = new HashSet<Address>();
        Collections.addAll(addrSet, addrs);
        increment(accountId, addrSet);
    }

    public synchronized void increment(String email, String displayName) {
        long now = System.currentTimeMillis();
        email = email.toLowerCase();
        ContactEntry entry = mEntries.get(email.toLowerCase());
        if (entry == null) {
            entry = new ContactEntry();
            entry.mEmail = email;
            entry.setName(displayName);
            entry.mRanking = 1;
            entry.mFolderId = ContactAutoComplete.FOLDER_ID_UNKNOWN;
            entry.mLastAccessed = now;

            if (mEntries.size() >= mTableSize) {
                ContactEntry lastEntry = getSortedSet().last();
                if (lastEntry.mRanking < 1)
                    remove(lastEntry);
            }

            if (mEntries.size() < mTableSize) {
                add(entry);
            } else {
                for (ContactEntry e : mEntries.values()) {
                    int weeksOld = (int) ((now - e.mLastAccessed) / Constants.MILLIS_PER_WEEK) + 1;
                    e.mRanking -= weeksOld;
                    if (e.mRanking < 0)
                        e.mRanking = 0;
                }
            }
        } else {
            entry.mRanking++;
            if (entry.mRanking <= 0)
                entry.mRanking = 1;
            if (displayName != null && displayName.length() > 0)
                entry.setName(displayName);
            entry.mLastAccessed = now;
        }
    }
    public int query(String email) {
        ContactEntry entry = mEntries.get(email.toLowerCase());
        if (entry != null)
            return entry.mRanking;
        return 0;
    }
    public synchronized Collection<ContactEntry> search(String str) {
        TreeSet<ContactEntry> entries = new TreeSet<ContactEntry>();
        int len = str.length();
        for (String k : mEntryMap.tailMap(str).keySet()) {
            if (k.length() >= len &&
                    k.substring(0, len).equalsIgnoreCase(str)) {
                entries.addAll(mEntryMap.get(k));
            } else
                break;
        }
        return entries;
    }
    private synchronized TreeSet<ContactEntry> getSortedSet() {
        return new TreeSet<ContactEntry>(mEntries.values());
    }
    private synchronized void readFromDatabase() throws ServiceException {
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(mAccountId);
        Metadata config = mbox.getConfig(null, CONFIG_KEY_CONTACT_RANKINGS);
        if (config == null) {
            config = new Metadata();
            mbox.setConfig(null, CONFIG_KEY_CONTACT_RANKINGS, config);
        }
        for (Map.Entry<Object, Object> entry : config.map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) entry.getValue();
                ContactEntry contact = new ContactEntry();
                contact.mEmail = ((String) entry.getKey()).toLowerCase();
                Long num = (Long) m.get(KEY_RANKING);
                contact.mRanking = num.intValue();
                num = (Long) m.get(KEY_LAST_ACCESSED);
                contact.mLastAccessed = num.longValue();
                contact.setName((String) m.get(KEY_NAME));
                contact.mFolderId = ContactAutoComplete.FOLDER_ID_UNKNOWN;
                add(contact);
            }
        }
        dump("reading");
    }
    private synchronized void writeToDatabase() throws ServiceException {
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(mAccountId);
        Metadata config = new Metadata();
        for (ContactEntry entry : getSortedSet()) {
            Metadata m = new Metadata();
            m.put(KEY_RANKING, entry.mRanking);
            if (entry.mDisplayName != null)
                m.put(KEY_NAME, entry.mDisplayName);
            m.put(KEY_LAST_ACCESSED, entry.mLastAccessed);
            config.put(entry.mEmail, m);
        }
        mbox.setConfig(null, CONFIG_KEY_CONTACT_RANKINGS, config);
        dump("writing");
    }
    private synchronized TreeSet<ContactEntry> get(String str) {
        TreeSet<ContactEntry> val = mEntryMap.get(str.toLowerCase());
        if (val == null) {
            val = new TreeSet<ContactEntry>();
            mEntryMap.put(str.toLowerCase(), val);
        }
        return val;
    }
    private synchronized void add(ContactEntry entry) {
        get(entry.mEmail).add(entry);
        if (entry.mDisplayName.length() > 0)
            get(entry.mDisplayName).add(entry);
        if (entry.mLastName.length() > 0)
            get(entry.mLastName).add(entry);
        mEntries.put(entry.mEmail.toLowerCase(), entry);
    }
    private synchronized void remove(ContactEntry entry) {
        get(entry.mEmail).remove(entry);
        if (entry.mDisplayName.length() > 0)
            get(entry.mDisplayName).remove(entry);
        if (entry.mLastName.length() > 0)
            get(entry.mLastName).remove(entry);
        mEntries.remove(entry.mEmail.toLowerCase());
    }
    private void dump(String action) {
        if (ZimbraLog.gal.isDebugEnabled()) {
            StringBuilder buf = new StringBuilder(action + " contact rankings");
            buf.append("\n");
            for (ContactEntry entry : getSortedSet()) {
                entry.toString(buf);
                buf.append("\n");
            }
            ZimbraLog.gal.debug(buf.toString());
        }
    }
}

