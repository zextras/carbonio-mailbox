// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning.MailThreadingAlgorithm;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

public class ItemActionTest {
    private static final String tag1 = "foo", tag2 = "bar";

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();

        prov.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());

        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("test2@zimbra.com", "secret", attrs);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void inherit() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  mbox.grantAccess(null, Mailbox.ID_FOLDER_INBOX, acct2.getId(), ACL.GRANTEE_USER, ACL.RIGHT_READ, null);
  String targets = Mailbox.ID_FOLDER_INBOX + "," + Mailbox.ID_FOLDER_TRASH;

  Element request = new Element.XMLElement(MailConstants.ITEM_ACTION_REQUEST);
  request.addElement(MailConstants.E_ACTION).addAttribute(MailConstants.A_OPERATION, ItemAction.OP_INHERIT).addAttribute(MailConstants.A_ID, targets);
  new ItemAction().handle(request, ServiceTestUtil.getRequestContext(acct));

  Folder inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
  assertFalse(inbox.isTagged(Flag.FlagInfo.NO_INHERIT), "inbox doesn't have \\NoInherit");
  assertNull(inbox.getEffectiveACL(), "no ACL on inbox");
 }

 @Test
 void moveConversationToAcctRelativePath() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  acct.setMailThreadingAlgorithm(MailThreadingAlgorithm.subject);

  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  int msgId = mbox.addMessage(null, pm, dopt, null).getId();

  String targetFolderName = "folder1";
  ItemActionHelper.MOVE(null, mbox, SoapProtocol.Soap12, Arrays.asList(msgId * -1), null, targetFolderName);
  Folder newFolder = mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, targetFolderName);
  Message msg = mbox.getMessageById(null, msgId);
  assertEquals(msg.getFolderId(), newFolder.getId());
 }

 @Test
 void copyMessageFromDraftsToSent() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject copyMessageFromDraftsToSent");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_DRAFTS);
  int msgId = mbox.addMessage(null, pm, dopt, null).getId();
  ItemId iidTarget = new ItemId(mbox, Mailbox.ID_FOLDER_SENT);
  ItemActionHelper op = ItemActionHelper.COPY(null, mbox, SoapProtocol.Soap12, Arrays.asList(msgId), MailItem.Type.MESSAGE, null, iidTarget);
  assertNotNull(op, "test non-null response");
  assertTrue(op.getResult() instanceof CopyActionResult, "test CopyActionResult");
  CopyActionResult copyActionResult = (CopyActionResult) op.getResult();
  assertNotNull(copyActionResult.getSuccessIds(), "test non-null success info");
  assertEquals(1, copyActionResult.getSuccessIds().size(), "test correct success count");
  ItemId iid = new ItemId(copyActionResult.getCreatedIds().get(0), acct.getId());
  assertNotNull(iid, "test non-null created info");
  Message copiedMessage = mbox.getMessageById(null, iid.getId());
  assertNotNull(copiedMessage, "test non-null message");
  assertNotNull(copiedMessage.getSubject(), "test non-null subject in copied message");
  assertEquals("test subject copyMessageFromDraftsToSent", copiedMessage.getSubject());
  assertEquals(Mailbox.ID_FOLDER_SENT, copiedMessage.getFolderId(), "test parent folder of copied message");
 }

 @Test
 void deleteIncompleteConversation() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  acct.setMailThreadingAlgorithm(MailThreadingAlgorithm.subject);

  // setup: add the root message
  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  int rootId = mbox.addMessage(null, pm, dopt, null).getId();

  // add additional messages
  pm = MailboxTestUtil.generateMessage("Re: test subject");
  Message draft = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT, rootId + "", MailSender.MSGTYPE_REPLY, null, null, 0);
  Message parent = mbox.getMessageById(null, rootId);
  assertEquals(parent.getConversationId(), draft.getConversationId());

  pm = MailboxTestUtil.generateMessage("Re: test subject");
  Message draft2 = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT);
  parent = mbox.getMessageById(null, rootId);
  assertEquals(parent.getConversationId(), draft2.getConversationId());

  MailItem.TargetConstraint tcon = new MailItem.TargetConstraint(mbox, MailItem.TargetConstraint.INCLUDE_TRASH);
  ItemId iid = new ItemId(mbox, Mailbox.ID_FOLDER_TRASH);

  // trash one message in conversation
  ItemActionHelper.MOVE(null, mbox, SoapProtocol.Soap12, Collections.singletonList(draft.getId()), MailItem.Type.MESSAGE, tcon, iid);
  draft = mbox.getMessageById(null, draft.getId());
  assertEquals(draft.getFolderId(), Mailbox.ID_FOLDER_TRASH);

  ItemActionHelper.HARD_DELETE(null, mbox, SoapProtocol.Soap12, Collections.singletonList(draft.getConversationId()), MailItem.Type.CONVERSATION, tcon);

  // the messages not in the trash should still exist and attached to the same conversation
  parent = mbox.getMessageById(null, rootId);
  Message m = mbox.getMessageById(null, draft2.getId());
  assertEquals(parent.getConversationId(), m.getConversationId());
 }

 @Test
 void deleteConversation() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  acct.setMailThreadingAlgorithm(MailThreadingAlgorithm.subject);

  // setup: add the root message
  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  int rootId = mbox.addMessage(null, pm, dopt, null).getId();

  // add additional messages
  pm = MailboxTestUtil.generateMessage("Re: test subject");
  Message draft = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT, rootId + "", MailSender.MSGTYPE_REPLY, null, null, 0);
  Message parent = mbox.getMessageById(null, rootId);
  assertEquals(parent.getConversationId(), draft.getConversationId());

  pm = MailboxTestUtil.generateMessage("Re: test subject");
  Message draft2 = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT);
  parent = mbox.getMessageById(null, rootId);
  assertEquals(parent.getConversationId(), draft2.getConversationId());

  MailItem.TargetConstraint tcon = new MailItem.TargetConstraint(mbox, MailItem.TargetConstraint.INCLUDE_TRASH);
  ItemId iid = new ItemId(mbox, Mailbox.ID_FOLDER_TRASH);

  // trash the conversation
  ItemActionHelper.MOVE(null, mbox, SoapProtocol.Soap12, Arrays.asList(parent.getId(), draft.getId(), draft2.getId()), MailItem.Type.MESSAGE, tcon, iid);
  parent = mbox.getMessageById(null, parent.getId());
  draft = mbox.getMessageById(null, draft.getId());
  draft2 = mbox.getMessageById(null, draft2.getId());
  assertEquals(parent.getFolderId(), Mailbox.ID_FOLDER_TRASH);
  assertEquals(draft.getFolderId(), Mailbox.ID_FOLDER_TRASH);
  assertEquals(draft2.getFolderId(), Mailbox.ID_FOLDER_TRASH);

  ItemActionHelper.HARD_DELETE(null, mbox, SoapProtocol.Soap12, Collections.singletonList(parent.getConversationId()), MailItem.Type.CONVERSATION, tcon);
  Exception ex = null;
  try {
   mbox.getMessageById(null, parent.getId());
  } catch (Exception e) {
   ex = e;
  }
  assertTrue(ex instanceof NoSuchItemException, "test NoSuchItemException (parent/id)");
  ex = null;
  try {
   mbox.getMessageById(null, draft.getId());
  } catch (Exception e) {
   ex = e;
  }
  assertTrue(ex instanceof NoSuchItemException, "test NoSuchItemException (draft/id)");
  ex = null;
  try {
   mbox.getMessageById(null, draft2.getId());
  } catch (Exception e) {
   ex = e;
  }
  assertTrue(ex instanceof NoSuchItemException, "test NoSuchItemException (draft2/id)");
  ex = null;
  try {
   mbox.getConversationById(null, draft2.getConversationId());
  } catch (Exception e) {
   ex = e;
  }
  assertTrue(ex instanceof NoSuchItemException, "test NoSuchItemException (draft2/conversation)");
 }

 @Test
 void moveConversationPreviousFolderTest() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  acct.setMailThreadingAlgorithm(MailThreadingAlgorithm.subject);

  // setup: add the root message
  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg1 = mbox.addMessage(null, pm, dopt, null);
  Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE);
  int folder1Id = mbox.createFolder(null, "folder1", fopt).getId();
  ItemId iid1 =  new ItemId(mbox, folder1Id);
  int folder2Id = mbox.createFolder(null, "folder2", fopt).getId();
  new ItemId(mbox, folder2Id);
  int folder3Id = mbox.createFolder(null, "folder3", fopt).getId();
  ItemId iid3 =  new ItemId(mbox, folder3Id);

  ItemActionHelper.MOVE(null, mbox, SoapProtocol.Soap12, Arrays.asList(msg1.getId()), MailItem.Type.MESSAGE, null, iid1);
  String msg1PrevFolder = mbox.getLastChangeID() + ":" +  Mailbox.ID_FOLDER_INBOX;
  assertEquals(msg1PrevFolder, msg1.getPrevFolders());

  dopt = new DeliveryOptions().setFolderId(folder2Id);
  Message msg2 = mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject"), dopt, null);
  assertNull(msg2.getPrevFolders());

  Element request = new Element.XMLElement(MailConstants.CONV_ACTION_REQUEST);
  request.addElement(MailConstants.E_ACTION).addAttribute(MailConstants.A_OPERATION, ItemAction.OP_MOVE).addAttribute(MailConstants.A_ID, msg1.getConversationId()).addAttribute("l", iid3.getId());
  new ConvAction().handle(request, ServiceTestUtil.getRequestContext(acct));
  msg1PrevFolder = msg1PrevFolder + ";" + mbox.getLastChangeID() + ":" +  folder1Id;
  String msg2PrevFolder = mbox.getLastChangeID() + ":" +  folder2Id;
  assertEquals(msg1PrevFolder, msg1.getPrevFolders());
  assertEquals(msg2PrevFolder, msg2.getPrevFolders());
 }

 @Test
 void mute() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // setup: add a message
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  Message msg = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  assertTrue(msg.isUnread(), "root unread");
  assertFalse(msg.isTagged(Flag.FlagInfo.MUTED), "root not muted");

  // mute virtual conv
  Element request = new Element.XMLElement(MailConstants.CONV_ACTION_REQUEST);
  Element action = request.addElement(MailConstants.E_ACTION).addAttribute(MailConstants.A_OPERATION, ItemAction.OP_MUTE).addAttribute(MailConstants.A_ID, msg.getConversationId());
  new ConvAction().handle(request, ServiceTestUtil.getRequestContext(acct));

  msg = mbox.getMessageById(null, msg.getId());
  assertFalse(msg.isUnread(), "root now read");
  assertTrue(msg.isTagged(Flag.FlagInfo.MUTED), "root now muted");

  // unmute virtual conv
  action.addAttribute(MailConstants.A_OPERATION, "!" + ItemAction.OP_MUTE);
  new ConvAction().handle(request, ServiceTestUtil.getRequestContext(acct));

  msg = mbox.getMessageById(null, msg.getId());
  assertFalse(msg.isUnread(), "root still read");
  assertFalse(msg.isTagged(Flag.FlagInfo.MUTED), "root now unmuted");

  // add another message to create a real conv
  dopt.setConversationId(msg.getConversationId());
  Message msg2 = mbox.addMessage(null, MailboxTestUtil.generateMessage("Re: test subject"), dopt, null);
  assertTrue(msg2.isUnread(), "reply unread");
  assertFalse(msg2.isTagged(Flag.FlagInfo.MUTED), "reply not muted");
  assertFalse(msg2.getConversationId() < 0, "reply in real conv");

  // mute real conv
  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_MUTE).addAttribute(MailConstants.A_ID, msg2.getConversationId());
  new ConvAction().handle(request, ServiceTestUtil.getRequestContext(acct));

  msg2 = mbox.getMessageById(null, msg2.getId());
  assertFalse(msg2.isUnread(), "reply now read");
  assertTrue(msg2.isTagged(Flag.FlagInfo.MUTED), "reply now muted");

  // unmute real conv
  action.addAttribute(MailConstants.A_OPERATION, "!" + ItemAction.OP_MUTE);
  new ConvAction().handle(request, ServiceTestUtil.getRequestContext(acct));

  msg2 = mbox.getMessageById(null, msg2.getId());
  assertFalse(msg2.isUnread(), "reply still read");
  assertFalse(msg2.isTagged(Flag.FlagInfo.MUTED), "reply now unmuted");
 }

 @Test
 void deleteAllTagKeepsStatusOfFlags() throws Exception {
  //Bug 76781
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name,
    "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  acct.setMailThreadingAlgorithm(MailThreadingAlgorithm.subject);

  // setup: add the root message
  ParsedMessage pm = MailboxTestUtil.generateMessage("test subject");
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(
    Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(null, pm, dopt, null);

  // add additional messages for conversation
  pm = MailboxTestUtil.generateMessage("Re: test subject");
  int msgId = mbox.addMessage(null, pm, dopt, null).getId();
  // set flag to unread for  this message
  MailboxTestUtil.setFlag(mbox, msgId, Flag.FlagInfo.UNREAD);


  MailItem item = mbox.getItemById(null, msgId, MailItem.Type.UNKNOWN);
  // verify message unread flag is set
  assertEquals(Flag.BITMASK_UNREAD,
    item.getFlagBitmask(),
    "Verifying Unread flag is set.");

  // add 2 tags
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag1, true, null);
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag2, true, null);


  Element request = new Element.XMLElement(
    MailConstants.ITEM_ACTION_REQUEST);
  Element action = request.addElement(MailConstants.E_ACTION);
  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_UPDATE);
  action.addAttribute(MailConstants.A_ITEM_TYPE, "");
  action.addAttribute(MailConstants.A_ID, msgId);

  new ItemAction().handle(request, ServiceTestUtil.getRequestContext(acct));
  assertEquals(Flag.BITMASK_UNREAD,
    item.getFlagBitmask(),
    "Verifying unread flag is set after tag deletion");

  Tag tag = mbox.getTagByName(null, tag1);
  assertEquals(0, tag.getSize(), tag1 + " (tag messages)");

  tag = mbox.getTagByName(null, tag2);
  assertEquals(0, tag.getSize(), tag1 + " (tag messages)");

 }

}
