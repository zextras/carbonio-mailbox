// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.MethodRule;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.util.ZTestWatchman;

public class SearchTest {
    
    public String testName;
    @Rule
    public MethodRule watchman = new ZTestWatchman();
    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  Provisioning prov = Provisioning.getInstance();
  prov.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());
  prov.createAccount("testZCS3705@zimbra.com", "secret", Maps.<String, Object>newHashMap());
 }

 @Test
 void mute() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // setup: add a message
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_MUTED);
  Message msg = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  assertTrue(msg.isUnread(), "root unread");
  assertTrue(msg.isTagged(Flag.FlagInfo.MUTED), "root muted");

  // search for the conversation (normal)
  Element request = new Element.XMLElement(MailConstants.SEARCH_REQUEST).addAttribute(MailConstants.A_SEARCH_TYPES, "conversation");
  request.addAttribute(MailConstants.E_QUERY, "test", Element.Disposition.CONTENT);
  Element response = new Search().handle(request, ServiceTestUtil.getRequestContext(acct));

  List<Element> hits = response.listElements(MailConstants.E_CONV);
  assertEquals(1, hits.size(), "1 hit");
  assertEquals(msg.getConversationId(), hits.get(0).getAttributeLong(MailConstants.A_ID), "correct hit");

  // search for the conversation (no muted items)
  request.addAttribute(MailConstants.A_INCLUDE_TAG_MUTED, false);
  response = new Search().handle(request, ServiceTestUtil.getRequestContext(acct));

  hits = response.listElements(MailConstants.E_CONV);
  assertTrue(hits.isEmpty(), "no hits");
 }

 @Test
 @Disabled("Fix me. Got -257 conversationId instead of -258.")
 void testZCS3705() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("testZCS3705@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  // add two messages - msg1 and msg2
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX)
    .setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_MUTED);
  Message msg1 = mbox.addMessage(null, MailboxTestUtil.generateMessage("read subject"), dopt,
    null);
  Message msg2 = mbox.addMessage(null, MailboxTestUtil.generateMessage("unread subject"),
    dopt, null);
  assertTrue(msg1.isUnread(), "msg unread");
  assertTrue(msg2.isUnread(), "msg unread");

  // read msg1
  Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
  Element action = request.addElement(MailConstants.E_MSG);
  action.addAttribute(MailConstants.A_ID, msg1.getId());
  action.addAttribute(MailConstants.A_MARK_READ, 1);
  new GetMsg().handle(request, ServiceTestUtil.getRequestContext(mbox.getAccount()))
    .getElement(MailConstants.E_MSG);
  assertFalse(msg1.isUnread(), "msg read");
  assertTrue(msg2.isUnread(), "msg unread");

  // search for the conversation (sortBy readDesc) - msg2 should be listed before msg1
  Element searchRequest = new Element.XMLElement(MailConstants.SEARCH_REQUEST)
    .addAttribute(MailConstants.A_SEARCH_TYPES, "conversation");
  searchRequest.addAttribute(MailConstants.E_QUERY, "subject", Element.Disposition.CONTENT);
  searchRequest.addAttribute("sortBy", "readDesc");
  Element searchResponse = new Search().handle(searchRequest,
    ServiceTestUtil.getRequestContext(acct));
  List<Element> hits = searchResponse.listElements(MailConstants.E_CONV);
  assertEquals(2, hits.size(), "2 hits");
  assertEquals(msg2.getConversationId(),
    hits.get(0).getAttributeLong(MailConstants.A_ID),
    "correct hit");
  assertEquals(msg1.getConversationId(),
    hits.get(1).getAttributeLong(MailConstants.A_ID),
    "correct hit");

  // search for the conversation (sortBy unreadDesc) - msg1 should be listed before msg2
  searchRequest = new Element.XMLElement(MailConstants.SEARCH_REQUEST)
    .addAttribute(MailConstants.A_SEARCH_TYPES, "conversation");
  searchRequest.addAttribute(MailConstants.E_QUERY, "subject", Element.Disposition.CONTENT);
  searchRequest.addAttribute("sortBy", "readAsc");
  searchResponse = new Search().handle(searchRequest,
    ServiceTestUtil.getRequestContext(acct));
  hits = searchResponse.listElements(MailConstants.E_CONV);
  assertEquals(2, hits.size(), "2 hits");
  assertEquals(msg1.getConversationId(),
    hits.get(0).getAttributeLong(MailConstants.A_ID),
    "correct hit");
  assertEquals(msg2.getConversationId(),
    hits.get(1).getAttributeLong(MailConstants.A_ID),
    "correct hit");
 }

    @AfterEach
    public void tearDown() {
        try {
            MailboxTestUtil.clearData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}