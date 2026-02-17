// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.UUIDUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbResults;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.mailbox.MailItem.CustomMetadata;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mime.ParsedMessage;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Folder}.
 */
class FolderTest extends MailboxTestSuite {

    private Account testAccount;

    private static void checkName(Mailbox mbox, String name, boolean valid) {
        try {
            mbox.createFolder(null, name, Mailbox.ID_FOLDER_USER_ROOT, new Folder.FolderOptions().setDefaultView(
                Type.FOLDER));
            if (!valid) {
                Assertions.fail("should not have been allowed to create folder: [" + name + "]");
            }
        } catch (ServiceException e) {
            assertEquals(MailServiceException.INVALID_NAME, e.getCode(), "unexpected error code");
            if (valid) {
                Assertions.fail("should have been allowed to create folder: [" + name + "]");
            }
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        clearData();
        initData();

        // Create test account using MailboxTestSuite utilities
        testAccount = createAccount()
            .withUsername("test")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .create();
    }

    private int checkMODSEQ(String msg, Mailbox mbox, int folderId, int lastMODSEQ) throws Exception {
        int modseq = mbox.getFolderById(null, folderId).getImapMODSEQ();
        assertTrue(modseq != lastMODSEQ, "modseq change after " + msg);
        return modseq;
    }

 @Test
 void accountShouldHaveAnArchiveFolder() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName(testAccount.getName());
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
  Folder archive = mbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
  assertNotNull(archive, "Archive folder should have been added");
  assertEquals("Archive", archive.getName(), "Archive folder should have correct name");
  assertEquals(Folder.FOLDER_IS_IMMUTABLE, archive.getAttributes(), "Archive folder should have correct attributes");
 }

 @Test
 void imapMODSEQ() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName(testAccount.getName());
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // initial state: empty folder
  Folder f = mbox.createFolder(null, "foo", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  int folderId = f.getId(), modseq = f.getImapMODSEQ();

  // add a message to the folder
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(folderId).setFlags(Flag.BITMASK_UNREAD);
  int msgId = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
  modseq = checkMODSEQ("message add", mbox, folderId, modseq);

  // mark message read
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.UNREAD, false, null);
  modseq = checkMODSEQ("mark read", mbox, folderId, modseq);

  // move message out of folder
  mbox.move(null, msgId, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX);
  modseq = checkMODSEQ("move msg out", mbox, folderId, modseq);

  // move message back into folder
  mbox.move(null, msgId, MailItem.Type.MESSAGE, folderId);
  modseq = checkMODSEQ("move msg in", mbox, folderId, modseq);

  // mark message answered
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.REPLIED, true, null);
  modseq = checkMODSEQ("mark answered", mbox, folderId, modseq);

  // move virtual conversation out of folder
  mbox.move(null, -msgId, MailItem.Type.CONVERSATION, Mailbox.ID_FOLDER_INBOX);
  modseq = checkMODSEQ("move vconv out", mbox, folderId, modseq);

  // move virtual conversation back into folder
  mbox.move(null, -msgId, MailItem.Type.CONVERSATION, folderId);
  checkMODSEQ("move vconv in", mbox, folderId, modseq);

  // add a draft reply to the message (don't care about modseq change)
  ParsedMessage pm = new ParsedMessage(ThreaderTest.getSecondMessage(), false);
  mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT, Integer.toString(msgId), MailSender.MSGTYPE_REPLY, null, null, 0L);
  modseq = mbox.getFolderById(null, folderId).getImapMODSEQ();

  // move conversation out of folder
  int convId = mbox.getMessageById(null, msgId).getConversationId();
  mbox.move(null, convId, MailItem.Type.CONVERSATION, Mailbox.ID_FOLDER_INBOX);
  modseq = checkMODSEQ("move conv out", mbox, folderId, modseq);

  // move conversation back into folder
  mbox.move(null, convId, MailItem.Type.CONVERSATION, folderId);
  checkMODSEQ("move conv in", mbox, folderId, modseq);

  // tag message
  Tag tag = mbox.createTag(null, "taggity", (byte) 3);
  modseq = mbox.getFolderById(null, folderId).getImapMODSEQ();
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag.getName(), true, null);
  modseq = checkMODSEQ("add tag", mbox, folderId, modseq);

  // rename tag
  mbox.rename(null, tag.getId(), MailItem.Type.TAG, "blaggity", Mailbox.ID_AUTO_INCREMENT);
  modseq = checkMODSEQ("rename tag", mbox, folderId, modseq);

  // untag message
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag.getName(), false, null);
  modseq = checkMODSEQ("remove tag", mbox, folderId, modseq);

  // retag message
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag.getName(), true, null);
  modseq = checkMODSEQ("re-add tag", mbox, folderId, modseq);

  // delete tag
  mbox.delete(null, tag.getId(), MailItem.Type.TAG);
  modseq = checkMODSEQ("tag delete", mbox, folderId, modseq);

  // hard delete message
  mbox.delete(null, msgId, MailItem.Type.MESSAGE);
  checkMODSEQ("hard delete", mbox, folderId, modseq);
 }

 @Test
 void checkpointRECENT() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
  int changeId = mbox.getLastChangeID();
  Folder inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
  int modMetadata = inbox.getModifiedSequence();
  int modContent = inbox.getSavedSequence();
  assertEquals(0, inbox.getImapRECENTCutoff());

  mbox.recordImapSession(Mailbox.ID_FOLDER_INBOX);

  inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
  assertEquals(changeId, mbox.getLastChangeID());
  assertEquals(modMetadata, inbox.getModifiedSequence());
  assertEquals(modContent, inbox.getSavedSequence());
  assertEquals(mbox.getLastItemId(), inbox.getImapRECENTCutoff());
 }

 @Test
 void defaultFolderFlags() throws Exception {
  try {
   testAccount.setDefaultFolderFlags("*");
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
   Folder inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
   assertTrue(inbox.isFlagSet(Flag.BITMASK_SUBSCRIBED));
  } finally {
   testAccount.setDefaultFolderFlags(null); //don't leave account in modified state since other tests (such as create) assume no default flags
  }
 }

 @Test
 void deleteFolder() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);

  Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(Type.FOLDER);
  Folder root = mbox.createFolder(null, "/Root", fopt);
  mbox.createFolder(null, "/Root/test1", fopt);
  mbox.createFolder(null, "/Root/test2", fopt);

  // Verify folders exist
  assertDoesNotThrow(() -> mbox.getFolderByPath(null, "/Root"));
  assertDoesNotThrow(() -> mbox.getFolderByPath(null, "/Root/test1"));
  assertDoesNotThrow(() -> mbox.getFolderByPath(null, "/Root/test2"));

  // delete the root folder and make sure it and all the leaves are gone
  mbox.delete(null, root.mId, MailItem.Type.FOLDER);

  assertThrows(NoSuchItemException.class, () -> mbox.getFolderByPath(null, "/Root"));
  assertThrows(NoSuchItemException.class, () -> mbox.getFolderByPath(null, "/Root/test1"));
  assertThrows(NoSuchItemException.class, () -> mbox.getFolderByPath(null, "/Root/test2"));
 }

 /**
  * Confirms that deleting a parent folder also deletes the child.
  */
 @Test
 void deleteParent() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
  Folder parent = mbox.createFolder(null, "/" + "deleteParent-parent", new Folder.FolderOptions());
  int parentId = parent.getId();
  Folder child = mbox.createFolder(null, "deleteParent-child", parent.getId(), new Folder.FolderOptions());
  int childId = child.getId();
  mbox.delete(null, parent.getId(), parent.getType());

  // Look up parent by id
  assertThrows(NoSuchItemException.class, () -> mbox.getFolderById(null, parentId),
      "Parent folder lookup by id should have not succeeded");

  // Look up parent by query
  String sql =
    "SELECT id " +
      "FROM " + DbMailItem.getMailItemTableName(mbox) +
      " WHERE mailbox_id = " + mbox.getId() + " AND id = " + parentId;
  DbResults results = DbUtil.executeQuery(sql);
  assertEquals(0, results.size(), "Parent folder query returned data.  id=" + parentId);

  // Look up child by id
  assertThrows(NoSuchItemException.class, () -> mbox.getFolderById(null, childId),
      "Child folder lookup by id should have not succeeded");

  // Look up child by query
  sql =
    "SELECT id " +
      "FROM " + DbMailItem.getMailItemTableName(mbox) +
      " WHERE mailbox_id = " + mbox.getId() + " AND id = " + childId;
  results = DbUtil.executeQuery(sql);
  assertEquals(0, results.size(), "Child folder query returned data.  id=" + childId);
 }

 /**
  * Confirms that emptying a folder removes subfolders only when requested.
  */
 @Test
 void emptyFolderNonrecursive() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
  Folder parent = mbox.createFolder(null, "/" + "emptyParent", new Folder.FolderOptions());
  int parentId = parent.getId();
  Folder child = mbox.createFolder(null, "emptyChild", parent.getId(), new Folder.FolderOptions());
  int childId = child.getId();
  mbox.emptyFolder(null, parent.getId(), false);

  // Look up parent by id
  mbox.getFolderById(null, parentId);

  // Look up parent by query
  String sql =
    "SELECT id " +
      "FROM " + DbMailItem.getMailItemTableName(mbox) +
      " WHERE mailbox_id = " + mbox.getId() + " AND id = " + parentId;
  DbResults results = DbUtil.executeQuery(sql);
  assertEquals(1, results.size(), "Parent folder query returned no data.  id=" + parentId);

  // Look up child by id
  mbox.getFolderById(null, childId);

  // Look up child by query
  sql =
    "SELECT id " +
      "FROM " + DbMailItem.getMailItemTableName(mbox) +
      " WHERE mailbox_id = " + mbox.getId() + " AND id = " + childId;
  results = DbUtil.executeQuery(sql);
  assertEquals(1, results.size(), "Child folder query returned no data.  id=" + childId);
 }

 /**
  * Confirms that emptying a folder removes subfolders only when requested.
  */
 @Test
 void testEmptyFolderRecursive() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
  Folder parent = mbox.createFolder(null, "/" + "emptyRecursiveParent", new Folder.FolderOptions());
  int parentId = parent.getId();
  Folder child = mbox.createFolder(null, "emptyRecursiveChild", parent.getId(), new Folder.FolderOptions());
  int childId = child.getId();
  mbox.emptyFolder(null, parent.getId(), true);

  // Look up parent by id
  mbox.getFolderById(null, parentId);

  // Look up parent by query
  String sql =
    "SELECT id " +
      "FROM " + DbMailItem.getMailItemTableName(mbox) +
      " WHERE mailbox_id = " + mbox.getId() + " AND id = " + parentId;
  DbResults results = DbUtil.executeQuery(sql);
  assertEquals(1, results.size(), "Parent folder query returned no data.  id=" + parentId);

  // Look up child by id
  assertThrows(NoSuchItemException.class, () -> mbox.getFolderById(null, childId),
      "Child folder lookup by id should have not succeeded");

  // Look up child by query
  sql =
    "SELECT id " +
      "FROM " + DbMailItem.getMailItemTableName(mbox) +
      " WHERE mailbox_id = " + mbox.getId() + " AND id = " + childId;
  results = DbUtil.executeQuery(sql);
  assertEquals(0, results.size(), "Child folder query returned data.  id=" + childId);
 }

 /**
  * Creates a hierarchy twenty folders deep.
  */
 @Test
 void manySubfolders() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
  final int NUM_LEVELS = 20;
  int parentId = Mailbox.ID_FOLDER_INBOX;
  Folder top = null;

  for (int i = 1; i <= NUM_LEVELS; i++) {
   Folder folder = mbox.createFolder(null, "manySubfolders " + i, parentId, new Folder.FolderOptions());
   if (i == 1) {
    top = folder;
   }
   parentId = folder.getId();
  }

  if (top != null) {
    mbox.delete(null, top.getId(), top.getType());
  }
 }

 /**
  * Deletes a folder that contains messages in a conversation.  Confirms
  * that the conversation size was correctly decremented.
  */
 @Test
 void markDeletionTargets() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
  String name = "MDT";

  // Create three messages and move two of them into a new folder.
  Message m1 = addTestMessage(mbox, name);
  ZimbraLog.test.debug("Created message 1, id=" + m1.getId());
  Message m2 = addTestMessage(mbox, "RE: " + name);
  ZimbraLog.test.debug("Created message 2, id=" + m2.getId());
  Message m3 = addTestMessage(mbox, "RE: " + name);
  ZimbraLog.test.debug("Created message 3, id=" + m3.getId());

  Folder f = mbox.createFolder(null, name, Mailbox.ID_FOLDER_INBOX, new Folder.FolderOptions());
  mbox.move(null, m1.getId(), m1.getType(), f.getId());
  mbox.move(null, m2.getId(), m2.getType(), f.getId());

  // Verify conversation size
  Conversation conv = mbox.getConversationById(null, m1.getConversationId());
  int convId = conv.getId();
  assertEquals(3, conv.getSize(), "Conversation size before folder delete");

  // Delete the folder and confirm that the conversation size was decremented
  mbox.delete(null, f.getId(), f.getType());
  conv = mbox.getConversationById(null, convId);
  assertEquals(1, conv.getSize(), "Conversation size after folder delete");
 }

 /**
  * Helper method to add a test message
  */
 private Message addTestMessage(Mailbox mbox, String subject) throws Exception {
     String content = "From: sender@" + DEFAULT_DOMAIN_NAME + "\r\n" +
                      "To: " + testAccount.getName() + "\r\n" +
                      "Subject: " + subject + "\r\n" +
                      "Date: Mon, 1 Jan 2007 12:00:00 -0800\r\n" +
                      "Message-ID: <" + System.currentTimeMillis() + "@" + DEFAULT_DOMAIN_NAME + ">\r\n" +
                      "\r\n" +
                      "This is a test message.";
     ParsedMessage pm = new ParsedMessage(content.getBytes(), false);
     DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
     return mbox.addMessage(null, pm, dopt, null);
 }

 /**
  * Confirms that deleting a subfolder correctly updates the subfolder hierarchy.
  */
 @Test
 void updateHierarchy() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);

  Folder f1 = mbox.createFolder(null, "/f1-hierarchy", new Folder.FolderOptions());
  Folder f2 = mbox.createFolder(null, "/f1-hierarchy/f2", new Folder.FolderOptions());
  mbox.createFolder(null, "/f1-hierarchy/f2/f3", new Folder.FolderOptions());
  assertEquals(3, f1.getSubfolderHierarchy().size(), "Hierarchy size before delete");

  mbox.delete(null, f2.getId(), f2.getType());
  f1 = mbox.getFolderById(null, f1.getId());
  List<Folder> hierarchy = f1.getSubfolderHierarchy();
  assertEquals(1, hierarchy.size(), "Hierarchy size after delete");
  assertEquals(f1.getId(), hierarchy.get(0).getId(), "Folder id");
 }

 @Test
 void names() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);

  // empty or all-whitespace
  checkName(mbox, "", false);
  checkName(mbox, "   ", false);

  // invalid path characters
  checkName(mbox, "sam\rwise", false);
  checkName(mbox, "sam\nwise", false);
  checkName(mbox, "sam\twise", false);
  checkName(mbox, "sam\u0003wise", false);
  checkName(mbox, "sam\uFFFEwise", false);
  checkName(mbox, "sam\uDBFFwise", false);
  checkName(mbox, "sam\uDC00wise", false);
  checkName(mbox, "sam/wise", false);
  checkName(mbox, "sam\"wise", false);
  checkName(mbox, "sam:wise", false);

  // reserved names
  checkName(mbox, ".", false);
  checkName(mbox, "..", false);
  checkName(mbox, ".  ", false);
  checkName(mbox, ".. ", false);

  // valid path characters
  checkName(mbox, "sam\\wise", true);
  checkName(mbox, "sam'wise", true);
  checkName(mbox, "sam*wise", true);
  checkName(mbox, "sam|wise", true);
  checkName(mbox, "sam wise", true);
 }

 @Test
 void create() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);

  final String uuid = UUIDUtil.generateUUID();
  final long date = ((System.currentTimeMillis() - Constants.MILLIS_PER_MONTH) / 1000) * 1000;

  Folder.FolderOptions fopt = new Folder.FolderOptions();
  fopt.setAttributes(Folder.FOLDER_DONT_TRACK_COUNTS);
  fopt.setColor((byte) 3);
  fopt.setCustomMetadata(new CustomMetadata("s", "d1:a1:be"));
  fopt.setDate(date);
  fopt.setDefaultView(MailItem.Type.CONTACT);
  fopt.setFlags(Flag.BITMASK_CHECKED);
  fopt.setUuid(uuid);
  // setting folder sync URL triggers an error in MockProvisioning; comment out for now
//        fopt.setUrl(url);

  // create the folder and make sure all the options were applied
  Folder folder = mbox.createFolder(null, "test-create", Mailbox.ID_FOLDER_CONTACTS, fopt);

  assertEquals("test-create", folder.getName(), "correct name");
  assertEquals(Mailbox.ID_FOLDER_CONTACTS, folder.getFolderId(), "correct parent");
  assertEquals(Folder.FOLDER_DONT_TRACK_COUNTS, folder.getAttributes(), "correct attributes");
  assertEquals(3, folder.getColor(), "correct color");
  CustomMetadata custom = folder.getCustomData("s");
  assertNotNull(custom, "custom data set");
  assertEquals(1, custom.size(), "1 entry in custom data");
  assertEquals("b", custom.get("a"), "correct custom data");
  assertEquals(date, folder.getDate(), "correct date");
  assertEquals(MailItem.Type.CONTACT, folder.getDefaultView(), "correct view");
  assertEquals(Flag.BITMASK_CHECKED, folder.getFlagBitmask(), "correct flags");
  assertEquals(uuid, folder.getUuid(), "correct uuid");
//        Assert.assertEquals("correct url", url, folder.getUrl());

  // check again after forcing a reload from disk, just in case
  mbox.purge(MailItem.Type.FOLDER);
  folder = mbox.getFolderById(null, folder.getId());

  assertEquals("test-create", folder.getName(), "correct name");
  assertEquals(Mailbox.ID_FOLDER_CONTACTS, folder.getFolderId(), "correct parent");
  assertEquals(Folder.FOLDER_DONT_TRACK_COUNTS, folder.getAttributes(), "correct attributes");
  assertEquals(3, folder.getColor(), "correct color");
  custom = folder.getCustomData("s");
  assertNotNull(custom, "custom data set");
  assertEquals(1, custom.size(), "1 entry in custom data");
  assertEquals("b", custom.get("a"), "correct custom data");
  assertEquals(date, folder.getDate(), "correct date");
  assertEquals(MailItem.Type.CONTACT, folder.getDefaultView(), "correct view");
  assertEquals(Flag.BITMASK_CHECKED, folder.getFlagBitmask(), "correct flags");
  assertEquals(uuid, folder.getUuid(), "correct uuid");
//        Assert.assertEquals("correct url", url, folder.getUrl());
 }
}
