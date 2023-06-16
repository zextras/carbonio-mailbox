// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;

public class SetActiveSyncDisabledTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
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
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

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
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
  assertFalse(folder.isActiveSyncDisabled());

  //try setting disableActiveSync to true!!
  folder.setActiveSyncDisabled(true);

  //cannot disable activesync for system folders!!
  assertFalse(folder.isActiveSyncDisabled());
 }

}
