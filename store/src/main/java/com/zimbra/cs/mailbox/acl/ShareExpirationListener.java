// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.acl;

import java.util.Date;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.zimbra.common.mailbox.BaseItemInfo;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxListener;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.ScheduledTask;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.session.PendingModifications.Change;

/**
 * Listens to ACL changes on folder and document items and schedules an {@link ExpireGrantsTask} for the item
 * if required.
 */
public class ShareExpirationListener extends MailboxListener {

    private static final ImmutableSet<MailItem.Type> registeredTypes = ImmutableSet.of(
            MailItem.Type.FOLDER);

    @Override
    public Set<MailItem.Type> registerForItemTypes() {
        return registeredTypes;
    }

    @Override
    public void notify(ChangeNotification notification) {
        if (notification.op == MailboxOperation.ExpireAccess) {
            return;
        }
        if (notification.mods.created != null) {
            for (BaseItemInfo created : notification.mods.created.values()) {
                if (created instanceof Folder) {
                    MailItem mi = (MailItem) created;
                    if (mi.getACL() != null) {
                        scheduleExpireAccessOpIfReq(mi);
                    }
                }
            }
        }
        if (notification.mods.modified != null) {
            for (Change change : notification.mods.modified.values()) {
                if ((change.what instanceof Folder) &&
                        (change.why & Change.ACL) != 0) {
                    scheduleExpireAccessOpIfReq((MailItem) change.what);
                }
            }
        }
    }

    static void scheduleExpireAccessOpIfReq(MailItem item) {
        // first cancel any existing task for this item
        try {
            ExpireGrantsTask.cancel(item.getMailboxId(), item.getId());
        } catch (ServiceException e) {
            ZimbraLog.scheduler.warn("Error in canceling existing ExpireGrantsTask for (id=%s,mailboxId=%s)",
                    item.getId(), item.getMailboxId(), e);
            return;
        }
        ACL acl = item.getACL();
        if (acl == null) {
            return;
        }
        long nextExpiry = 0; // 0 indicates never expires
        for (ACL.Grant grant : acl.getGrants()) {
            long expiry = grant.getEffectiveExpiry(acl);
            if (expiry != 0) {
                nextExpiry = nextExpiry == 0 ? expiry : Math.min(expiry, nextExpiry);
            }
        }
        if (nextExpiry == 0) {
            // there isn't any grant that's going to expire
            return;
        }
        ScheduledTask task = new ExpireGrantsTask();
        task.setMailboxId(item.getMailboxId());
        // schedule after a minute of the next expiry time
        task.setExecTime(new Date(nextExpiry + Constants.MILLIS_PER_MINUTE));
        task.setProperty(ExpireGrantsTask.ITEM_ID_PROP_NAME, Integer.toString(item.getId()));
        try {
            ScheduledTaskManager.schedule(task);
        } catch (ServiceException e) {
            ZimbraLog.scheduler.warn("Error in scheduling task %s", task.toString(), e);
        }
    }
}
