// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.mailbox.util.TagUtil;

public class TagActionTest {
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

    private static final String name1 = "tag1", name2 = "tag2";

    private static String getTagIds(Tag... tags) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            sb.append(sb.length() == 0 ? "" : ",").append(tag.getId());
        }
        return sb.toString();
    }

 @Test
 void byId() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  Tag tag1 = mbox.createTag(null, name1, (byte) 0);
  Tag tag2 = mbox.createTag(null, name2, (byte) 1);
  String tagids = getTagIds(tag1, tag2);

  Element request = new Element.XMLElement(MailConstants.TAG_ACTION_REQUEST);
  Element action = request.addElement(MailConstants.E_ACTION);
  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_COLOR).addAttribute(MailConstants.A_COLOR, 4);
  action.addAttribute(MailConstants.A_ID, tagids);
  Element ack = new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct)).getElement(MailConstants.E_ACTION);

  assertEquals(4, mbox.getTagByName(null, name1).getColor(), name1 + " color set");
  assertEquals(4, mbox.getTagByName(null, name2).getColor(), name2 + " color set");

  assertEquals(tagids, ack.getAttribute(MailConstants.A_ID));
 }

 @Test
 void byName() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  Tag tag1 = mbox.createTag(null, name1, (byte) 0);
  Tag tag2 = mbox.createTag(null, name2, (byte) 1);
  String tagnames = TagUtil.encodeTags(name1, name2);

  Element request = new Element.XMLElement(MailConstants.TAG_ACTION_REQUEST);
  Element action = request.addElement(MailConstants.E_ACTION);
  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_COLOR).addAttribute(MailConstants.A_COLOR, 4);
  action.addAttribute(MailConstants.A_TAG_NAMES, tagnames);
  Element ack = new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct)).getElement(MailConstants.E_ACTION);

  assertEquals(4, mbox.getTagByName(null, name1).getColor(), name1 + " color set");
  assertEquals(4, mbox.getTagByName(null, name2).getColor(), name2 + " color set");

  assertEquals(getTagIds(tag1, tag2), ack.getAttribute(MailConstants.A_ID));
  assertEquals(tagnames, ack.getAttribute(MailConstants.A_TAG_NAMES));
 }

 @Test
 void invalid() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  mbox.createTag(null, name1, (byte) 0);

  Element request = new Element.XMLElement(MailConstants.TAG_ACTION_REQUEST);
  Element action = request.addElement(MailConstants.E_ACTION);
  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_MOVE).addAttribute(MailConstants.A_FOLDER, Mailbox.ID_FOLDER_USER_ROOT);
  action.addAttribute(MailConstants.A_TAG_NAMES, TagUtil.encodeTags(name1));
  try {
   new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct));
   fail("operation should not be permitted: " + ItemAction.OP_MOVE);
  } catch (ServiceException e) {
   assertEquals(ServiceException.INVALID_REQUEST, e.getCode(), "expected error code: " + ServiceException.INVALID_REQUEST);
  }

  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_RENAME).addAttribute(MailConstants.A_NAME, "tag3");
  action.addAttribute(MailConstants.A_TAG_NAMES, TagUtil.encodeTags(name2));
  try {
   new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct));
   fail("allowed op on nonexistent tag");
  } catch (ServiceException e) {
   assertEquals(MailServiceException.NO_SUCH_TAG, e.getCode(), "expected error code: " + MailServiceException.NO_SUCH_TAG);
  }
 }

 @Test
 void permissions() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  Tag tag1 = mbox.createTag(null, name1, (byte) 0);

  Element request = new Element.XMLElement(MailConstants.TAG_ACTION_REQUEST);
  Element action = request.addElement(MailConstants.E_ACTION);
  action.addAttribute(MailConstants.A_OPERATION, ItemAction.OP_COLOR).addAttribute(MailConstants.A_COLOR, 4);
  action.addAttribute(MailConstants.A_TAG_NAMES, TagUtil.encodeTags(name1));
  try {
   new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct2, acct));
   fail("colored another user's tags without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "expected error code: " + ServiceException.PERM_DENIED);
  }

  action.addAttribute(MailConstants.A_TAG_NAMES, (String) null).addAttribute(MailConstants.A_ID, tag1.getId());
  try {
   new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct2, acct));
   fail("colored another user's tags without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "expected error code: " + ServiceException.PERM_DENIED);
  }

  action.addAttribute(MailConstants.A_TAG_NAMES, TagUtil.encodeTags(name2));
  try {
   new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct2, acct));
   fail("colored another user's tags without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "expected error code: " + ServiceException.PERM_DENIED);
  }
 }

 @Test
 void delete() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // create the tag
  Element request = new Element.XMLElement(MailConstants.CREATE_TAG_REQUEST);
  request.addUniqueElement(MailConstants.E_TAG).addAttribute(MailConstants.A_COLOR, 4).addAttribute(MailConstants.A_NAME, "test");
  Element response = new CreateTag().handle(request, ServiceTestUtil.getRequestContext(acct));

  int tagId = response.getElement(MailConstants.E_TAG).getAttributeInt(MailConstants.A_ID);
  try {
   mbox.getTagById(null, tagId);
  } catch (ServiceException e) {
   fail("tag not created: " + e);
  }

  // delete the tag
  request = new Element.XMLElement(MailConstants.TAG_ACTION_REQUEST);
  request.addUniqueElement(MailConstants.E_ACTION).addAttribute(MailConstants.A_OPERATION, ItemAction.OP_HARD_DELETE).addAttribute(MailConstants.A_ID, tagId);
  new TagAction().handle(request, ServiceTestUtil.getRequestContext(acct));

  try {
   mbox.getTagById(null, tagId);
   fail("tag not deleted");
  } catch (NoSuchItemException e) {
  }
 }
}
