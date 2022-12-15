// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxListener;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.cs.session.PendingModifications.ModificationKey;

public class DataSourceFolderListener extends MailboxListener {

    public static final ImmutableSet<MailItem.Type> ITEMTYPES = ImmutableSet.of(
            MailItem.Type.FOLDER
    );

    @Override
    public void notify(ChangeNotification notification) {
        if (notification.mods.deleted != null) {
            for (Map.Entry<ModificationKey, Change> entry : notification.mods.deleted.entrySet()) {
                MailItem.Type type = (MailItem.Type) entry.getValue().what;
                if (type == MailItem.Type.FOLDER) {
                    Folder oldFolder = (Folder) entry.getValue().preModifyObj;
                    if (oldFolder == null) {
                        ZimbraLog.datasource.warn("Cannot determine the old folder name for %s.", entry.getKey());
                        continue;
                    }
                    try {
                        ZimbraLog.datasource.info("Deleting datasources that reference %s.", oldFolder.getPath());
                        Account account = oldFolder.getAccount();
                        List<DataSource> datasources = account.getAllDataSources();
                        for (DataSource datasource : datasources) {
                            if (datasource.getFolderId() == oldFolder.getId()) {
                                ZimbraLog.datasource.debug("Deleting datasource %s.", datasource.getName());
                                account.deleteDataSource(datasource.getId());
                            }
                        }
                    } catch (ServiceException e) {
                        ZimbraLog.datasource.warn("Could not delete datasources for folder.", oldFolder.getPath());
                    }
                }
            }
        }
    }

    @Override
    public Set<MailItem.Type> registerForItemTypes() {
        return ITEMTYPES;
    }
}
