// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.account.Key.AccountBy;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.util.TypedIdList;

public class ConversationTest {
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

 @Test
 void delete() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  mbox.beginTrackingSync();
  // root message in Inbox
  int msgId = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  // two replies in Trash
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_TRASH).setConversationId(-msgId);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject"), dopt, null);
  Message msg3 = mbox.addMessage(null, MailboxTestUtil.generateMessage("Fwd: test subject"), dopt, null);

  int modSeq = msg3.getModifiedSequence();

  // make sure they're all grouped in a single conversation
  int convId = msg3.getConversationId();
  assertEquals(3, mbox.getConversationById(null, convId).getSize(), "3 messages in conv");

  // empty trash and make sure we're down to 1 message in the conversation
  mbox.emptyFolder(null, Mailbox.ID_FOLDER_TRASH, true);
  assertEquals(1, mbox.getConversationById(null, convId).getSize(), "1 message remaining in conv (cache)");

  // clear the cache and make sure the counts are correct in the DB as well
  mbox.purge(MailItem.Type.CONVERSATION);
  assertEquals(1, mbox.getConversationById(null, convId).getSize(), "1 message remaining in conv (DB)");

  TypedIdList list = mbox.getTombstones(modSeq);
  List<Integer> tombstoneConvs = list.getIds(MailItem.Type.CONVERSATION);
  assertNull(tombstoneConvs, "No conv tombstone yet");

  mbox.move(null, msgId, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_TRASH);
  mbox.emptyFolder(null, Mailbox.ID_FOLDER_TRASH, true);
  list = mbox.getTombstones(modSeq);
  tombstoneConvs = list.getIds(MailItem.Type.CONVERSATION);
  assertNotNull(tombstoneConvs, "conv tombstone exist");
  assertEquals(1, tombstoneConvs.size(), "conv tombstone size");
  assertEquals((Integer) convId, tombstoneConvs.get(0), "conv tombstone id");
 }

 @Test
 void expiry() throws Exception {
  Account account = Provisioning.getInstance().get(AccountBy.id, MockProvisioning.DEFAULT_ACCOUNT_ID);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  // root message in Inbox
  int msgId = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  // two old replies in Trash
  long old = System.currentTimeMillis() - 3 * Constants.MILLIS_PER_MONTH;
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_TRASH).setConversationId(-msgId);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject").setReceivedDate(old), dopt, null);
  Message msg3 = mbox.addMessage(null, MailboxTestUtil.generateMessage("Fwd: test subject").setReceivedDate(old), dopt, null);

  // make sure they're all grouped in a single conversation
  int convId = msg3.getConversationId();
  assertEquals(3, mbox.getConversationById(null, convId).getSize(), "3 messages in conv");

  // purge old messages and make sure we're down to 1 message in the conversation
  account.setMailTrashLifetime("30d");
  account.setMailPurgeUseChangeDateForTrash(false);
  mbox.purgeMessages(null);
  assertEquals(0, mbox.getFolderById(null, Mailbox.ID_FOLDER_TRASH).getSize(), "empty Trash folder");
  assertEquals(1, mbox.getConversationById(null, convId).getSize(), "1 message remaining in conv (cache)");

  // clear the cache and make sure the counts are correct in the DB as well
  mbox.purge(MailItem.Type.CONVERSATION);
  assertEquals(1, mbox.getConversationById(null, convId).getSize(), "1 message remaining in conv (DB)");
 }
}
