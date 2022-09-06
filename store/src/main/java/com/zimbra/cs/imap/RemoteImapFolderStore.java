// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.google.common.base.MoreObjects;
import com.zimbra.client.ZFolder;
import com.zimbra.common.mailbox.FolderStore;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.util.ItemId;

public class RemoteImapFolderStore implements ImapFolderStore {
  private transient ZFolder folder;

  public RemoteImapFolderStore(ZFolder folder) {
    this.folder = folder;
  }

  @Override
  public String getFolderIdAsString() {
    return (folder == null) ? null : folder.getFolderIdAsString();
  }

  @Override
  public FolderStore getFolderStore() {
    return folder;
  }

  @Override
  public boolean isUserRootFolder() {
    try {
      return (new ItemId(folder.getId(), (String) null).getId() == Mailbox.ID_FOLDER_USER_ROOT);
    } catch (ServiceException e) {
      return true; // Shouldn't happen but assume the worst if it does
    }
  }

  @Override
  public boolean isIMAPDeleted() {
    return folder.isIMAPDeleted();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("folder", folder).toString();
  }
}
