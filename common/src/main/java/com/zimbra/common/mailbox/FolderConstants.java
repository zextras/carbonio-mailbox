// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

public final class FolderConstants {
  private FolderConstants() {}

  public static final int ID_AUTO_INCREMENT = -1;
  public static final int ID_FOLDER_USER_ROOT = 1;
  public static final int ID_FOLDER_INBOX = 2;
  public static final int ID_FOLDER_TRASH = 3;
  public static final int ID_FOLDER_SPAM = 4;
  public static final int ID_FOLDER_SENT = 5;
  public static final int ID_FOLDER_DRAFTS = 6;
  public static final int ID_FOLDER_CONTACTS = 7;
  public static final int ID_FOLDER_TAGS = 8;
  public static final int ID_FOLDER_CONVERSATIONS = 9;
  public static final int ID_FOLDER_CALENDAR = 10;
  public static final int ID_FOLDER_ROOT = 11;

  // no longer created in new mailboxes since Helix (bug 39647).  old mailboxes may still contain a
  // system folder with id 12
  @Deprecated public static final int ID_FOLDER_NOTEBOOK = 12;
  public static final int ID_FOLDER_AUTO_CONTACTS = 13;
  public static final int ID_FOLDER_IM_LOGS = 14;
  public static final int ID_FOLDER_TASKS = 15;
  public static final int ID_FOLDER_BRIEFCASE = 16;
  public static final int ID_FOLDER_COMMENTS = 17;
  // ID_FOLDER_PROFILE Was used for folder related to ProfileServlet which was used in pre-release
  // Iron Maiden only.
  // Old mailboxes may still contain a system folder with id 18
  @Deprecated public static final int ID_FOLDER_PROFILE = 18;

  public static final int ID_FOLDER_UNSUBSCRIBE = 19;

  public static final int HIGHEST_SYSTEM_ID = 19;
}
