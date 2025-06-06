// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SetRetentionPolicyTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  /** Verifies serializing, deserializing, and replaying for folder. */
  @Test
  void redoFolder() throws Exception {
		Mailbox mbox =
        MailboxManager.getInstance().getMailboxByAccount(AccountUtil.createAccount());

    // Create folder.
    Folder folder =
        mbox.createFolder(
            null, "/redo", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
    assertEquals(0, folder.getRetentionPolicy().getPurgePolicy().size());

    // Create RedoableOp.
    RetentionPolicy rp = new RetentionPolicy(List.of(Policy.newUserPolicy("45m")));
    SetRetentionPolicy redoPlayer =
        new SetRetentionPolicy(mbox.getId(), MailItem.Type.FOLDER, folder.getId(), rp);

    // Serialize, deserialize, and redo.
    byte[] data = redoPlayer.testSerialize();
    redoPlayer = new SetRetentionPolicy();
    redoPlayer.setMailboxId(mbox.getId());
    redoPlayer.testDeserialize(data);
    redoPlayer.redo();
    folder = mbox.getFolderById(null, folder.getId());
    assertEquals(1, folder.getRetentionPolicy().getPurgePolicy().size());
    assertEquals("45m", folder.getRetentionPolicy().getPurgePolicy().get(0).getLifetime());
  }

  /** Verifies serializing, deserializing, and replaying for tag. */
  @Test
  void redoTag() throws Exception {
		Mailbox mbox =
        MailboxManager.getInstance().getMailboxByAccount(AccountUtil.createAccount());

    // Create folder.
    Tag tag = mbox.createTag(null, "tag", (byte) 0);
    assertEquals(0, tag.getRetentionPolicy().getPurgePolicy().size());

    // Create RedoableOp.
    RetentionPolicy rp = new RetentionPolicy(List.of(Policy.newUserPolicy("45m")));
    SetRetentionPolicy redoPlayer =
        new SetRetentionPolicy(mbox.getId(), MailItem.Type.TAG, tag.getId(), rp);

    // Serialize, deserialize, and redo.
    byte[] data = redoPlayer.testSerialize();
    redoPlayer = new SetRetentionPolicy();
    redoPlayer.setMailboxId(mbox.getId());
    redoPlayer.testDeserialize(data);
    redoPlayer.redo();

    tag = mbox.getTagById(null, tag.getId());
    assertEquals(1, tag.getRetentionPolicy().getPurgePolicy().size());
    assertEquals("45m", tag.getRetentionPolicy().getPurgePolicy().get(0).getLifetime());
  }
}
