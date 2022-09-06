// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.google.common.base.MoreObjects;
import com.zimbra.common.mailbox.FolderStore;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;

public class LocalImapFolderStore implements ImapFolderStore {

  private transient Folder folder;

  public LocalImapFolderStore(Folder folder) {
    this.folder = folder;
  }

  @Override
  public String getFolderIdAsString() {
    return folder.getFolderIdAsString();
  }

  @Override
  public FolderStore getFolderStore() {
    return folder;
  }

  @Override
  public boolean isUserRootFolder() {
    return (folder.getId() == Mailbox.ID_FOLDER_USER_ROOT);
  }

  @Override
  public boolean isIMAPDeleted() {
    return (folder.isTagged(Flag.FlagInfo.DELETED));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("folder", folder).toString();
  }
}
