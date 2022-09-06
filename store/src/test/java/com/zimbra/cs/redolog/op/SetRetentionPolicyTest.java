// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.Assert.assertEquals;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SetRetentionPolicyTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  /** Verifies serializing, deserializing, and replaying for folder. */
  @Test
  public void redoFolder() throws Exception {
    Mailbox mbox =
        MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

    // Create folder.
    Folder folder =
        mbox.createFolder(
            null, "/redo", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
    assertEquals(0, folder.getRetentionPolicy().getKeepPolicy().size());
    assertEquals(0, folder.getRetentionPolicy().getPurgePolicy().size());

    // Create RedoableOp.
    RetentionPolicy rp =
        new RetentionPolicy(
            Arrays.asList(Policy.newSystemPolicy("123")),
            Arrays.asList(Policy.newUserPolicy("45m")));
    SetRetentionPolicy redoPlayer =
        new SetRetentionPolicy(mbox.getId(), MailItem.Type.FOLDER, folder.getId(), rp);

    // Serialize, deserialize, and redo.
    byte[] data = redoPlayer.testSerialize();
    redoPlayer = new SetRetentionPolicy();
    redoPlayer.setMailboxId(mbox.getId());
    redoPlayer.testDeserialize(data);
    redoPlayer.redo();
    folder = mbox.getFolderById(null, folder.getId());
    assertEquals(1, folder.getRetentionPolicy().getKeepPolicy().size());
    assertEquals(1, folder.getRetentionPolicy().getPurgePolicy().size());
    assertEquals("45m", folder.getRetentionPolicy().getPurgePolicy().get(0).getLifetime());
    assertEquals("123", folder.getRetentionPolicy().getKeepPolicy().get(0).getId());
  }

  /** Verifies serializing, deserializing, and replaying for tag. */
  @Test
  public void redoTag() throws Exception {
    Mailbox mbox =
        MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

    // Create folder.
    Tag tag = mbox.createTag(null, "tag", (byte) 0);
    assertEquals(0, tag.getRetentionPolicy().getKeepPolicy().size());
    assertEquals(0, tag.getRetentionPolicy().getPurgePolicy().size());

    // Create RedoableOp.
    RetentionPolicy rp =
        new RetentionPolicy(
            Arrays.asList(Policy.newSystemPolicy("123")),
            Arrays.asList(Policy.newUserPolicy("45m")));
    SetRetentionPolicy redoPlayer =
        new SetRetentionPolicy(mbox.getId(), MailItem.Type.TAG, tag.getId(), rp);

    // Serialize, deserialize, and redo.
    byte[] data = redoPlayer.testSerialize();
    redoPlayer = new SetRetentionPolicy();
    redoPlayer.setMailboxId(mbox.getId());
    redoPlayer.testDeserialize(data);
    redoPlayer.redo();

    tag = mbox.getTagById(null, tag.getId());
    assertEquals(1, tag.getRetentionPolicy().getKeepPolicy().size());
    assertEquals(1, tag.getRetentionPolicy().getPurgePolicy().size());
    assertEquals("45m", tag.getRetentionPolicy().getPurgePolicy().get(0).getLifetime());
    assertEquals("123", tag.getRetentionPolicy().getKeepPolicy().get(0).getId());
  }
}
