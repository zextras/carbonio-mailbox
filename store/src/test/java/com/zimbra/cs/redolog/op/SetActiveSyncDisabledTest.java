// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SetActiveSyncDisabledTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 /**
  * Verifies serializing, de-serializing, and replaying for folder.
  */
 @Test
 void setDisableActiveSyncUserFolder() throws Exception {
   final Account account = AccountUtil.createAccount();
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());

  // Create folder.
  Folder folder = mbox.createFolder(null, "/test", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  assertFalse(folder.isActiveSyncDisabled());

  // Create RedoableOp.
  SetActiveSyncDisabled redoPlayer = new SetActiveSyncDisabled(mbox.getId(), folder.getId(), true);

  // Serialize, deserialize, and redo.
  byte[] data = redoPlayer.testSerialize();
  redoPlayer = new SetActiveSyncDisabled();
  redoPlayer.setMailboxId(mbox.getId());
  redoPlayer.testDeserialize(data);
  redoPlayer.redo();
  folder = mbox.getFolderById(null, folder.getId());
  assertTrue(folder.isActiveSyncDisabled());
 }

 @Test
 void setDisableActiveSyncSystemFolder() throws Exception {
   final Account account = AccountUtil.createAccount();
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
  assertFalse(folder.isActiveSyncDisabled());

  //try setting disableActiveSync to true!!
  folder.setActiveSyncDisabled(true);

  //cannot disable activesync for system folders!!
  assertFalse(folder.isActiveSyncDisabled());
 }

}
