// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.util.TypedIdList;
import com.zimbra.cs.util.ZTestWatchman;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.ConvActionRequest;
import com.zimbra.soap.mail.message.SearchRequest;
import com.zimbra.soap.mail.type.BulkAction;
import com.zimbra.soap.type.SearchHit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;
import org.mockito.MockedStatic;

public class SearchActionTest {

  @Rule public TestName testName = new TestName();
  @Rule public MethodRule watchman = new ZTestWatchman();

  @Before
  public void setUp() throws Exception {
    System.out.println(testName.getMethodName());
    MailboxTestUtil.initServer();
    MailboxTestUtil.clearData();
    Provisioning prov = Provisioning.getInstance();
    Map<String, Object> attrs = Maps.newHashMap();
    prov.createDomain("zimbra.com", attrs);

    attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("testMove@zimbra.com", "secret", attrs);

    attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("testRead@zimbra.com", "secret", attrs);
  }

  @Test
  public void testSearchActionMove() throws Exception {
    Account acct = Provisioning.getInstance().getAccountByName("testMove@zimbra.com");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    // Add two messages to inbox, one with search match and other with no match
    DeliveryOptions dopt =
        new DeliveryOptions()
            .setFolderId(Mailbox.ID_FOLDER_INBOX)
            .setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_MUTED);
    mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
    mbox.addMessage(null, MailboxTestUtil.generateMessage("unmatched subject"), dopt, null);
    TypedIdList ids = mbox.getItemIds(null, 2);
    assertEquals(2, ids.size());
    SearchRequest sRequest = new SearchRequest();
    sRequest.setSearchTypes("conversation");
    // search with query 'test'
    sRequest.setQuery("test");
    BulkAction bAction = new BulkAction();
    // search action - move search result to 'Trash'
    bAction.setOp(BulkAction.Operation.move);
    bAction.setFolder("Trash");
    Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
    ZimbraSoapContext zsc = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
    Element searchResponse =
        new Search().handle(zsc.jaxbToElement(sRequest), ServiceTestUtil.getRequestContext(acct));
    com.zimbra.soap.mail.message.SearchResponse sResponse = zsc.elementToJaxb(searchResponse);
    List<SearchHit> searchHits = sResponse.getSearchHits();
    SearchAction.performAction(bAction, sRequest, searchHits, mbox, null);
    // check inbox contains only 1 unmatched mail item after move
    List<MailItem> mailItems =
        mbox.getItemList(null, MailItem.Type.MESSAGE, 2, com.zimbra.cs.index.SortBy.DATE_DESC);
    assertEquals(1, mailItems.size());
    assertEquals("unmatched subject", mailItems.get(0).getSubject());
    // check trash contains mail item having 'test subject' after move
    mailItems =
        mbox.getItemList(null, MailItem.Type.MESSAGE, 3, com.zimbra.cs.index.SortBy.DATE_DESC);
    assertEquals(1, mailItems.size());
    assertEquals("test subject", mailItems.get(0).getSubject());
  }

  @Test
  public void testSearchActionRead() throws Exception {
    Account acct = Provisioning.getInstance().getAccountByName("testRead@zimbra.com");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    // Add two messages to inbox, one with search match and other with no match
    DeliveryOptions dopt =
        new DeliveryOptions()
            .setFolderId(Mailbox.ID_FOLDER_INBOX)
            .setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_MUTED);
    Message message1 =
        mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
    Message message2 =
        mbox.addMessage(null, MailboxTestUtil.generateMessage("unmatched subject"), dopt, null);
    TypedIdList ids = mbox.getItemIds(null, 2);
    assertEquals(2, ids.size());
    assertEquals(true, message1.isUnread());
    assertEquals(true, message2.isUnread());
    SearchRequest sRequest = new SearchRequest();
    sRequest.setSearchTypes("conversation");
    // search with query 'test'
    sRequest.setQuery("test");
    BulkAction bAction = new BulkAction();
    // search action - mark search result with 'read'
    bAction.setOp(BulkAction.Operation.read);
    Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
    ZimbraSoapContext zsc = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
    Element searchResponse =
        new Search().handle(zsc.jaxbToElement(sRequest), ServiceTestUtil.getRequestContext(acct));
    com.zimbra.soap.mail.message.SearchResponse sResponse = zsc.elementToJaxb(searchResponse);
    List<SearchHit> searchHits = sResponse.getSearchHits();
    ConvActionRequest req = SearchAction.getConvActionRequest(searchHits, "read");
    ConvAction convAction = new ConvAction();
    SoapHttpTransport mockSoapHttpTransport = mock(SoapHttpTransport.class);
    MockedStatic<SearchAction> searchActionMockedStatic = mockStatic(SearchAction.class);
    searchActionMockedStatic
        .when(() -> SearchAction.getSoapHttpTransportInstance(anyString()))
        .thenReturn(mockSoapHttpTransport);

    when(mockSoapHttpTransport.invokeWithoutSession(any()))
        .thenReturn(
            convAction.handle(zsc.jaxbToElement(req), ServiceTestUtil.getRequestContext(acct)));
    SearchAction.performAction(bAction, sRequest, searchHits, mbox, null);
    // check search result message is marked read
    assertEquals(false, message1.isUnread());
    assertEquals(true, message2.isUnread());
  }

  @After
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
