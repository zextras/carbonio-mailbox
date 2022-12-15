// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.acl.EffectiveACLCache;
import com.zimbra.cs.mailbox.calendar.cache.CalendarCacheManager;
import com.zimbra.cs.memcached.MemcachedConnector;
import com.zimbra.cs.session.PendingLocalModifications;

public class MemcachedCacheManager extends MailboxListener {

    public static void purgeMailbox(Mailbox mbox) throws ServiceException {
        CalendarCacheManager.getInstance().purgeMailbox(mbox);
        EffectiveACLCache.getInstance().purgeMailbox(mbox);
        FoldersTagsCache.getInstance().purgeMailbox(mbox);
    }

    @Override
    public void notify(ChangeNotification notification) {
        PendingLocalModifications mods = notification.mods;
        int changeId = notification.lastChangeId;
        // We have to notify calendar cache before checking memcached connectedness
        // because a portion of calendar cache is not memcached-based.
        CalendarCacheManager.getInstance().notifyCommittedChanges(mods, changeId);
        if (MemcachedConnector.isConnected()) {
            EffectiveACLCache.getInstance().notifyCommittedChanges(mods, changeId);
        }
    }
}
