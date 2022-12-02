// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxListener;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.session.PendingModifications;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.cs.session.PendingModifications.ModificationKey;

public class FilterListener extends MailboxListener {

    public static final ImmutableSet<MailboxOperation> EVENTS = ImmutableSet.of(
            MailboxOperation.MoveItem, MailboxOperation.DeleteItem,
            MailboxOperation.RenameItem, MailboxOperation.RenameItemPath,
            MailboxOperation.RenameTag
    );

    public static final ImmutableSet<MailItem.Type> ITEMTYPES = ImmutableSet.of(
            MailItem.Type.FOLDER, MailItem.Type.MOUNTPOINT, MailItem.Type.TAG
    );

    @Override
    public void notify(ChangeNotification notification) {
        if (notification.mods.modified != null && EVENTS.contains(notification.op)) {
            for (PendingModifications.Change change : notification.mods.modified.values()) {
                if (change.what instanceof Folder) {
                    if ((change.why & Change.PARENT) == 0 && (change.why & Change.NAME) == 0) {
                        continue;
                    }
                    Folder folder = (Folder) change.what;
                    Folder oldFolder = (Folder) change.preModifyObj;
                    if (oldFolder == null) {
                        ZimbraLog.filter.warn("Cannot determine the old folder name for %s.", folder.getName());
                        continue;
                    }
                    updateFilterRules(notification.mailboxAccount, folder, oldFolder.getPath());
                } else if (change.what instanceof Tag) {
                    if ((change.why & Change.NAME) == 0) {
                        continue;
                    }
                    Tag tag = (Tag) change.what;
                    Tag oldTag = (Tag) change.preModifyObj;
                    if (oldTag == null) {
                        ZimbraLog.filter.warn("Cannot determine the old tag name for %s.", tag.getName());
                        continue;
                    }
                    updateFilterRules(notification.mailboxAccount, tag, oldTag.getName());
                }
            }
        }
        if (notification.mods.deleted != null) {
            for (Map.Entry<ModificationKey, Change> entry : notification.mods.deleted.entrySet()) {
                MailItem.Type type = (MailItem.Type) entry.getValue().what;
                if (type == MailItem.Type.FOLDER || type == MailItem.Type.MOUNTPOINT) {
                    Folder oldFolder = (Folder) entry.getValue().preModifyObj;
                    if (oldFolder == null) {
                        ZimbraLog.filter.warn("Cannot determine the old folder name for %s.", entry.getKey());
                        continue;
                    }
                    updateFilterRules(notification.mailboxAccount, (Folder) null, oldFolder.getPath());
                } else if (type == MailItem.Type.TAG) {
                    Tag oldTag = (Tag) entry.getValue().preModifyObj;
                    updateFilterRules(notification.mailboxAccount, oldTag);
                }
            }
        }
    }

    @Override
    public Set<MailItem.Type> registerForItemTypes() {
        return ITEMTYPES;
    }

    private void updateFilterRules(Account account, Folder folder, String oldPath) {
        try {
            if (folder == null || folder.inTrash() || folder.isHidden()) {
                ZimbraLog.filter.info("Disabling filter rules that reference %s.", oldPath);
                RuleManager.folderDeleted(account, oldPath);
            } else if (!folder.getPath().equals(oldPath)) {
                ZimbraLog.filter.info("Updating filter rules that reference %s.", oldPath);
                RuleManager.folderRenamed(account, oldPath, folder.getPath());
            }
        } catch (ServiceException e) {
            ZimbraLog.filter.warn("Unable to update filter rules with new folder path.", e);
        }
    }

    private void updateFilterRules(Account account, Tag tag, String oldName) {
        try {
            ZimbraLog.filter.info("Updating filter rules that reference %s.", oldName);
            RuleManager.tagRenamed(account, oldName, tag.getName());
        } catch (ServiceException e) {
            ZimbraLog.filter.warn("Unable to update filter rules with new folder path.", e);
        }
    }

    private void updateFilterRules(Account account, Tag tag) {
        try {
            ZimbraLog.filter.info("Disabling filter rules that reference %s.", tag.getName());
            RuleManager.tagDeleted(account, tag.getName());
        } catch (ServiceException e) {
            ZimbraLog.filter.warn("Unable to update filter rules with new folder path.", e);
        }
    }
}
