// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.client.ZFolder;
import com.zimbra.common.mailbox.FolderStore;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Folder;

public interface ImapFolderStore {

  public String getFolderIdAsString();

  public boolean isUserRootFolder();

  public boolean isIMAPDeleted();

  public FolderStore getFolderStore();

  public static ImapFolderStore get(FolderStore folder) throws ServiceException {
    if (folder == null) {
      return null;
    }
    if (folder instanceof Folder) {
      return new LocalImapFolderStore((Folder) folder);
    }
    if (folder instanceof ZFolder) {
      return new RemoteImapFolderStore((ZFolder) folder);
    }
    return null; // TODO or throw an exception?
  }
}
