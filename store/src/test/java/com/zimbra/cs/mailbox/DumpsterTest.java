// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.mime.ParsedMessageOptions;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DumpsterTest {

  private Mailbox mbox;
  private Folder folder;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>())
        .setDumpsterEnabled(true);
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
    mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
    folder =
        mbox.createFolder(
            null,
            "/Briefcase/f",
            new Folder.FolderOptions().setDefaultView(Type.FOLDER));
  }

 @Test
 void moveDumpsterEmail() throws Exception {
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg =
    mbox.addMessage(
      null,
      new ParsedMessage("From: test@zimbra.com\r\nTo: test@zimbra.com".getBytes(), false),
      opt,
      null);

  mbox.delete(null, msg.mId, MailItem.Type.MESSAGE);
  msg = (Message) mbox.getItemById(null, msg.mId, MailItem.Type.MESSAGE, true);
  boolean success = false;
  boolean immutableException = false;
  try {
   mbox.move(null, msg.mId, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX);
   success = true;
  } catch (MailServiceException e) {
   immutableException = MailServiceException.NO_SUCH_MSG.equals(e.getCode());
   if (!immutableException) {
    throw e;
   }
  } finally {
   mbox.endTransaction(success);
  }

  assertTrue(immutableException, "expected NO_SUCH_DOC exception");
 }

 @Test
 void reanalyzeDumpsterItem() throws Exception {
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg =
    mbox.addMessage(
      null,
      new ParsedMessage("From: test@zimbra.com\r\nTo: test@zimbra.com".getBytes(), false),
      opt,
      null);

  mbox.delete(null, msg.mId, MailItem.Type.MESSAGE);
  msg = (Message) mbox.getItemById(null, msg.mId, MailItem.Type.MESSAGE, true);
  try {
   ParsedMessage pm = null;
   mbox.lock.lock();
   try {
    // force the pm's received-date to be the correct one
    ParsedMessageOptions messageOptions =
      new ParsedMessageOptions()
        .setContent(msg.getMimeMessage(false))
        .setReceivedDate(msg.getDate())
        .setAttachmentIndexing(mbox.attachmentsIndexingEnabled())
        .setSize(msg.getSize())
        .setDigest(msg.getDigest());
    pm = new ParsedMessage(messageOptions);
   } finally {
    mbox.lock.release();
   }

   pm.setDefaultCharset(mbox.getAccount().getPrefMailDefaultCharset());
   mbox.reanalyze(msg.mId, MailItem.Type.MESSAGE, pm, msg.getSize());
  } catch (MailServiceException e) {
   fail("should not be throwing an exception");
  }
 }

 @Test
 void msgInConversationTest() throws Exception {
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg1 =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  int msgId = msg1.getId();
  Message msg2 =
    mbox.addMessage(
      null,
      MailboxTestUtil.generateMessage("Re: test subject"),
      dopt.setConversationId(-msgId),
      null);
  Message msg3 =
    mbox.addMessage(
      null,
      MailboxTestUtil.generateMessage("Fwd: test subject"),
      dopt.setConversationId(-msgId),
      null);

  // make sure they're all grouped in a single conversation
  int convId = msg3.getConversationId();
  assertEquals(3, mbox.getConversationById(null, convId).getSize(), "3 messages in conv");
  mbox.move(null, convId, MailItem.Type.CONVERSATION, Mailbox.ID_FOLDER_TRASH);
  mbox.delete(null, convId, MailItem.Type.CONVERSATION);
  msg3 = (Message) mbox.getItemById(null, msg3.mId, MailItem.Type.MESSAGE, true);
  boolean noSuchObjException = false;
  try {
   mbox.move(null, msg3.mId, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX);
   fail(
     "should not be able to move message from a conversation that is already in dumpster");
  } catch (MailServiceException e) {
   noSuchObjException = MailServiceException.NO_SUCH_MSG.equals(e.getCode());
   if (!noSuchObjException) {
    throw e;
   }
  }

  assertTrue(noSuchObjException, "expected NO_SUCH_MSG exception");
 }

 @Test
 void conversationTest() throws Exception {
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg1 =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  int msgId = msg1.getId();
  mbox.addMessage(
    null,
    MailboxTestUtil.generateMessage("Re: test subject"),
    dopt.setConversationId(-msgId),
    null);
  Message msg3 =
    mbox.addMessage(
      null,
      MailboxTestUtil.generateMessage("Fwd: test subject"),
      dopt.setConversationId(-msgId),
      null);

  // make sure they're all grouped in a single conversation
  int convId = msg3.getConversationId();
  assertEquals(3, mbox.getConversationById(null, convId).getSize(), "3 messages in conv");
  mbox.move(null, convId, MailItem.Type.CONVERSATION, Mailbox.ID_FOLDER_TRASH);
  mbox.delete(null, convId, MailItem.Type.CONVERSATION);
  boolean noSuchObjException = false;
  try {
   mbox.move(null, convId, MailItem.Type.CONVERSATION, Mailbox.ID_FOLDER_INBOX);
   fail("should not be able to move a conversation that is already in dumpster");
  } catch (MailServiceException e) {
   noSuchObjException = MailServiceException.NO_SUCH_CONV.equals(e.getCode());
   if (!noSuchObjException) {
    throw e;
   }
  }

  assertTrue(noSuchObjException, "expected NO_SUCH_CONV exception");
 }

 @Test
 void folderTest() throws Exception {
  Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(Type.FOLDER);
  Folder rootSource = mbox.createFolder(null, "/DumpsterTestSource", fopt);
  Folder subFolder1 = mbox.createFolder(null, "/DumpsterTestSource/test1", fopt);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(subFolder1.mId);
  Message msg =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  try {
   mbox.getFolderByPath(null, "/DumpsterTestSource");
   mbox.getFolderByPath(null, "/DumpsterTestSource/test1");
  } catch (Exception e) {
   fail();
  }
  boolean noSuchObjException = false;
  // delete the root folder and make sure it and all the leaves are gone
  mbox.move(null, rootSource.mId, MailItem.Type.FOLDER, Mailbox.ID_FOLDER_TRASH);
  mbox.delete(null, subFolder1.mId, MailItem.Type.FOLDER);
  mbox.delete(null, rootSource.mId, MailItem.Type.FOLDER);
  try {
   msg = (Message) mbox.getItemById(null, msg.mId, MailItem.Type.MESSAGE, true);
   assertNotNull(msg, "should find the message in dumpster");
  } catch (Exception e) {
   fail("should find the message in dumpster");
  }
  try {
   mbox.move(null, msg.mId, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX);
   fail("should throw NO_SUCH_MSG exception");
  } catch (MailServiceException e) {
   noSuchObjException = MailServiceException.NO_SUCH_MSG.equals(e.getCode());
   if (!noSuchObjException) {
    throw e;
   }
  }
  assertTrue(noSuchObjException, "expected NO_SUCH_MSG exception");
 }

}
