// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zimbra.client.ZMailbox;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;

public class ImapServerListenerPool {
    private static final ImapServerListenerPool SINGLETON = new ImapServerListenerPool();

    /**
     * Only supporting one ImapServerListener per server.  Initially planned to allow for multiple
     * listeners per server to ensure timely updates when handling large numbers of accounts but
     * believe this can be addressed by having more IMAP server front loaders.
     * May revisit this later if needed.
     */
    private final LoadingCache <String, ImapServerListener> serverToListenerMap = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(16) /* TODO - base on the total number of servers or use LDAP config? */
            .build(new CacheLoader<>() {

              @Override
              public ImapServerListener load(String serverName) throws Exception {
                return new ImapServerListener(serverName);
              }
            });

    private ImapServerListenerPool() {
    }

    public static ImapServerListenerPool getInstance() {
        return SINGLETON;
    }

    public ImapServerListener getForAccountId(String acctId) throws ServiceException {
        Account acct = Provisioning.getInstance().get(AccountBy.id, acctId);
        if(acct == null) {
            ZimbraLog.imap.error("Cannot get ImapServerListener for %s. Account does not exist.", acctId);
            return null;
        }
        try {
            return serverToListenerMap.get(acct.getServerName());
        } catch (ExecutionException e) {
            ZimbraLog.imap.error("Problem getting ImapServerListener for %s", acct.getServerName(), e);
            return null;
        }
    }

    public ImapServerListener get(ZMailbox zmbox) throws ServiceException {
        return getForAccountId(zmbox.getAccountId());
    }
}