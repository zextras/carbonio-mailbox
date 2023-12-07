// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import qa.unittest.TestUtil;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;

public class PurgeTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        Provisioning prov = Provisioning.getInstance();
        prov.deleteAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
        Config config = Provisioning.getInstance().getConfig();
        RetentionPolicyManager mgr = RetentionPolicyManager.getInstance();
        RetentionPolicy rp = mgr.getSystemRetentionPolicy(config);

        List<Policy> purge = rp.getPurgePolicy();
        if (purge != null) {
            for (Policy policy : purge) {
                mgr.deleteSystemPolicy(config, policy.getId());
            }
        }
    }

 @Test
 void folderPurgePolicy() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // Create folder and test messages.
  Folder folder = mbox.createFolder(null, "/folderPurgePolicy", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  Message older = TestUtil.addMessage(mbox, folder.getId(), "test1", System.currentTimeMillis() - (60 * Constants.MILLIS_PER_MINUTE));
  Message newer = TestUtil.addMessage(mbox, folder.getId(), "test2", System.currentTimeMillis() - (30 * Constants.MILLIS_PER_MINUTE));
  folder = mbox.getFolderById(null, folder.getId());

  // Run purge with default settings and make sure nothing was deleted.
  mbox.purgeMessages(null);
  folder = mbox.getFolderById(null, folder.getId());
  assertEquals(2, folder.getSize());

  // Add retention policy.
  Policy p = Policy.newUserPolicy("45m");
  RetentionPolicy purgePolicy = new RetentionPolicy(Arrays.asList(p));
  mbox.setRetentionPolicy(null, folder.getId(), MailItem.Type.FOLDER, purgePolicy);

  // Purge the folder cache and make sure that purge policy is reloaded from metadata.
  mbox.purge(MailItem.Type.FOLDER);
  folder = mbox.getFolderById(null, folder.getId());
  List<Policy> purgeList = folder.getRetentionPolicy().getPurgePolicy();
  assertEquals(1, purgeList.size());
  assertEquals("45m", purgeList.get(0).getLifetime());

  // Run purge and make sure one of the messages was deleted.
  mbox.purgeMessages(null);
  assertEquals(1, folder.getSize());
  mbox.getMessageById(null, newer.getId());
  try {
   mbox.getMessageById(null, older.getId());
   fail("Older message was not purged.");
  } catch (NoSuchItemException e) {
   // Older message was purged.
  }

  // Remove purge policy and verify that the folder state was properly updated.
  mbox.setRetentionPolicy(null, folder.getId(), MailItem.Type.FOLDER, null);
  mbox.purge(MailItem.Type.FOLDER);
  folder = mbox.getFolderById(null, folder.getId());
  assertEquals(0, folder.getRetentionPolicy().getPurgePolicy().size());
 }

 @Test
 void tagPurgePolicy() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // Create folder and test messages.
  Tag tag = mbox.createTag(null, "tag", (byte) 0);
  Folder inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
  Message older = TestUtil.addMessage(mbox, inbox.getId(), "test1", System.currentTimeMillis() - (60 * Constants.MILLIS_PER_MINUTE));
  Message newer = TestUtil.addMessage(mbox, inbox.getId(), "test2", System.currentTimeMillis() - (30 * Constants.MILLIS_PER_MINUTE));
  Message notTagged = TestUtil.addMessage(mbox, inbox.getId(), "test3", System.currentTimeMillis() - (90 * Constants.MILLIS_PER_MINUTE));
  mbox.setTags(null, older.getId(), older.getType(), 0, new String[]{tag.getName()});
  mbox.setTags(null, newer.getId(), newer.getType(), 0, new String[]{tag.getName()});

  // Run purge with default settings and make sure nothing was deleted.
  mbox.purgeMessages(null);
  assertEquals(3, inbox.getSize());

  // Add retention policy.
  Policy p = Policy.newUserPolicy("45m");
  RetentionPolicy purgePolicy = new RetentionPolicy(Arrays.asList(p));
  mbox.setRetentionPolicy(null, tag.getId(), MailItem.Type.TAG, purgePolicy);

  // Purge the tag cache and make sure that purge policy is reloaded from metadata.
  mbox.purge(MailItem.Type.TAG);
  tag = mbox.getTagById(null, tag.getId());
  List<Policy> purgeList = tag.getRetentionPolicy().getPurgePolicy();
  assertEquals(1, purgeList.size());
  assertEquals("45m", purgeList.get(0).getLifetime());

  // Run purge and make sure one of the messages was deleted.
  mbox.purgeMessages(null);
  inbox = mbox.getFolderById(null, inbox.getId());
  assertEquals(2, inbox.getSize());
  mbox.getMessageById(null, newer.getId());
  mbox.getMessageById(null, notTagged.getId());
  try {
   mbox.getMessageById(null, older.getId());
   fail("Older message was not purged.");
  } catch (NoSuchItemException e) {
  }

  // Remove purge policy and verify that the folder state was properly updated.
  mbox.setRetentionPolicy(null, tag.getId(), MailItem.Type.TAG, null);
  mbox.purge(MailItem.Type.TAG);
  tag = mbox.getTagById(null, tag.getId());
  assertEquals(0, tag.getRetentionPolicy().getPurgePolicy().size());
 }

 /**
  * Tests the user retention policy for the <tt>Inbox</tt> folder.
  */
 @Test
 void purgeInbox()
   throws Exception {
  // Set retention policy
  Account account = getAccount();
  account.setPrefInboxUnreadLifetime("24h");
  account.setPrefInboxReadLifetime("16h");

  // Insert messages
  String prefix = "purgeInbox ";
  Mailbox mbox = getMailbox();
  Message purgedUnread = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, prefix + "purgedUnread",
    System.currentTimeMillis() - (25 * Constants.MILLIS_PER_HOUR));
  Message keptUnread = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, prefix + "keptUnread",
    System.currentTimeMillis() - (18 * Constants.MILLIS_PER_HOUR));
  Message purgedRead = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, prefix + "purgedRead",
    System.currentTimeMillis() - (18 * Constants.MILLIS_PER_HOUR));
  Message keptRead = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, prefix + "keptRead",
    System.currentTimeMillis() - (15 * Constants.MILLIS_PER_HOUR));

  // Mark read/unread and refresh
  purgedUnread = alterUnread(purgedUnread, true);
  keptUnread = alterUnread(keptUnread, true);
  purgedRead = alterUnread(purgedRead, false);
  keptRead = alterUnread(keptRead, false);

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purgedUnread.getId()), "purgedUnread was kept");
  assertTrue(messageExists(keptUnread.getId()), "keptUnread was purged");
  assertFalse(messageExists(purgedRead.getId()), "purgedRead was kept");
  assertTrue(messageExists(keptRead.getId()), "keptRead was purged");
 }

 /**
  * Tests the user retention policy for the <tt>Sent</tt> folder.
  */
 @Test
 void purgeSent()
   throws Exception {
  // Set retention policy
  Account account = getAccount();
  account.setPrefSentLifetime("24h");

  // Insert messages
  String prefix = "purgeSent ";
  Mailbox mbox = getMailbox();
  Message purged = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_SENT, prefix + "purged",
    System.currentTimeMillis() - (25 * Constants.MILLIS_PER_HOUR));
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_SENT, prefix + "kept",
    System.currentTimeMillis() - (18 * Constants.MILLIS_PER_HOUR));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Confirms that a shorter user trash lifetime setting overrides the
  * system setting.
  */
 @Test
 void testTrashUser()
   throws Exception {
  Account account = getAccount();
  account.setMailPurgeUseChangeDateForTrash(false);
  account.setPrefTrashLifetime("24h");
  account.setMailTrashLifetime("48h");

  // Insert messages
  String prefix = "testTrashUser ";
  Mailbox mbox = getMailbox();
  Message purged = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_TRASH, prefix + "purged",
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_TRASH, prefix + "kept",
    System.currentTimeMillis() - (16 * Constants.MILLIS_PER_HOUR));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Confirms that a shorter system trash lifetime setting overrides the
  * user setting.
  */
 @Test
 void testTrashSystem()
   throws Exception {
  Account account = getAccount();
  account.setMailPurgeUseChangeDateForTrash(false);
  account.setPrefTrashLifetime("48h");
  account.setMailTrashLifetime("24h");

  // Insert messages
  String prefix = "testTrashSystem ";
  Mailbox mbox = getMailbox();
  Message purged = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_TRASH, prefix + "purged",
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_TRASH, prefix + "kept",
    System.currentTimeMillis() - (16 * Constants.MILLIS_PER_HOUR));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 @Test
 void purgeBatchSize()
   throws Exception {
  Account account = getAccount();
  account.setMailPurgeUseChangeDateForTrash(false);
  account.setPrefTrashLifetime("24h");

  // Insert messages
  String prefix = "purgeBatchSize ";
  Mailbox mbox = getMailbox();
  Message purged = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_TRASH, prefix + "purged",
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_TRASH, prefix + "kept",
    System.currentTimeMillis() - (35 * Constants.MILLIS_PER_HOUR));

  // Run purge and verify results
  TestUtil.setServerAttr(Provisioning.A_zimbraMailPurgeBatchSize, Integer.toString(1));
  assertFalse(mbox.purgeMessages(null));
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");

  // Run purge again and make sure that the second message was purged.
  TestUtil.setServerAttr(Provisioning.A_zimbraMailPurgeBatchSize, Integer.toString(2));
  assertTrue(mbox.purgeMessages(null));
  assertFalse(messageExists(kept.getId()), "second message was not purged");
 }

 /**
  * Confirms that a shorter user Junk lifetime setting overrides the
  * system setting.
  */
 @Test
 void testJunkUser()
   throws Exception {
  Account account = getAccount();
  account.setMailPurgeUseChangeDateForSpam(false);
  account.setPrefJunkLifetime("24h");
  account.setMailSpamLifetime("48h");

  // Insert messages
  String prefix = "testJunkUser ";
  Mailbox mbox = getMailbox();
  Message purged = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_SPAM, prefix + "purged",
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_SPAM, prefix + "kept",
    System.currentTimeMillis() - (16 * Constants.MILLIS_PER_HOUR));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Confirms that a shorter system Junk lifetime setting overrides the
  * user setting.
  */
 @Test
 void testJunkSystem()
   throws Exception {
  Account account = getAccount();
  account.setMailPurgeUseChangeDateForSpam(false);
  account.setPrefJunkLifetime("48h");
  account.setMailSpamLifetime("24h");

  // Insert messages
  String prefix = "testJunkUser ";
  Mailbox mbox = getMailbox();
  Message purged = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_SPAM, prefix + "purged",
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_SPAM, prefix + "kept",
    System.currentTimeMillis() - (16 * Constants.MILLIS_PER_HOUR));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Tests the user retention policy for all messages.
  */
 @Test
 void testAll()
   throws Exception {
  // Set retention policy
  Account account = getAccount();
  account.setMailMessageLifetime("40d");

  // Insert messages
  String prefix = "testAll ";
  Mailbox mbox = getMailbox();
  Folder folder = mbox.createFolder(null, "/testAll", new Folder.FolderOptions());
  Message purged = TestUtil.addMessage(mbox, folder.getId(), prefix + "purged",
    System.currentTimeMillis() - (41 * Constants.MILLIS_PER_DAY));
  Message kept = TestUtil.addMessage(mbox, folder.getId(), prefix + "kept",
    System.currentTimeMillis() - (39 * Constants.MILLIS_PER_DAY));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Tests the safeguard for the mailbox-wide message retention policy.
  */
 @Test
 void testAllSafeguard()
   throws Exception {
  // Set retention policy
  Account account = getAccount();
  account.setMailMessageLifetime("1h");

  // Insert messages
  String prefix = "testAllSafeguard ";
  Mailbox mbox = getMailbox();
  Folder folder = mbox.createFolder(null, "/testAllSafeguard", new Folder.FolderOptions());
  Message purged = TestUtil.addMessage(mbox, folder.getId(), prefix + "purged",
    System.currentTimeMillis() - (32 * Constants.MILLIS_PER_DAY));
  Message kept = TestUtil.addMessage(mbox, folder.getId(), prefix + "kept",
    System.currentTimeMillis() - (30 * Constants.MILLIS_PER_DAY));

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(purged.getId()), "purged was kept");
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Confirms that messages are purged from trash based on the value of
  * <tt>zimbraMailPurgeUseChangeDateForSpam<tt>.  See bug 19702 for more details.
  */
 @Test
 void testSpamChangeDate()
   throws Exception {
  Account account = getAccount();
  account.setPrefJunkLifetime("24h");
  account.setMailPurgeUseChangeDateForSpam(true);

  // Insert message
  String subject = "testSpamChangeDate";
  Mailbox mbox = getMailbox();
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, subject,
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  mbox.move(null, kept.getId(), MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_SPAM);

  // Validate dates
  long cutoff = System.currentTimeMillis() - Constants.MILLIS_PER_DAY;
  assertTrue(kept.getDate() < cutoff,
    "Unexpected message date: " + kept.getDate());
  assertTrue(kept.getChangeDate() > cutoff,
    "Unexpected change date: " + kept.getChangeDate());

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Confirms that messages are purged from trash based on the value of
  * <tt>zimbraMailPurgeUseChangeDateForTrash<tt>.  See bug 19702 for more details.
  */
 @Test
 void testTrashChangeDate()
   throws Exception {
  Account account = getAccount();
  account.setPrefTrashLifetime("24h");
  account.setMailPurgeUseChangeDateForTrash(true);

  // Insert message
  String subject = "testTrashChangeDate";
  Mailbox mbox = getMailbox();
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, subject,
    System.currentTimeMillis() - (36 * Constants.MILLIS_PER_HOUR));
  mbox.move(null, kept.getId(), MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_TRASH);

  // Validate dates
  long cutoff = System.currentTimeMillis() - Constants.MILLIS_PER_DAY;
  assertTrue(kept.getDate() < cutoff,
    "Unexpected message date: " + kept.getDate());
  assertTrue(kept.getChangeDate() > cutoff,
    "Unexpected change date: " + kept.getChangeDate());

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }

 /**
  * Confirms that empty folders in trash are purged (bug 16885).
  */
 @Test
 void testFolderInTrash()
   throws Exception {
  // Create a subfolder of trash with a message in it.
  Mailbox mbox = getMailbox();
  String folderPath = "/Trash/testFolderInTrash";
  Folder f = mbox.createFolder(null, folderPath, new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  String subject = "testFolderInTrash";
  Message msg = TestUtil.addMessage(mbox, f.getId(), subject, System.currentTimeMillis() - Constants.MILLIS_PER_DAY);

  // Set retention policy.
  Account account = getAccount();
  account.setPrefTrashLifetime("1ms");
  account.setMailPurgeUseChangeDateForTrash(false);

  mbox.purgeMessages(null);

  // Make sure both the message and folder were deleted.
  try {
   mbox.getMessageById(null, msg.getId());
   fail("Message " + msg.getId() + " was not deleted.");
  } catch (NoSuchItemException e) {
  }

  try {
   mbox.getFolderById(null, f.getId());
   fail("Folder " + f.getId() + " was not deleted.");
  } catch (NoSuchItemException e) {
  }
 }

 /**
  * Confirms that recently moved trash folders do not get purged when ChangeDate is true.
  */
 @Test
 void testRecentFolderInTrashChangeDate()
   throws Exception {

  Account account = getAccount();
  account.setPrefTrashLifetime("12h");
  account.setMailPurgeUseChangeDateForTrash(true);

  // Create a subfolder of inbox with a message in it.
  Mailbox mbox = getMailbox();
  String folderPath = "/Inbox/testRecentFolderInTrashChangeDate";
  Folder f = mbox.createFolder(null, folderPath, new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  String subject = "testRecentFolderInTrashChangeDate";
  Message msg = TestUtil.addMessage(mbox, f.getId(), subject, System.currentTimeMillis() - Constants.MILLIS_PER_DAY);

  // move the folder to trash
  mbox.move(null, f.getId(), MailItem.Type.FOLDER, Mailbox.ID_FOLDER_TRASH);

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertTrue(messageExists(msg.getId()), "msg was purged");
 }

 /**
  * Confirms that recently moved trash folders does get purged when ChangeDate is false.
  */
 @Test
 void testRecentFolderInTrashNoChangeDate()
   throws Exception {

  Account account = getAccount();
  account.setPrefTrashLifetime("12h");
  account.setMailPurgeUseChangeDateForTrash(false);

  // Create a subfolder of inbox with a message in it.
  Mailbox mbox = getMailbox();
  String folderPath = "/Inbox/testRecentFolderInTrashNoChangeDate";
  Folder f = mbox.createFolder(null, folderPath, new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  String subject = "testRecentFolderInTrashNoChangeDate";
  Message msg = TestUtil.addMessage(mbox, f.getId(), subject, System.currentTimeMillis() - Constants.MILLIS_PER_DAY);

  // move the folder to trash
  mbox.move(null, f.getId(), MailItem.Type.FOLDER, Mailbox.ID_FOLDER_TRASH);

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(msg.getId()), "msg was not purged");
 }

 @Test
 void invalidFolderMessageLifetime() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  Folder folder = mbox.createFolder(null, "/invalidFolderMessageLifetime", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  Policy p = Policy.newUserPolicy("45x");
  RetentionPolicy purgePolicy = new RetentionPolicy(Arrays.asList(p));
  try {
   mbox.setRetentionPolicy(null, folder.getId(), MailItem.Type.FOLDER, purgePolicy);
   fail("Invalid time interval should not have been accepted.");
  } catch (ServiceException e) {
  }
 }

 @Test
 void multipleUserPolicy() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  Folder folder = mbox.createFolder(null, "/multipleUserPolicy", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));

  List<Policy> list = Arrays.asList(
    Policy.newUserPolicy("45d"),
    Policy.newUserPolicy("60d"));
  RetentionPolicy purgePolicy = new RetentionPolicy(list);
  try {
   mbox.setRetentionPolicy(null, folder.getId(), MailItem.Type.FOLDER, purgePolicy);
   fail("Multiple purge policies.");
  } catch (ServiceException e) {
  }

  purgePolicy = new RetentionPolicy(list);
  try {
   mbox.setRetentionPolicy(null, folder.getId(), MailItem.Type.FOLDER, purgePolicy);
   fail("Multiple keep policies.");
  } catch (ServiceException e) {
  }
 }

 @Test
 void modifySystemPolicy() throws Exception {
  Config config = Provisioning.getInstance().getConfig();
  RetentionPolicyManager mgr = RetentionPolicyManager.getInstance();
  Policy purge1 = mgr.createSystemPurgePolicy(config, "purge1", "500d");
  Policy purge2 = mgr.createSystemPurgePolicy(config, "purge2", "500d");

  assertEquals(purge1, mgr.getPolicyById(config, purge1.getId()));
  assertEquals(purge2, mgr.getPolicyById(config, purge2.getId()));

  // Test modify.
  mgr.modifySystemPolicy(config, purge1.getId(), "new purge1", "501d");
  Policy newPurge1 = mgr.getPolicyById(config, purge1.getId());
  assertNotEquals(purge1, newPurge1);
  assertEquals(purge1.getId(), newPurge1.getId());
  assertEquals("new purge1", newPurge1.getName());
  assertEquals("501d", newPurge1.getLifetime());

  // Test delete.
  assertTrue(mgr.deleteSystemPolicy(config, purge2.getId()));
  assertNull(mgr.getPolicyById(config, purge2.getId()));
  RetentionPolicy rp = mgr.getSystemRetentionPolicy(config);
  assertEquals(1, rp.getPurgePolicy().size());
 }

 @Test
 void modifyCosSystemPolicy() throws Exception {
  Map<String, Object> attrs = new HashMap<String, Object>();
  Cos cos = Provisioning.getInstance().createCos("testcos", attrs);
  RetentionPolicyManager mgr = RetentionPolicyManager.getInstance();

  Policy purge1 = mgr.createSystemPurgePolicy(cos, "purge1", "500d");
  Policy purge2 = mgr.createSystemPurgePolicy(cos, "purge2", "500d");

  assertEquals(purge1, mgr.getPolicyById(cos, purge1.getId()));
  assertEquals(purge2, mgr.getPolicyById(cos, purge2.getId()));

  // Test modify.
  mgr.modifySystemPolicy(cos, purge1.getId(), "new purge1", "301d");
  Policy newKeep1 = mgr.getPolicyById(cos, purge1.getId());
  assertNotEquals(purge1, newKeep1);
  assertEquals(purge1.getId(), newKeep1.getId());
  assertEquals("new purge1", newKeep1.getName());
  assertEquals("301d", newKeep1.getLifetime());

  // Test delete.
  assertTrue(mgr.deleteSystemPolicy(cos, purge2.getId()));
  assertNull(mgr.getPolicyById(cos, purge2.getId()));
  RetentionPolicy rp = mgr.getSystemRetentionPolicy(cos);
  assertEquals(1, rp.getPurgePolicy().size());
 }

 /**
  * Tests {@link RetentionPolicyManager#getCompleteRetentionPolicy(Account, RetentionPolicy).  Confirms
  * that system policy elements are updated with the latest values in LDAP.
  */
 @Test
 void completeRetentionPolicy() throws Exception {
  RetentionPolicyManager mgr = RetentionPolicyManager.getInstance();
  Config config = Provisioning.getInstance().getConfig();
  Policy purge1 = mgr.createSystemPurgePolicy(config, "purge1", "300d");

  // Create mailbox policy that references the system policy, and confirm that
  // lookup returns the latest values.
  RetentionPolicy mboxRP = new RetentionPolicy(Arrays.asList(Policy.newSystemPolicy(purge1.getId())));
  RetentionPolicy completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP);
  Policy latest = completeRP.getPurgePolicy().get(0);
  assertEquals(purge1, latest);

  // Modify system policy and confirm that the accessor returns the latest values.
  mgr.modifySystemPolicy(config, purge1.getId(), "new purge1", "301d");
  completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP);
  latest = completeRP.getPurgePolicy().get(0);
  assertNotEquals(purge1, latest);
  assertEquals(purge1.getId(), latest.getId());
  assertEquals("new purge1", latest.getName());
  assertEquals("301d", latest.getLifetime());
 }

 @Test
 void completeCosRetentionPolicy() throws Exception {
  RetentionPolicyManager mgr = RetentionPolicyManager.getInstance();
  Config config = Provisioning.getInstance().getConfig();
  Policy purge1 = mgr.createSystemPurgePolicy(config, "purge1", "300d");


  Map<String, Object> attrs = new HashMap<String, Object>();
  Cos cos = Provisioning.getInstance().createCos("testcos2", attrs);
  Policy purge2 = mgr.createSystemPurgePolicy(cos, "purge2", "600d");

  // Assign cos to the account
  getAccount().setCOSId(cos.getId());

  // Create mailbox policy that references the system policy, and confirm that
  // lookup returns the latest values.
  RetentionPolicy mboxRP1 = new RetentionPolicy(Arrays.asList(Policy.newSystemPolicy(purge2.getId())));
  RetentionPolicy completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP1);
  Policy latest = completeRP.getPurgePolicy().get(0);
  assertEquals(purge2, latest);

  // Modify cos policy and confirm that the accessor returns the latest values.
  mgr.modifySystemPolicy(cos, purge2.getId(), "new purge2", "301d");
  completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP1);
  latest = completeRP.getPurgePolicy().get(0);
  assertNotEquals(purge2, latest);
  assertEquals(purge2.getId(), latest.getId());
  assertEquals("new purge2", latest.getName());
  assertEquals("301d", latest.getLifetime());

  // Make sure system policy does not apply to this user.
  RetentionPolicy mboxRP2 = new RetentionPolicy(Arrays.asList(Policy.newSystemPolicy(purge1.getId())));
  completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP2);
  assertTrue(completeRP.getPurgePolicy().isEmpty());

  // remove cos retention policy
  mgr.deleteSystemPolicy(cos, purge2.getId());

  // make sure account retention policy is empty
  completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP1);
  assertTrue(completeRP.getPurgePolicy().isEmpty());

  // make sure system policy is applicable now
  completeRP = mgr.getCompleteRetentionPolicy(getAccount(), mboxRP2);
  latest = completeRP.getPurgePolicy().get(0);
  assertEquals(purge1, latest);
  assertEquals(purge1.getId(), latest.getId());
  assertEquals("purge1", latest.getName());
  assertEquals("300d", latest.getLifetime());
 }

 @Test
 void purgeWithSystemPolicy() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // Create folder and test messages.
  Folder folder = mbox.createFolder(null, "/purgeWithSystemPolicy", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  Message older = TestUtil.addMessage(mbox, folder.getId(), "older", System.currentTimeMillis() - (60 * Constants.MILLIS_PER_MINUTE));
  Message newer = TestUtil.addMessage(mbox, folder.getId(), "newer", System.currentTimeMillis() - (30 * Constants.MILLIS_PER_MINUTE));
  folder = mbox.getFolderById(null, folder.getId());

  // Add user and system retention policy.
  Config config = Provisioning.getInstance().getConfig();
  Policy system = RetentionPolicyManager.getInstance().createSystemPurgePolicy(config, "system", "45m");

  Policy p1 = Policy.newUserPolicy("90m");
  Policy p2 = Policy.newSystemPolicy(system.getId());
  RetentionPolicy purgePolicy = new RetentionPolicy(Arrays.asList(p1, p2));
  mbox.setRetentionPolicy(null, folder.getId(), MailItem.Type.FOLDER, purgePolicy);

  // Run purge and make sure one of the messages was deleted.
  mbox.purgeMessages(null);
  folder = mbox.getFolderById(folder.getId());
  assertEquals(1, folder.getSize());
  mbox.getMessageById(null, newer.getId());
  try {
   mbox.getMessageById(null, older.getId());
   fail("Older message was not purged.");
  } catch (NoSuchItemException e) {
  }

  // Update system policy, rerun purge, and make sure the older message was deleted.
  RetentionPolicyManager.getInstance().modifySystemPolicy(config, system.getId(), system.getName(), "20m");
  mbox.purgeMessages(null);
  folder = mbox.getFolderById(folder.getId());
  assertEquals(0, folder.getSize());
  try {
   mbox.getMessageById(null, newer.getId());
   fail("Newer message was not purged.");
  } catch (NoSuchItemException e) {
  }
 }

    private Message alterUnread(Message msg, boolean unread) throws Exception {
        Mailbox mbox = getMailbox();
        mbox.alterTag(null, msg.getId(), msg.getType(), Flag.FlagInfo.UNREAD, unread, null);
        return mbox.getMessageById(null, msg.getId());
    }

    private boolean messageExists(int id)
    throws Exception {
        Mailbox mbox = getMailbox();
        try {
            mbox.getMessageById(null, id);
        } catch (ServiceException e) {
            assertTrue(e instanceof NoSuchItemException, "Unexpected exception type: " + e);
            return false;
        }
        return true;
    }

    private Account getAccount()
    throws ServiceException {
        return Provisioning.getInstance().get(AccountBy.id, MockProvisioning.DEFAULT_ACCOUNT_ID);
    }

    private Mailbox getMailbox() throws ServiceException {
        return MailboxManager.getInstance().getMailboxByAccount(getAccount());
    }

 /**
  * Confirms that IMAP deleted messages are purged based on the value of
  * <tt>zimbraMailTrashLifetime<tt>.  See bug 74953 for more details.
  */
 @Test
 void testExpiredIMAPDeletedOnChangeDate()
   throws Exception {
  Account account = getAccount();
  account.setMailTrashLifetime("24h");

  // Insert message
  String subject = "testNotExpiredIMAPDeletedOnChangeDate";
  Mailbox mbox = getMailbox();
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, subject, System.currentTimeMillis() - Constants.MILLIS_PER_MONTH);
  long changeDate = (System.currentTimeMillis() - (Constants.MILLIS_PER_DAY * 2)) / 1000;
  TestUtil.updateMailItemChangeDateAndFlag(mbox, kept.getId(), changeDate, Flag.BITMASK_DELETED);

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertFalse(messageExists(kept.getId()), "kept was not purged");
 }

 /**
  * Confirms that IMAP deleted messages are not purged based on the value of
  * <tt>zimbraMailTrashLifetime<tt>.  See bug 74953 for more details.
  */
 @Test
 void testNotExpiredIMAPDeletedOnChangeDate()
   throws Exception {
  Account account = getAccount();
  account.setMailTrashLifetime("24h");

  // Insert message
  String subject = "testNotExpiredIMAPDeletedOnChangeDate";
  Mailbox mbox = getMailbox();
  Message kept = TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_INBOX, subject, System.currentTimeMillis() - Constants.MILLIS_PER_MONTH);
  long changeDate = (System.currentTimeMillis() - (Constants.MILLIS_PER_HOUR * 5)) / 1000;
  TestUtil.updateMailItemChangeDateAndFlag(mbox, kept.getId(), changeDate, Flag.BITMASK_DELETED);

  // Run purge and verify results
  mbox.purgeMessages(null);
  assertTrue(messageExists(kept.getId()), "kept was purged");
 }
}
