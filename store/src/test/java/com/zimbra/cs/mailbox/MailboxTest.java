// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning.MailThreadingAlgorithm;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.mime.InternetAddress;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.BrowseTerm;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.session.PendingLocalModifications;
import com.zimbra.cs.session.PendingModifications;
import com.zimbra.cs.session.PendingModifications.ModificationKey;
import com.zimbra.cs.store.MockStoreManager;
import com.zimbra.cs.store.StoreManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.*;

/**
 * Unit test for {@link Mailbox}.
 *
 * @author ysasaki
 */
public final class MailboxTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() throws Exception {
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @AfterEach
  public void cleanUp() throws Exception {
    Mailbox mbox = null;
    try {
      mbox =
          MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
      if (mbox != null) {
        // Keeping these for exercising this code, even though deleting the account
        MailboxTestUtil.clearData();
        MailboxTestUtil.cleanupIndexStore(
            MailboxManager.getInstance()
                .getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID));
        Provisioning prov = Provisioning.getInstance();
        prov.deleteAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
      }
    } catch (Exception ex) {
    }
  }

  public static final DeliveryOptions STANDARD_DELIVERY_OPTIONS =
      new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);

 @Test
 void browse() throws Exception {
  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(
    null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test1-2@sub1.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test1-3@sub1.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test1-4@sub1.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test2-1@sub2.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test2-2@sub2.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test2-3@sub2.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test3-1@sub3.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test3-2@sub3.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test4-1@sub4.zimbra.com".getBytes(), false), dopt, null);
  mbox.index.indexDeferredItems();

  List<BrowseTerm> terms = mbox.browse(null, Mailbox.BrowseBy.domains, null, 100);
  assertEquals("sub1.zimbra.com", terms.get(0).getText());
  assertEquals("sub2.zimbra.com", terms.get(1).getText());
  assertEquals("sub3.zimbra.com", terms.get(2).getText());
  assertEquals("sub4.zimbra.com", terms.get(3).getText());
  assertEquals(4, terms.size(), "Number of expected terms");
  assertEquals(8, terms.get(0).getFreq());
  assertEquals(6, terms.get(1).getFreq());
  assertEquals(4, terms.get(2).getFreq());
  assertEquals(2, terms.get(3).getFreq());
 }

 @Test
 void testRecentMessageCount() throws Exception {
  Account acct1 =
    Provisioning.getInstance().get(Key.AccountBy.id, MockProvisioning.DEFAULT_ACCOUNT_ID);
  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertEquals(
    0,
    mbox.getRecentMessageCount(),
    "recent message count should be 0 before adding a message");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(
    null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);
  assertEquals(
    1,
    mbox.getRecentMessageCount(),
    "recent message count should be 1 after adding one message");
  mbox.resetRecentMessageCount(new OperationContext(acct1));
  assertEquals(
    0, mbox.getRecentMessageCount(), "recent message count should be 0 after reset");
  mbox.addMessage(
    null, new ParsedMessage("From: test1-2@sub1.zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(
    null, new ParsedMessage("From: test1-3@sub1.zimbra.com".getBytes(), false), dopt, null);
  assertEquals(
    2,
    mbox.getRecentMessageCount(),
    "recent message count should be 2 after adding two messages");
  mbox.resetRecentMessageCount(new OperationContext(acct1));
  assertEquals(
    0, mbox.getRecentMessageCount(), "recent message count should be 0 after the second reset");
 }

 @Test
 void threadDraft() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("test@zimbra.com");
  acct.setMailThreadingAlgorithm(MailThreadingAlgorithm.subject);

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // setup: add the root message
  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject");
  int rootId = mbox.addMessage(null, pm, STANDARD_DELIVERY_OPTIONS, null).getId();

  // first draft explicitly references the parent by item ID (how ZWC does it)
  pm = MailboxTestUtil.generateMessage("Re: test subject");
  Message draft =
    mbox.saveDraft(
      null,
      pm,
      Mailbox.ID_AUTO_INCREMENT,
      rootId + "",
      MailSender.MSGTYPE_REPLY,
      null,
      null,
      0);
  Message parent = mbox.getMessageById(null, rootId);
  assertEquals(
    parent.getConversationId(), draft.getConversationId(), "threaded explicitly");

  // second draft implicitly references the parent by default threading rules
  pm = MailboxTestUtil.generateMessage("Re: test subject");
  draft = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT);
  parent = mbox.getMessageById(null, rootId);
  assertEquals(
    parent.getConversationId(), draft.getConversationId(), "threaded implicitly [saveDraft]");

  // threading is set up at first save time, so modifying the second draft should *not* affect
  // threading
  pm = MailboxTestUtil.generateMessage("Re: changed the subject");
  draft = mbox.saveDraft(null, pm, draft.getId());
  parent = mbox.getMessageById(null, rootId);
  assertEquals(
    parent.getConversationId(), draft.getConversationId(), "threaded implicitly [resaved]");

  // third draft is like second draft, but goes via Mailbox.addMessage (how IMAP does it)
  pm = MailboxTestUtil.generateMessage("Re: test subject");
  DeliveryOptions dopt =
    new DeliveryOptions().setFlags(Flag.BITMASK_DRAFT).setFolderId(Mailbox.ID_FOLDER_DRAFTS);
  draft = mbox.addMessage(null, pm, dopt, null);
  parent = mbox.getMessageById(null, rootId);
  assertEquals(
    parent.getConversationId(), draft.getConversationId(), "threaded implicitly [addMessage]");

  // fourth draft explicitly references the parent by item ID, even though it wouldn't get
  // threaded using the default threader
  pm = MailboxTestUtil.generateMessage("changed the subject");
  draft =
    mbox.saveDraft(
      null,
      pm,
      Mailbox.ID_AUTO_INCREMENT,
      rootId + "",
      MailSender.MSGTYPE_REPLY,
      null,
      null,
      0);
  parent = mbox.getMessageById(null, rootId);
  assertEquals(
    parent.getConversationId(),
    draft.getConversationId(),
    "threaded explicitly (changed subject)");

  // fifth draft is not related to the parent and should not be threaded
  pm = MailboxTestUtil.generateMessage("Re: unrelated subject");
  draft = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT);
  assertEquals(-draft.getId(), draft.getConversationId(), "unrelated");
 }

 @Test
 void trimTombstones() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // add a message
  int changeId1 = mbox.getLastChangeID();
  int msgId =
    mbox.addMessage(
      null, MailboxTestUtil.generateMessage("foo"), STANDARD_DELIVERY_OPTIONS, null)
      .getId();

  // turn on sync tracking -- tombstone table should be empty
  mbox.beginTrackingSync();
  int changeId2 = mbox.getLastChangeID();
  assertTrue(mbox.getTombstones(changeId2).isEmpty(), "no changes");

  // verify that we can't use a sync token from *before* sync tracking was enabled
  try {
   mbox.getTombstones(changeId1);
   fail("too-early sync token");
  } catch (MailServiceException e) {
   assertEquals(e.getCode(), MailServiceException.MUST_RESYNC, "too-early sync token");
  }

  // delete the message and check that it generated a tombstone
  mbox.delete(null, msgId, MailItem.Type.MESSAGE);
  int changeId3 = mbox.getLastChangeID();
  assertTrue(mbox.getTombstones(changeId2).contains(msgId), "deleted item in tombstones");
  assertTrue(mbox.getTombstones(changeId3).isEmpty(), "no changes since delete");

  // purge the account with the default tombstone purge lifetime (3 months)
  mbox.purgeMessages(null);
  assertTrue(
    mbox.getTombstones(changeId2).contains(msgId), "deleted item still in tombstones");

  // purge the account and all its tombstones
  LC.tombstone_max_age_ms.setDefault(0);
  mbox.purgeMessages(null);
  try {
   mbox.getTombstones(changeId2);
   fail("sync token predates purged tombstone");
  } catch (MailServiceException e) {
   assertEquals(
     e.getCode(), MailServiceException.MUST_RESYNC, "sync token predates purged tombstone");
  }
  assertTrue(
    mbox.getTombstones(changeId3).isEmpty(), "sync token matches last purged tombstone");
 }

 @Test
 void test_markMailboxDeleted_deletes_account_when_account_exists() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("test@zimbra.com");
  MailboxManager mailboxManager = MailboxManager.getInstance();
  Mailbox mbox = mailboxManager.getMailboxByAccount(acct);
  mbox.markMailboxDeleted();
  AccountServiceException thrown = assertThrows(
          AccountServiceException.class,
          () -> mailboxManager.getMailboxByAccountId(acct.getId())
  );
  assertEquals(AccountServiceException.NO_SUCH_ACCOUNT, thrown.getCode());
 }

 @Test
 void test_markMailboxDeleted_is_ok_when_account_does_not_exists() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("test@zimbra.com");
  MailboxManager mailboxManager = MailboxManager.getInstance();
  Mailbox mbox = mailboxManager.getMailboxByAccount(acct);
  mbox.markMailboxDeleted();
  mbox.markMailboxDeleted();
  AccountServiceException thrown = assertThrows(
          AccountServiceException.class,
          () -> mailboxManager.getMailboxByAccountId(acct.getId())
  );
  assertEquals(AccountServiceException.NO_SUCH_ACCOUNT, thrown.getCode());
 }

  static class MockListener extends MailboxListener {
    /**
     * Information on creations/modifications and deletions seen since {@link clear} was last called
     * (or listener was instantiated)
     */
    PendingLocalModifications pms;

    @Override
    public void notify(ChangeNotification notification) {
      PendingLocalModifications newPms = notification.mods;

      if (this.pms == null) {
        this.pms = newPms;
      } else {
        if (newPms.created != null) {
          if (pms.created == null) {
            pms.created = Maps.newLinkedHashMap();
          }
          pms.created.putAll(newPms.created);
        }
        if (newPms.modified != null) {
          if (pms.modified == null) {
            pms.modified = Maps.newHashMap();
          }
          pms.modified.putAll(newPms.modified);
        }
        if (newPms.deleted != null) {
          if (pms.deleted == null) {
            pms.deleted = Maps.newHashMap();
          }
          pms.deleted.putAll(newPms.deleted);
        }
      }
    }

    public PendingLocalModifications getPms() {
      return pms;
    }

    /** Regard all previously items seen during listening as processed. */
    public void clear() {
      pms = null;
    }
  }

 @Test
 void notifications() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  MockListener ml = new MockListener();
  MailboxListener.register(ml);

  try {
   Folder f =
     mbox.createFolder(
       null, "foo", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
   Folder fParent = (Folder) f.getParent();

   ModificationKey fkey = new ModificationKey(f);
   ModificationKey fParentKey = new ModificationKey(fParent);

   assertNull(ml.getPms().deleted, "no deletes after create");

   assertNotNull(ml.getPms().created, "creates aren't null");
   assertEquals(1, ml.getPms().created.size(), "one created folder");
   assertNotNull(ml.getPms().created.get(fkey), "created folder has entry");
   assertEquals(
     f.getId(),
     ml.getPms().created.get(fkey).getIdInMailbox(),
     "created folder matches created entry");

   assertNotNull(ml.getPms().modified, "modifications aren't null");
   assertEquals(1, ml.getPms().modified.size(), "one modified folder");
   PendingModifications.Change pModification = ml.getPms().modified.get(fParentKey);
   assertNotNull(pModification, "parent folder modified");
   assertEquals(
     fParent.getId(),
     ((Folder) pModification.what).getId(),
     "parent folder matches modified entry");
   assertNotNull(pModification.preModifyObj, "preModifyObj is not null");
   assertEquals(
     fParent.getId(),
     ((Folder) pModification.preModifyObj).getId(),
     "preModifyObj is a snapshot of parent folder");

   DeliveryOptions dopt = new DeliveryOptions().setFolderId(f.getId());
   Message m =
     mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
   ModificationKey mkey = new ModificationKey(m);

   ml.clear();
   mbox.delete(null, f.getId(), MailItem.Type.FOLDER);

   assertNull(ml.getPms().created, "no creates after delete");

   assertNotNull(ml.getPms().deleted, "deletes aren't null");
   assertEquals(
     3, ml.getPms().deleted.size(), "1 deleted folder, 1 deleted message, 1 deleted vconv");
   PendingModifications.Change fDeletion = ml.getPms().deleted.get(fkey);
   assertNotNull(fDeletion, "deleted folder has entry");
   assertTrue(
     f.getType() == fDeletion.what && f.getId() == ((Folder) fDeletion.preModifyObj).getId(),
     "deleted folder matches deleted entry");
   PendingModifications.Change mDeletion = ml.getPms().deleted.get(mkey);
   assertNotNull(mDeletion, "deleted message has entry");
   // Note that preModifyObj may be null for the deleted message, so just check for the type
   assertEquals(m.getType(), mDeletion.what, "deleted message matches deleted entry");

   assertNotNull(ml.getPms().modified, "modifications aren't null");
   // Bug 80980 "folder size modified" notification present because folder delete is now a 2
   // stage operation.
   // Empty folder, then delete it.
   assertEquals(
     3,
     ml.getPms().modified.size(),
     "parent folder modified, mailbox size modified, folder size modified");
   pModification = ml.getPms().modified.get(fParentKey);
   assertNotNull(pModification, "parent folder modified");
   assertEquals(
     fParent.getId(),
     ((Folder) pModification.what).getId(),
     "parent folder matches modified entry");
   assertNotNull(pModification.preModifyObj, "preModifyObj is not null");
   assertEquals(
     fParent.getId(),
     ((Folder) pModification.preModifyObj).getId(),
     "preModifyObj is a snapshot of parent folder");
  } finally {
   MailboxListener.unregister(ml);
  }
 }

 @Test
 void dumpster() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("test@zimbra.com");
  acct.setDumpsterEnabled(true);

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  int msgId =
    mbox.addMessage(
      null, MailboxTestUtil.generateMessage("test"), STANDARD_DELIVERY_OPTIONS, null)
      .getId();

  mbox.index.indexDeferredItems();

  mbox.delete(null, msgId, MailItem.Type.MESSAGE);
  mbox.recover(null, new int[]{msgId}, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX);
 }

 @Test
 void deleteMailbox() throws Exception {
  MockStoreManager sm = (MockStoreManager) StoreManager.getInstance();

  // first test normal mailbox delete
  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertEquals(0, sm.size(), "start with no blobs in the store");

  MailItem item =
    mbox.addMessage(
      null, MailboxTestUtil.generateMessage("test"), STANDARD_DELIVERY_OPTIONS, null);
  assertEquals(1, sm.size(), "1 blob in the store");
  // Index the mailbox so that mime message gets cached
  mbox.index.indexDeferredItems();
  // make sure digest is in message cache.
  assertTrue(MessageCache.contains(item.getDigest()));

  mbox.deleteMailbox();
  assertEquals(0, sm.size(), "end with no blobs in the store");
  // make sure digest is removed from message cache.
  assertFalse(MessageCache.contains(item.getDigest()));

  // then test mailbox delete without store delete
  mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertEquals(0, sm.size(), "start with no blobs in the store");

  item =
    mbox.addMessage(
      null, MailboxTestUtil.generateMessage("test"), STANDARD_DELIVERY_OPTIONS, null);
  assertEquals(1, sm.size(), "1 blob in the store");

  // Index the mailbox so that mime message gets cached
  mbox.index.indexDeferredItems();
  // make sure digest is in message cache.
  assertTrue(MessageCache.contains(item.getDigest()));

  mbox.deleteMailbox(Mailbox.DeleteBlobs.NEVER);
  assertEquals(1, sm.size(), "end with 1 blob in the store");
  // make sure digest is still present in message cache.
  assertTrue(MessageCache.contains(item.getDigest()));
  sm.purge();

  // then do it contingent on whether the store is centralized or local
  mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertEquals(0, sm.size(), "start with no blobs in the store");

  mbox.addMessage(null, MailboxTestUtil.generateMessage("test"), STANDARD_DELIVERY_OPTIONS, null)
    .getId();
  assertEquals(1, sm.size(), "1 blob in the store");

  mbox.deleteMailbox(Mailbox.DeleteBlobs.UNLESS_CENTRALIZED);
  int expected =
    StoreManager.getInstance().supports(StoreManager.StoreFeature.CENTRALIZED) ? 1 : 0;
  assertEquals(expected, sm.size(), "end with " + expected + " blob(s) in the store");
  sm.purge();
 }

 @Test
 void muted() throws Exception {
  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // root message
  DeliveryOptions dopt =
    new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  Message msg =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  assertTrue(msg.isUnread(), "root unread");
  assertFalse(msg.isTagged(Flag.FlagInfo.MUTED), "root not muted");
  assertTrue(msg.getConversationId() < 0, "root in virtual conv");

  // mark root muted
  mbox.alterTag(null, msg.getId(), MailItem.Type.MESSAGE, Flag.FlagInfo.MUTED, true, null);
  msg = mbox.getMessageById(null, msg.getId());
  assertTrue(msg.isUnread(), "root unread");
  assertTrue(msg.isTagged(Flag.FlagInfo.MUTED), "root muted");
  assertTrue(msg.getConversationId() < 0, "root in virtual conv");
  assertTrue(
    mbox.getConversationById(null, msg.getConversationId()).isTagged(Flag.FlagInfo.MUTED),
    "virtual conv muted");

  // add a reply to the muted virtual conversation
  dopt.setConversationId(msg.getConversationId());
  Message msg2 =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject"), dopt, null);
  assertFalse(msg2.isUnread(), "reply read");
  assertTrue(msg2.isTagged(Flag.FlagInfo.MUTED), "reply muted");
  assertFalse(msg2.getConversationId() < 0, "reply in real conv");
  assertTrue(
    mbox.getConversationById(null, msg2.getConversationId()).isTagged(Flag.FlagInfo.MUTED),
    "real conversation muted");

  // add another reply to the now-real still-muted conversation
  dopt.setConversationId(msg2.getConversationId());
  Message msg3 =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject"), dopt, null);
  assertFalse(msg3.isUnread(), "second reply read");
  assertTrue(msg3.isTagged(Flag.FlagInfo.MUTED), "second reply muted");
  assertFalse(msg3.getConversationId() < 0, "second reply in real conv");
  assertTrue(
    mbox.getConversationById(null, msg3.getConversationId()).isTagged(Flag.FlagInfo.MUTED),
    "real conversation muted");

  // unmute conversation
  mbox.alterTag(
    null,
    msg3.getConversationId(),
    MailItem.Type.CONVERSATION,
    Flag.FlagInfo.MUTED,
    false,
    null);
  msg3 = mbox.getMessageById(null, msg3.getId());
  assertFalse(msg3.isTagged(Flag.FlagInfo.MUTED), "second reply not muted");
  assertFalse(
    mbox.getConversationById(null, msg3.getConversationId()).isTagged(Flag.FlagInfo.MUTED),
    "real conversation not muted");

  // add a last reply to the now-unmuted conversation
  Message msg4 =
    mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject"), dopt, null);
  assertTrue(msg4.isUnread(), "third reply unread");
  assertFalse(msg4.isTagged(Flag.FlagInfo.MUTED), "third reply not muted");
  assertFalse(msg4.getConversationId() < 0, "third reply in real conv");
  assertFalse(
    mbox.getConversationById(null, msg4.getConversationId()).isTagged(Flag.FlagInfo.MUTED),
    "real conversation not muted");
 }

 @Test
 void createAutoContactTestWhenMaxEntriesLimitIsReached() throws Exception {

  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  Collection<InternetAddress> addrs = new ArrayList<InternetAddress>();
  addrs.add(new InternetAddress("user2@email.com"));
  addrs.add(new InternetAddress("user3@email.com"));
  addrs.add(new InternetAddress("user4@email.com"));

  Provisioning prov = Provisioning.getInstance();
  Account acct1 =
    Provisioning.getInstance().get(Key.AccountBy.id, MockProvisioning.DEFAULT_ACCOUNT_ID);
  Map<String, Object> attrs = new HashMap<String, Object>();
  attrs.put(Provisioning.A_zimbraContactMaxNumEntries, Integer.toString(2));
  prov.modifyAttrs(acct1, attrs);
  List<Contact> contactList = mbox.createAutoContact(null, addrs);
  assertEquals(2, contactList.size());

  attrs.put(Provisioning.A_zimbraContactMaxNumEntries, Integer.toString(10));
  prov.modifyAttrs(acct1, attrs);
  addrs = new ArrayList<InternetAddress>();
  addrs.add(new InternetAddress("user2@email.com"));
  addrs.add(new InternetAddress("user3@email.com"));
  addrs.add(new InternetAddress("user4@email.com"));
  contactList = mbox.createAutoContact(null, addrs);
  assertEquals(3, contactList.size());
 }

 @Test
 void createAutoContactTestForDisplayNameFormat() throws Exception {
  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  Account acct1 =
    Provisioning.getInstance().get(Key.AccountBy.id, MockProvisioning.DEFAULT_ACCOUNT_ID);

  Collection<InternetAddress> addrs = new ArrayList<InternetAddress>();
  addrs.add(new InternetAddress("\"First Last\" <user@email.com>"));
  List<Contact> contactList = mbox.createAutoContact(null, addrs);
  Contact contact = contactList.get(0);
  assertEquals("First", contact.get("firstName"));
  assertEquals("Last", contact.get("lastName"));

  addrs = new ArrayList<InternetAddress>();
  addrs.add(new InternetAddress("\"Last First\" <user@email.com>"));
  acct1.setPrefLocale("ja");
  ;
  contactList = mbox.createAutoContact(new OperationContext(acct1), addrs);
  contact = contactList.get(0);
  assertEquals("First", contact.get("firstName"));
  assertEquals("Last", contact.get("lastName"));
 }

 @Test
 void getVisibleFolders() throws Exception {
  Mailbox mbox =
    MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  mbox.getVisibleFolders(new OperationContext(mbox));
 }

 @Test
 void testLocalMsgReadStatusForForMailForwards() throws Exception {
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = new HashMap<String, Object>();
  attrs.put(Provisioning.A_zimbraFeatureMailForwardingEnabled, "TRUE");
  attrs.put(Provisioning.A_zimbraPrefMailForwardingAddress, "user@zimbra.com");
  attrs.put(Provisioning.A_zimbraFeatureMarkMailForwardedAsRead, "TRUE");
  Account acct = prov.createAccount("user@zimbra.com", "secret", attrs);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(acct.getId());

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  dopt.setFlags(Flag.BITMASK_UNREAD);
  Message message =
    mbox.addMessage(
      null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);
  assertEquals(false, message.isUnread());
 }

 @Test
 void testLocalMsgReadStatusForForMailForwardsWhenMarkAsReadIsFalse() throws Exception {
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = new HashMap<String, Object>();
  attrs.put(Provisioning.A_zimbraFeatureMailForwardingEnabled, "TRUE");
  attrs.put(Provisioning.A_zimbraPrefMailForwardingAddress, "user2@zimbra.com");
  attrs.put(Provisioning.A_zimbraFeatureMarkMailForwardedAsRead, "FALSE");
  Account acct = prov.createAccount("user@zimbra.com", "secret", attrs);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(acct.getId());

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  dopt.setFlags(Flag.BITMASK_UNREAD);
  Message message =
    mbox.addMessage(
      null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);
  assertEquals(true, message.isUnread());
 }

 @Test
 void testGetModifiedItemsCount() throws Exception {
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = new HashMap<String, Object>();
  Account acct = prov.createAccount("testGetModifiedItemsCount@zimbra.com", "secret", attrs);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(acct.getId());

  Map<String, Object> fields = new HashMap<String, Object>();
  fields.put(ContactConstants.A_firstName, "First1");
  fields.put(ContactConstants.A_lastName, "Last1");
  mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);
  fields.put(ContactConstants.A_firstName, "First2");
  fields.put(ContactConstants.A_lastName, "Last2");
  mbox.createContact(null, new ParsedContact(fields), Mailbox.ID_FOLDER_CONTACTS, null);

  Set<Integer> folderIds = new HashSet<Integer>();
  folderIds.add(Mailbox.ID_FOLDER_CONTACTS);
  OperationContext octxt = new OperationContext(acct);
  int count = mbox.getModifiedItemsCount(octxt, 0, 0, MailItem.Type.CONTACT, folderIds);
  assertEquals(2, count);
 }

 @Test
 @Disabled("Fix me. Assertions fails. Standard error: missing .platform")
 void testAdditionalQuotaProviderExceedsQuota() throws Exception {
  AdditionalQuotaProvider additionalQuotaProvider =
    new AdditionalQuotaProvider() {
     @Override
     public long getAdditionalQuota(Mailbox mailbox) {
      return 10;
     }
    };
  MailboxManager.getInstance().addAdditionalQuotaProvider(additionalQuotaProvider);
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = new HashMap<String, Object>();
  attrs.put("zimbraMailQuota", "5");
  Account acct = prov.createAccount("testAdditionalQuotaProvider@zimbra.com", "secret", attrs);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
  try {
   mbox.checkSizeChange(0);
   fail("Expected QUOTA_EXCEEDED exception");
  } catch (MailServiceException ignored) {
  }

  assertEquals(10L, mbox.getSize());

  MailboxManager.getInstance().removeAdditionalQuotaProvider(additionalQuotaProvider);
  try {
   mbox.checkSizeChange(5);
  } catch (MailServiceException ignored) {
   fail("Unexpected QUOTA_EXCEEDED exception");
  }

  assertEquals(0L, mbox.getSize());
 }

 @Test
 void testAdditionalQuotaProviderRespectsQuota() throws Exception {
  MailboxManager.getInstance()
    .addAdditionalQuotaProvider(
      new AdditionalQuotaProvider() {
       public long getAdditionalQuota(Mailbox mbox) {
        return 10;
       }
      });
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = new HashMap<String, Object>();
  attrs.put("zimbraMailQuota", "30");
  Account acct = prov.createAccount("testAdditionalQuotaProvider@zimbra.com", "secret", attrs);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
  try {
   mbox.checkSizeChange(10);
  } catch (MailServiceException ignored) {
   fail("Unexpected QUOTA_EXCEEDED exception");
  }

  assertEquals(10L, mbox.getSize());
 }

  /**
   * @throws java.lang.Exception
   */
  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }
}
