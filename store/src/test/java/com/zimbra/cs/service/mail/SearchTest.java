// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SearchTest extends MailboxTestSuite {

  private static Account testAccount;

  @BeforeAll
  public static void init() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    provisioning.createDomain("zimbra.com", new HashMap<>());
    provisioning.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());
    provisioning.createAccount(
        "testZCS3705@zimbra.com", "secret", Maps.<String, Object>newHashMap());
    testAccount = provisioning.createAccount("test@test.com", "password", new HashMap<>());
  }

//  @BeforeEach
//  public void setUp() throws Exception {
//    clearData();
//    initData();
//  }

  @Test
  void mute() throws Exception {
    Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

    // setup: add a message
    DeliveryOptions dopt =
        new DeliveryOptions()
            .setFolderId(Mailbox.ID_FOLDER_INBOX)
            .setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_MUTED);
    Message msg =
        mbox.addMessage(null, MailboxTestUtil.generateMessage("test subject"), dopt, null);
    assertTrue(msg.isUnread(), "root unread");
    assertTrue(msg.isTagged(Flag.FlagInfo.MUTED), "root muted");

    // search for the conversation (normal)
    Element request =
        new Element.XMLElement(MailConstants.SEARCH_REQUEST)
            .addAttribute(MailConstants.A_SEARCH_TYPES, "conversation");
    request.addAttribute(MailConstants.E_QUERY, "test", Element.Disposition.CONTENT);
    Element response = new Search().handle(request, ServiceTestUtil.getRequestContext(acct));

    List<Element> hits = response.listElements(MailConstants.E_CONV);
    assertEquals(1, hits.size(), "1 hit");
    assertEquals(
        msg.getConversationId(), hits.get(0).getAttributeLong(MailConstants.A_ID), "correct hit");

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
    DeliveryOptions dopt =
        new DeliveryOptions()
            .setFolderId(Mailbox.ID_FOLDER_INBOX)
            .setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_MUTED);
    Message msg1 =
        mbox.addMessage(null, MailboxTestUtil.generateMessage("read subject"), dopt, null);
    Message msg2 =
        mbox.addMessage(null, MailboxTestUtil.generateMessage("unread subject"), dopt, null);
    assertTrue(msg1.isUnread(), "msg unread");
    assertTrue(msg2.isUnread(), "msg unread");

    // read msg1
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg1.getId());
    action.addAttribute(MailConstants.A_MARK_READ, 1);
    new GetMsg()
        .handle(request, ServiceTestUtil.getRequestContext(mbox.getAccount()))
        .getElement(MailConstants.E_MSG);
    assertFalse(msg1.isUnread(), "msg read");
    assertTrue(msg2.isUnread(), "msg unread");

    // search for the conversation (sortBy readDesc) - msg2 should be listed before msg1
    Element searchRequest =
        new Element.XMLElement(MailConstants.SEARCH_REQUEST)
            .addAttribute(MailConstants.A_SEARCH_TYPES, "conversation");
    searchRequest.addAttribute(MailConstants.E_QUERY, "subject", Element.Disposition.CONTENT);
    searchRequest.addAttribute("sortBy", "readDesc");
    Element searchResponse =
        new Search().handle(searchRequest, ServiceTestUtil.getRequestContext(acct));
    List<Element> hits = searchResponse.listElements(MailConstants.E_CONV);
    assertEquals(2, hits.size(), "2 hits");
    assertEquals(
        msg2.getConversationId(), hits.get(0).getAttributeLong(MailConstants.A_ID), "correct hit");
    assertEquals(
        msg1.getConversationId(), hits.get(1).getAttributeLong(MailConstants.A_ID), "correct hit");

    // search for the conversation (sortBy unreadDesc) - msg1 should be listed before msg2
    searchRequest =
        new Element.XMLElement(MailConstants.SEARCH_REQUEST)
            .addAttribute(MailConstants.A_SEARCH_TYPES, "conversation");
    searchRequest.addAttribute(MailConstants.E_QUERY, "subject", Element.Disposition.CONTENT);
    searchRequest.addAttribute("sortBy", "readAsc");
    searchResponse = new Search().handle(searchRequest, ServiceTestUtil.getRequestContext(acct));
    hits = searchResponse.listElements(MailConstants.E_CONV);
    assertEquals(2, hits.size(), "2 hits");
    assertEquals(
        msg1.getConversationId(), hits.get(0).getAttributeLong(MailConstants.A_ID), "correct hit");
    assertEquals(
        msg2.getConversationId(), hits.get(1).getAttributeLong(MailConstants.A_ID), "correct hit");
  }

  @Test
  @DisplayName("Check Calendar Search returns RidZ field. Added for BUG CO-831.")
  void shouldEncodeCalItemWithRidZField() throws Exception {
    Map<String, Object> context = new HashMap<>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(testAccount),
            testAccount.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

    final Element createAppointmentElement =
        Element.parseJSON(
            new String(
                Objects.requireNonNull(this.getClass()
                        .getResourceAsStream("CO-831-CalendarAppointmentRequest.json"))
                    .readAllBytes()),
            MailConstants.CREATE_APPOINTMENT_REQUEST,
            JSONElement.mFactory);
    final CreateCalendarItem createCalendarItem = new CreateCalendarItem();
    createCalendarItem.setResponseQName(MailConstants.CREATE_APPOINTMENT_REQUEST);
    createCalendarItem.handle(createAppointmentElement, context);

    final Search search = new Search();
    final String jsonSearch =
        new String(
            this.getClass()
                .getResourceAsStream("CO-831-CalendarSearchRequest.json")
                .readAllBytes());
    final Element searchRequestElement =
        Element.parseJSON(jsonSearch, MailConstants.SEARCH_REQUEST, JSONElement.mFactory);
    final Element searchResponseElement = search.handle(searchRequestElement, context);
    Assertions.assertTrue(searchResponseElement.toString().contains("ridZ"));
  }

  /**
   * Creates test messages in the inbox
   */
  private Message[] createTestMessages(Mailbox mbox, String... subjects) throws Exception {
    Message[] messages = new Message[subjects.length];
    DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);

    for (int i = 0; i < subjects.length; i++) {
      messages[i] = mbox.addMessage(null, MailboxTestUtil.generateMessage(subjects[i]), dopt, null);
    }

    return messages;
  }

  /**
   * Trashes messages in the given order
   */
  private void trashMessagesInOrder(Account acct, Message... messages) throws Exception {
    for (Message message : messages) {
      trashMessage(acct, message);
      Thread.sleep(1000); // Small delay to ensure different change dates
    }
  }

  /**
   * Trashes a single message
   */
  private void trashMessage(Account acct, Message message) throws Exception {
    Element actionRequest = new Element.XMLElement(MailConstants.MSG_ACTION_REQUEST);
    Element action = actionRequest.addUniqueElement(MailConstants.E_ACTION);
    action.addAttribute(MailConstants.A_ID, message.getId());
    action.addAttribute(MailConstants.A_OPERATION, "trash");
    new MsgAction().handle(actionRequest, ServiceTestUtil.getRequestContext(acct));
  }

  /**
   * Creates a search request for trash folder with optional query and sort
   */
  private Element createSearchRequest(String query, String sortBy) {
    Element request = new Element.XMLElement(MailConstants.SEARCH_REQUEST)
        .addAttribute(MailConstants.A_SEARCH_TYPES, "message");

    String fullQuery = "in:trash" + (query != null && !query.isEmpty() ? " " + query : "");
    request.addAttribute(MailConstants.E_QUERY, fullQuery, Element.Disposition.CONTENT);

    if (sortBy != null && !sortBy.isEmpty()) {
      request.addAttribute(MailConstants.A_SORTBY, sortBy);
    }

    return request;
  }

  /**
   * Executes search and returns the results
   */
  private SearchResult executeSearch(Account acct, Element searchRequest) throws Exception {
    Element response = new Search().handle(searchRequest, ServiceTestUtil.getRequestContext(acct));
    List<Element> hits = response.listElements(MailConstants.E_MSG);
    String sortBy = response.getAttribute(MailConstants.A_SORTBY);

    return new SearchResult(hits, sortBy);
  }

  /**
   * Creates a test account with the given username
   */
  private Account createTestAccount(String username) throws Exception {
    return createAccount()
        .withUsername(username)
        .withDomain(DEFAULT_DOMAIN_NAME)
        .withPassword("secret")
        .create();
  }

  @Test
  @DisplayName("Test Search API with changeDateDesc sort - most recently deleted items first")
  void testSearchWithChangeDateDescSort() throws Exception {
    Account acct = createTestAccount("test5420");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

    // Create three messages
    Message[] messages = createTestMessages(mbox,
        "test subject 1",
        "test subject 2",
        "test subject 3");

    // Trash messages in a specific order: msg1, then msg3, then msg2
    // This means msg2 was deleted most recently, msg3 second, msg1 first
    trashMessagesInOrder(acct, messages[0], messages[2], messages[1]);

    // Search trash with changeDateDesc sort (newest deletion first)
    Element request = createSearchRequest(null, "changeDateDesc");
    SearchResult result = executeSearch(acct, request);

    // Verify results
    assertEquals("changeDateDesc", result.sortBy, "Response should indicate changeDateDesc sort");
    assertEquals(3, result.hits.size(), "Should find 3 deleted messages");

    // With changeDateDesc sort, we expect: msg2 (most recently deleted), msg3, msg1 (oldest)
    assertSearchResultsOrder(result.hits,
        messages[1].getId(), // msg2 - most recently deleted
        messages[2].getId(), // msg3
        messages[0].getId()  // msg1 - first deleted
    );
  }

  @Test
  @DisplayName("Test Search API with changeDateAsc sort - oldest deleted items first")
  void testSearchWithChangeDateAscSort() throws Exception {
    Account acct = createTestAccount("test54202");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

    // Create three messages
    Message[] messages = createTestMessages(mbox,
        "test subject 1",
        "test subject 2",
        "test subject 3");

    // Trash messages in order: msg3, then msg1, then msg2
    trashMessagesInOrder(acct, messages[2], messages[0], messages[1]);

    // Search trash with changeDateAsc sort (oldest deletion first)
    Element request = createSearchRequest(null, "changeDateAsc");
    SearchResult result = executeSearch(acct, request);

    // Verify results
    assertEquals("changeDateAsc", result.sortBy, "Response should indicate changeDateAsc sort");
    assertEquals(3, result.hits.size(), "Should find 3 deleted messages");

    // With changeDateAsc sort, we expect: msg3 (first deleted), msg1, msg2 (most recent)
    assertSearchResultsOrder(result.hits,
        messages[2].getId(), // msg3 - first deleted
        messages[0].getId(), // msg1
        messages[1].getId()  // msg2 - most recently deleted
    );
  }

  @Test
  @DisplayName("Test Search API with changeDateDesc and query filters")
  void testSearchWithChangeDateDescAndFilters() throws Exception {
    Account acct = createTestAccount("test542034");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

    // Create messages with different subjects
    Message[] messages = createTestMessages(mbox,
        "important document",
        "meeting notes",
        "important meeting");

    // Trash all messages
    trashMessagesInOrder(acct, messages[0], messages[1], messages[2]);

    // Search trash for "important" messages with changeDateDesc sort
    Element request = createSearchRequest("important", "changeDateDesc");
    SearchResult result = executeSearch(acct, request);

    // Verify results
    assertEquals(2, result.hits.size(), "Should find 2 messages containing 'important'");

    // With changeDateDesc sort on "important" messages, we expect:
    // msg3 (important meeting - deleted last), msg1 (important document - deleted first)
    assertSearchResultsOrder(result.hits,
        messages[2].getId(), // msg3 - important meeting (deleted most recently)
        messages[0].getId()  // msg1 - important document (deleted first)
    );
  }

  /**
     * Simple data class to hold search results
     */
    private record SearchResult(List<Element> hits, String sortBy) {

  }

  /**
   * Asserts that search results are in the expected order
   */
  private void assertSearchResultsOrder(List<Element> hits, int... expectedMessageIds) throws ServiceException {
    assertEquals(expectedMessageIds.length, hits.size(),
        "Number of hits should match expected count");

    for (int i = 0; i < expectedMessageIds.length; i++) {
      assertEquals(expectedMessageIds[i], hits.get(i).getAttributeInt(MailConstants.A_ID),
          String.format("Result at position %d should have message id %d", i, expectedMessageIds[i]));
    }
  }
}
