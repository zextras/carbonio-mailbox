// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;


import com.zimbra.common.service.ServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.service.mail.Search;
import com.zimbra.cs.service.mail.ServiceTestUtil;

import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SearchRequest;
import com.zimbra.soap.mail.message.SearchResponse;
import com.zimbra.soap.type.SearchHit;

/**
 * Unit test for {@link ItemQuery}
 * @author Greg Solovyev
 *
 */
public class ItemQueryTest {
     public String testName;
    

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
  MockProvisioning prov = new MockProvisioning();
  prov.createAccount("zero@zimbra.com", "secret", new HashMap<String, Object>());
  Provisioning.setInstance(prov);
 }

 @Test
 void testSyntax() throws ServiceException {
  Account account = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);

  ItemQuery q = (ItemQuery) ItemQuery.create(mailbox, "1,2");
  assertEquals(String.format("Q(ITEMID,%s:1,%s:2)", MockProvisioning.DEFAULT_ACCOUNT_ID, MockProvisioning.DEFAULT_ACCOUNT_ID), q.toString());

  q = (ItemQuery) ItemQuery.create(mailbox, "1");
  assertEquals(String.format("Q(ITEMID,%s:1)", MockProvisioning.DEFAULT_ACCOUNT_ID), q.toString());

  q = (ItemQuery) ItemQuery.create(mailbox, "all");
  assertEquals("Q(ITEMID,all)", q.toString());

  q = (ItemQuery) ItemQuery.create(mailbox, "none");
  assertEquals("Q(ITEMID,none)", q.toString());

  q = (ItemQuery) ItemQuery.create(mailbox, "1--10");
  assertEquals(String.format("Q(ITEMID,%s:1--%s:10)", MockProvisioning.DEFAULT_ACCOUNT_ID, MockProvisioning.DEFAULT_ACCOUNT_ID), q.toString());
 }

 @Test
 void testAllItemsQuery() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject2"), dopt, null);

  SearchResponse resp;
  List<SearchHit> hits;
  SearchRequest sr = new SearchRequest();
  sr.setSearchTypes("message");
  sr.setQuery("item:{all}");
  sr.setSortBy(SortBy.ATTACHMENT_ASC.toString());
  resp = doSearch(sr, acct);
  hits = resp.getSearchHits();
  assertEquals(2, hits.size(), "Number of hits");
 }

 @Test
 void testNoneItemsQuery() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);

  SearchResponse resp;
  List<SearchHit> hits;
  SearchRequest sr = new SearchRequest();
  sr.setSearchTypes("message");
  sr.setQuery("item:{none}");
  sr.setSortBy(SortBy.ATTACHMENT_ASC.toString());
  resp = doSearch(sr, acct);
  hits = resp.getSearchHits();
  assertEquals(0, hits.size(), "Number of hits");
 }

 @Test
 void testOneItemQuery() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  Message msg2 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject2"), dopt, null);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject3"), dopt, null);

  SearchResponse resp;
  List<SearchHit> hits;
  SearchRequest sr = new SearchRequest();
  sr.setSearchTypes("message");
  sr.setQuery(String.format("item:{%d}", msg2.getId()));
  sr.setSortBy(SortBy.ATTACHMENT_ASC.toString());
  resp = doSearch(sr, acct);
  hits = resp.getSearchHits();
  assertEquals(1, hits.size(), "Number of hits");
  int msgId = Integer.parseInt(hits.get(0).getId());
  assertEquals(msg2.getId(), msgId, "correct hit 1");
 }

 @Test
 void testListOfItemsQuery() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  Message msg1 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject2"), dopt, null);
  Message msg3 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject3"), dopt, null);

  SearchResponse resp;
  List<SearchHit> hits;
  SearchRequest sr = new SearchRequest();
  sr.setSearchTypes("message");
  sr.setQuery(String.format("item:{%d,%d}", msg1.getId(), msg3.getId()));
  sr.setSortBy(SortBy.ATTACHMENT_ASC.toString());
  resp = doSearch(sr, acct);
  hits = resp.getSearchHits();
  assertEquals(2, hits.size(), "Number of hits");
  int msgId = Integer.parseInt(hits.get(0).getId());
  assertEquals(msg1.getId(), msgId, "correct hit 1");
  msgId = Integer.parseInt(hits.get(1).getId());
  assertEquals(msg3.getId(), msgId, "correct hit 2");
 }

 @Test
 void testRangeOfItemsQuery() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  Message msg1 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  Message msg2 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject2"), dopt, null);
  Message msg3 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject3"), dopt, null);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject4"), dopt, null);

  SearchResponse resp;
  List<SearchHit> hits;
  SearchRequest sr = new SearchRequest();
  sr.setSearchTypes("message");
  sr.setQuery(String.format("item:{%d--%d}", msg1.getId(), msg3.getId()));
  sr.setSortBy(SortBy.ATTACHMENT_ASC.toString());
  resp = doSearch(sr, acct);
  hits = resp.getSearchHits();
  assertEquals(3, hits.size(), "Number of hits");
  int msgId = Integer.parseInt(hits.get(0).getId());
  assertEquals(msg1.getId(), msgId, "correct hit 1");
  msgId = Integer.parseInt(hits.get(1).getId());
  assertEquals(msg2.getId(), msgId, "correct hit 2");
  msgId = Integer.parseInt(hits.get(2).getId());
  assertEquals(msg3.getId(), msgId, "correct hit 3");
 }

 @Test
 void testInvalidRangeQuery() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("zero@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  Message msg1 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
  Message msg2 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject2"), dopt, null);
  Message msg3 = mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject3"), dopt, null);
  mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject4"), dopt, null);

  SearchRequest sr = new SearchRequest();
  sr.setSearchTypes("message");
  String q = String.format("item:{%d--%d--%d}", msg1.getId(), msg2.getId(), msg3.getId());
  sr.setQuery(q);
  try {
   doSearch(sr, acct);
   fail("Should have thrown a PARSE_ERROR exception for " + q);
  } catch (ServiceException e) {
   assertEquals(e.getCode(), ServiceException.PARSE_ERROR);
  }

  sr.setSearchTypes("message");
  q = String.format("item:{%d--%d,%d}", msg1.getId(), msg2.getId(), msg3.getId());
  sr.setQuery(q);
  try {
   doSearch(sr, acct);
   fail("Should have thrown an INVALID_REQUEST exception for " + q);
  } catch (ServiceException e) {
   assertEquals(e.getCode(), ServiceException.INVALID_REQUEST);
  }

  sr.setSearchTypes("message");
  q = String.format("item:{%d,%d--%d}", msg1.getId(), msg2.getId(), msg3.getId());
  sr.setQuery(q);
  try {
   doSearch(sr, acct);
   fail("Should have thrown an INVALID_REQUEST exception for " + q);
  } catch (ServiceException e) {
   assertEquals(e.getCode(), ServiceException.INVALID_REQUEST);
  }
 }

    private static SearchResponse doSearch(SearchRequest request, Account acct) throws Exception {
        Element response = new Search().handle(JaxbUtil.jaxbToElement(request, Element.XMLElement.mFactory),
                                ServiceTestUtil.getRequestContext(acct));
        SearchResponse resp = JaxbUtil.elementToJaxb(response, SearchResponse.class);
        return resp;
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
