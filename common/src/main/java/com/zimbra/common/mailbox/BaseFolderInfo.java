// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.common.mailbox;

public interface BaseFolderInfo {
  /**
   * Returns the folder's absolute path. Paths are UNIX-style with <code>'/'</code> as the path
   * delimiter. Paths are relative to the user root folder, which has the path <code>"/"</code>. So
   * the Inbox's path is <code>"/Inbox"</code>, etc.
   */
  public String getPath();

  public int getFolderIdInOwnerMailbox();
}
