// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.ContactRankings;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.FullAutoComplete.AutoCompleteMatchElementBuilder;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.AutoCompleteMatch;
import com.zimbra.soap.mail.type.ContactSpec;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@Tag("api")
class FullAutoCompleteTest extends SoapTestSuite {

  private static AccountAction.Factory accountActionFactory;
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void beforeAll() throws Exception {
    accountActionFactory = new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
    accountCreatorFactory = new AccountCreator.Factory(Provisioning.getInstance());
  }

  private static Collection<Arguments> parsePreferredAccountsTestData() {
    return Arrays.asList(
        Arguments.of("", null, new LinkedHashSet<>()),
        Arguments.of(null, null, new LinkedHashSet<>()),
        Arguments.of("1,2,3", "1", new LinkedHashSet<>(Arrays.asList("2", "3"))),
        Arguments.of("abc,def,ghi", "abc", new LinkedHashSet<>(Arrays.asList("def", "ghi"))),
        Arguments.of("123", "123", new LinkedHashSet<>()),
        Arguments.of("123 , ,", "123", new LinkedHashSet<>()),
        Arguments.of("123 , ", "123", new LinkedHashSet<>()),
        Arguments.of(" 123, ", "123", new LinkedHashSet<>()),
        Arguments.of(",123,", "123", new LinkedHashSet<>()),
        Arguments.of("123,123,456,456,789", "123", new LinkedHashSet<>(Arrays.asList("456", "789")))
    );
  }


  @Test
  void should_return_matches_from_authenticated_account_only() throws Exception {
    String domain = "abc.com";
    String searchTerm = "fac";
    String contactEmail1 = searchTerm + UUID.randomUUID() + "@" + domain;
    String contactEmail2 = searchTerm + UUID.randomUUID() + "@" + domain;
    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2);

    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        new ArrayList<>());

    assertEquals(2, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void should_return_matches_from_authenticated_account_respecting_COS_AutoCompleteMaxResultsLimit() throws Exception {
    String searchTerm = "fac";
    final String contactEmail1 = searchTerm + UUID.randomUUID() + "@something.com";
    final String contactEmail2 = searchTerm + UUID.randomUUID() + "@something.com";
    final String contactEmail3 = searchTerm + UUID.randomUUID() + "@something.com";

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3);
    Cos cos = Provisioning.getInstance().createCos(UUID.randomUUID().toString(), Map.of());
    cos.setContactAutoCompleteMaxResults(1);
    Provisioning.getInstance().setCOS(account, cos);

    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        new ArrayList<>());

    assertEquals(1, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void should_return_matches_from_authenticated_account_respecting_account_AutoCompleteMaxResultsLimit()
      throws Exception {
    String searchTerm = "fac";
    String contactEmail1 = searchTerm + UUID.randomUUID() + "@something.com";
    String contactEmail2 = searchTerm + UUID.randomUUID() + "@something.com";

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2);

    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(contactEmail1)));
    getSoapClient().executeSoap(account,
        new CreateContactRequest(new ContactSpec().addEmail(searchTerm + UUID.randomUUID() + "@something.com")));

    account.setContactAutoCompleteMaxResults(1);

    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        new ArrayList<>());

    assertEquals(1, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void should_return_matches_from_other_preferred_accounts_respecting_account_AutoCompleteMaxResultsLimit()
      throws Exception {
    String searchTerm = "fac";
    String domain = UUID.randomUUID() + "something.com";
    String contactEmail1 = searchTerm + "email1@" + domain;
    String contactEmail2 = searchTerm + "email2@" + domain;
    String contactEmail3 = searchTerm + "email3@" + domain;
    String contactEmail4 = searchTerm + "email4@" + domain;

    Account account = createRandomAccountWithContacts(contactEmail1);
    Account account2 = createRandomAccountWithContacts(contactEmail1, contactEmail2);
    Account account3 = createRandomAccountWithContacts(contactEmail3, contactEmail4);

    shareAccountWithPrimary(account2, account);
    shareAccountWithPrimary(account3, account);

    account.setContactAutoCompleteMaxResults(3);

    ArrayList<Account> preferredAccounts = new ArrayList<>(List.of(account2, account3));
    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        preferredAccounts);

    assertEquals(3, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void should_return_matches_ordered_by_ranking_without_duplicates() throws Exception {
    String searchTerm = "fac";
    String domain = "something989.com";
    String userName = searchTerm + UUID.randomUUID();
    String contactEmail1 = userName + "_email1@" + domain;
    String contactEmail2 = userName + "_email2@" + domain;
    String contactEmail3 = userName + "_email3@" + domain;
    String contactEmail4 = userName + "_email4@" + domain;
    String contactEmail5 = userName + "_email5@" + domain;

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3);
    Account account2 = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3);
    Account account3 = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4,
        contactEmail5);

    shareAccountWithPrimary(account2, account);
    shareAccountWithPrimary(account3, account);

    incrementRankings(account, contactEmail1, 2);
    incrementRankings(account, contactEmail3, 3);
    incrementRankings(account2, contactEmail2, 3);
    incrementRankings(account2, contactEmail3, 2);
    incrementRankings(account3, contactEmail1, 2);
    incrementRankings(account3, contactEmail4, 4);

    ArrayList<Account> preferredAccounts = new ArrayList<>(List.of(account, account2, account3));
    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        preferredAccounts);

    assertEquals(5, fullAutocompleteResponse.getMatches().size());

    List<Integer> expectedRanking = List.of(3, 2, 0, 4, 0);
    List<String> expectedMatchedEmailAddresses = List.of(contactEmail3, contactEmail1, contactEmail2, contactEmail4,
        contactEmail5, contactEmail5);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  void should_return_matches()
      throws Exception {
    String searchTerm = "fac";
    String domain = "something123.com";

    String contactEmail1 = searchTerm + "_email1@" + domain;
    String contactEmail2 = searchTerm + "_email2@" + domain;
    String contactEmail3 = searchTerm + "_email3@" + domain;
    String contactEmail4 = searchTerm + "_email4@" + domain;

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4);
    Account account2 = createRandomAccountWithContacts(contactEmail1, contactEmail4);

    shareAccountWithPrimary(account2, account);

    incrementRankings(account2, contactEmail1, 2);
    incrementRankings(account2, contactEmail4, 2);

    incrementRankings(account, contactEmail1, 3);
    incrementRankings(account, contactEmail2, 6);
    incrementRankings(account, contactEmail3, 3);
    incrementRankings(account, contactEmail4, 6);

    ArrayList<Account> preferredAccounts = new ArrayList<>(List.of(account2, account));
    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        preferredAccounts);

    assertEquals(4, fullAutocompleteResponse.getMatches().size());
    List<Integer> expectedRanking = List.of(2, 2, 6, 3);
    List<String> expectedMatchedEmailAddresses = Arrays.asList(contactEmail1, contactEmail4, contactEmail2,
        contactEmail3);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  void should_proxy_autocomplete_request_to_server_if_requested_account_is_not_local()
      throws Exception {
    String searchTerm = "fac";

    Account account = accountCreatorFactory.get().create();
    Server server = Provisioning.getInstance().createServer(UUID.randomUUID() + ".com", new HashMap<>());
    server.setServiceEnabled(new String[]{"service"});
    Account account2 = accountCreatorFactory.get().withAttribute(Provisioning.A_zimbraMailHost, server.getHostname())
        .create();

    shareAccountWithPrimary(account2, account);
    ArrayList<Account> preferredAccounts = new ArrayList<>(List.of(account2, account));

    String orderedAccountIdsStr = preferredAccounts.stream().map(Account::getId).collect(Collectors.joining(","));
    FullAutocompleteRequest request = new FullAutocompleteRequest(new AutoCompleteRequest(searchTerm));
    request.setOrderedAccountIds(orderedAccountIdsStr);

    FullAutoComplete fullAutoComplete = Mockito.spy(FullAutoComplete.class);
    JaxbUtil.elementToJaxb(
        fullAutoComplete.handle(JaxbUtil.jaxbToElement(request),
            ServiceTestUtil.getRequestContext(account)));

    verify(fullAutoComplete, times(1)).proxyRequestInternal(Mockito.any(), Mockito.any(), Mockito.anyMap());
  }

  @Test
  void should_return_matches_without_duplicates_ordered_by_ranking_and_alphabetically_when_matches_have_same_ranking()
      throws Exception {
    String searchTerm = "fac";
    String domain = UUID.randomUUID() + "something.com";

    String contactEmail1 = searchTerm + "_email1@" + domain;
    String contactEmail2 = searchTerm + "_email2@" + domain;
    String contactEmail3 = searchTerm + "_email3@" + domain;
    String contactEmail4 = searchTerm + "_email4@" + domain;
    String contactEmail5 = searchTerm + "_email5@" + domain;
    String contactEmail6 = searchTerm + "_email6@" + domain;
    String contactEmail7 = searchTerm + "_email7@" + domain;

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4);
    Account account2 = createRandomAccountWithContacts(contactEmail5);
    Account account3 = createRandomAccountWithContacts(contactEmail5, contactEmail6, contactEmail7);

    shareAccountWithPrimary(account2, account);
    shareAccountWithPrimary(account3, account);

    incrementRankings(account, contactEmail1, 2);
    incrementRankings(account2, contactEmail5, 1);
    incrementRankings(account3, contactEmail5, 4);
    incrementRankings(account, contactEmail3, 2);
    incrementRankings(account3, contactEmail6, 2);

    ArrayList<Account> preferredAccounts = new ArrayList<>(List.of(account, account2, account3));
    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        preferredAccounts);

    assertEquals(7, fullAutocompleteResponse.getMatches().size());
    List<Integer> expectedRanking = List.of(2, 2, 0, 0, 4, 2, 0);
    List<String> expectedMatchedEmailAddresses = Arrays.asList(contactEmail1, contactEmail3, contactEmail2,
        contactEmail4, contactEmail5, contactEmail6, contactEmail7);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  void should_return_matches_from_authenticated_account_when_request_misses_OrderedAccountIds()
      throws Exception {
    String searchTerm = "fac";
    String domain = UUID.randomUUID() + "something.com";

    String contactEmail1 = searchTerm + "_email1@" + domain;
    String contactEmail2 = searchTerm + "_email2@" + domain;
    String contactEmail3 = searchTerm + "_email3@" + domain;
    String contactEmail4 = searchTerm + "_email4@" + domain;

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4);

    incrementRankings(account, contactEmail1, 2);
    incrementRankings(account, contactEmail3, 2);

    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        new ArrayList<>());

    assertEquals(4, fullAutocompleteResponse.getMatches().size());
    List<Integer> expectedRanking = List.of(2, 2, 0, 0, 2, 0, 0);
    List<String> expectedMatchedEmailAddresses = Arrays.asList(contactEmail1, contactEmail3, contactEmail2,
        contactEmail4);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  void should_return_matches_from_other_accounts_when_requested_account_not_found()
      throws Exception {
    String searchTerm = "fac";
    String domain = UUID.randomUUID() + "something.com";

    String contactEmail1 = searchTerm + "_email1@" + domain;
    String contactEmail2 = searchTerm + "_email2@" + domain;
    String contactEmail3 = searchTerm + "_email3@" + domain;
    String contactEmail4 = searchTerm + "_email4@" + domain;

    Account account = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4);
    Account account2 = createRandomAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4);

    shareAccountWithPrimary(account2, account);

    incrementRankings(account2, contactEmail1, 2);
    incrementRankings(account2, contactEmail3, 2);

    // create preferred account and delete it to simulate the corner case
    var preferredAccount = createRandomAccountWithContacts();
    Provisioning.getInstance().deleteAccount(preferredAccount.getId());

    var orderedAccounts = new ArrayList<Account>();
    orderedAccounts.add(preferredAccount);
    orderedAccounts.add(account2);
    var fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account, orderedAccounts);

    assertEquals(4, fullAutocompleteResponse.getMatches().size());
    List<Integer> expectedRanking = List.of(2, 2, 0, 0, 2, 0, 0);
    List<String> expectedMatchedEmailAddresses = Arrays.asList(contactEmail1, contactEmail3, contactEmail2,
        contactEmail4);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  void should_return_contact_group_when_matches() throws Exception {
    String searchTerm = "my";
    String domain = UUID.randomUUID() + "something.com";

    String contactEmail1 = searchTerm + "email1@" + domain;
    String contactEmail2 = searchTerm + "email2@" + domain;
    String contactEmail3 = searchTerm + "email3@" + domain;
    String contactEmail4 = searchTerm + "email4@" + domain;

    Account account = createRandomAccountWithContactGroup(searchTerm + "ContactGroup1", contactEmail1);
    Account account2 = createRandomAccountWithContactGroup(searchTerm + "ContactGroup2", contactEmail1, contactEmail2);
    createContactGroupForAccount(account2, searchTerm + "ContactGroup1", new String[]{contactEmail1});
    Account account3 = createRandomAccountWithContactGroup(searchTerm + "ContactGroup3", contactEmail3, contactEmail4);
    Account account4 = createRandomAccountWithContacts(contactEmail1, contactEmail4);

    incrementRankings(account2, contactEmail2, 20);
    incrementRankings(account4, contactEmail1, 10);

    shareAccountWithPrimary(account2, account);
    shareAccountWithPrimary(account3, account);
    shareAccountWithPrimary(account4, account);

    var preferredAccounts = new ArrayList<>(List.of(account, account2, account3, account4));
    var fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account,
        preferredAccounts);

    List<Integer> expectedRanking = List.of(0, 10, 0, 0, 0, 0);
    List<String> expectedMatchedEmailAddresses = Arrays.asList(null, contactEmail1, null, null, null, contactEmail4);

    assertEquals(6, fullAutocompleteResponse.getMatches().size());
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      var autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      if (autoCompleteMatch.getGroup()) {
        assertEquals(0, autoCompleteMatch.getRanking());
        assertNull(autoCompleteMatch.getEmail());
      } else {
        assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
      }
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
    }
  }

  @Test
  void autoCompleteMatchElementBuilder_should_omit_attributes_if_passed_values_are_null() throws Exception {
    var searchTerm = "my";
    var domain = UUID.randomUUID() + "something.com";
    var contactEmail1 = searchTerm + "email1@" + domain;

    var account = createRandomAccountWithContacts(contactEmail1);
    var zsc = MailDocumentHandler.getZimbraSoapContext(ServiceTestUtil.getRequestContext(account));

    var autoCompleteMatch = new AutoCompleteMatch();
    autoCompleteMatch.setRanking(10);
    autoCompleteMatch.setCompany("HellYeahInc");

    var matchElementBuilder = new AutoCompleteMatchElementBuilder(zsc);
    matchElementBuilder.setIntegerAttribute(MailConstants.A_RANKING, autoCompleteMatch.getRanking(), 0);
    matchElementBuilder.setStringAttribute(MailConstants.A_COMPANY, autoCompleteMatch.getCompany(), null);
    matchElementBuilder.setBooleanAttribute(MailConstants.A_IS_GROUP, autoCompleteMatch.getGroup(), Boolean.FALSE);

    matchElementBuilder.setStringAttribute(MailConstants.A_LASTNAME, autoCompleteMatch.getLastName(), null);
    matchElementBuilder.setBooleanAttribute(MailConstants.A_EXP, autoCompleteMatch.getCanExpandGroupMembers(), null);

    var matchElement = matchElementBuilder.build();

    assertEquals("<match ranking=\"10\" company=\"HellYeahInc\" isGroup=\"0\"/>", matchElement.toString(),
        "if the passed values (value and default value) are null, the attribute should be omitted");
  }

  @Test
  void autoCompleteMatchElementBuilder_should_add_attributes_with_defaultValues_if_passed_values_are_null()
      throws Exception {
    var searchTerm = "my";
    var domain = UUID.randomUUID() + "something.com";
    var contactEmail1 = searchTerm + "email1@" + domain;

    var account = createRandomAccountWithContacts(contactEmail1);
    var zsc = MailDocumentHandler.getZimbraSoapContext(ServiceTestUtil.getRequestContext(account));

    var autoCompleteMatch = new AutoCompleteMatch();
    var matchElementBuilder = new AutoCompleteMatchElementBuilder(zsc);

    matchElementBuilder.setIntegerAttribute(MailConstants.A_RANKING, autoCompleteMatch.getRanking(), 20);
    matchElementBuilder.setBooleanAttribute(MailConstants.A_IS_GROUP, autoCompleteMatch.getGroup(), Boolean.TRUE);
    matchElementBuilder.setStringAttribute(MailConstants.A_LASTNAME, autoCompleteMatch.getLastName(), "myLastName");

    var matchElement2 = matchElementBuilder.build();

    assertEquals("<match last=\"myLastName\" ranking=\"20\" isGroup=\"1\"/>",
        matchElement2.toString(),
        "if the passed value is null and defaultValue is valid, the attribute should be added with the defaultValue passed");
  }

  @Test
  void should_throw_exception_when_request_element_cannot_be_converted_to_fac_request_object() throws Exception {
    var account = accountCreatorFactory.get().create();

    var mockElement = mock(Element.class);
    when(JaxbUtil.elementToJaxb(mockElement)).thenReturn(null);

    var fullAutoComplete = Mockito.spy(FullAutoComplete.class);
    var requestContext = ServiceTestUtil.getRequestContext(account);

    var serviceException = assertThrows(ServiceException.class,
        () -> fullAutoComplete.handle(mockElement, requestContext));

    assertEquals(ServiceException.FAILURE, serviceException.getCode());
  }

  @ParameterizedTest
  @MethodSource("parsePreferredAccountsTestData")
  void testParsePreferredAccountsFrom(String input, String expectedPreferredAccount,
      LinkedHashSet<String> expectedOtherAccounts) {
    FullAutoComplete fullAutoComplete = new FullAutoComplete();
    Tuple2<String, LinkedHashSet<String>> result = fullAutoComplete.parsePreferredAccountsFrom(input);
    assertEquals(expectedPreferredAccount, result._1());
    assertEquals(expectedOtherAccounts, result._2());
  }

  private Account createRandomAccountWithContacts(String... emails) throws Exception {
    var account = accountCreatorFactory.get().create();
    for (String contactEmail : emails) {
      var response = getSoapClient().executeSoap(account,
          new CreateContactRequest(new ContactSpec().addEmail(contactEmail)));
      assertEquals(200, response.getStatusLine().getStatusCode());
    }
    return account;
  }

  private Element createAttributeElement(String attributeName, String attributeText) {
    var attributeElement = new Element.XMLElement(MailConstants.E_A);
    attributeElement.addAttribute(MailConstants.A_ATTRIBUTE_NAME, attributeName).setText(attributeText);
    return attributeElement;
  }

  private Account createRandomAccountWithContactGroup(String contactGroupName,
      String... membersEmailsForContactGroup) throws Exception {
    var account = accountCreatorFactory.get().create();
    createContactGroupForAccount(account, contactGroupName, membersEmailsForContactGroup);
    return account;
  }

  private void createContactGroupForAccount(Account account, String contactGroupName,
      String[] membersEmailsForContactGroup)
      throws Exception {
    var createContactRequest = new Element.XMLElement(MailConstants.CREATE_CONTACT_REQUEST);

    var contactSpecElement = new Element.XMLElement(MailConstants.E_CONTACT);
    contactSpecElement.addNonUniqueElement(
        createAttributeElement(ContactConstants.A_type, ContactConstants.TYPE_GROUP));
    contactSpecElement.addNonUniqueElement(createAttributeElement(ContactConstants.A_fullName, contactGroupName));
    contactSpecElement.addNonUniqueElement(createAttributeElement(ContactConstants.A_nickname, contactGroupName));
    contactSpecElement.addNonUniqueElement(createAttributeElement(ContactConstants.A_fileAs, "8:" + contactGroupName));

    for (String contactEmail : membersEmailsForContactGroup) {
      var member = new Element.XMLElement(MailConstants.E_CONTACT_GROUP_MEMBER);
      member.addAttribute(ContactConstants.A_type, ContactConstants.GROUP_MEMBER_TYPE_INLINE);
      member.addAttribute(MailConstants.A_CONTACT_GROUP_MEMBER_VALUE, contactEmail);
      contactSpecElement.addNonUniqueElement(member);
    }

    createContactRequest.addNonUniqueElement(contactSpecElement);
    var response = getSoapClient().executeSoap(account, createContactRequest);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }


  private void shareAccountWithPrimary(Account accountToShare, Account primaryAccount) {
    try {
      Mailbox mailboxOfTargetAccount = MailboxManager.getInstance().getMailboxByAccount(accountToShare);
      int attempts = 0;
      while (!mailboxOfTargetAccount.canAccessFolder(new OperationContext(primaryAccount),
          Mailbox.ID_FOLDER_USER_ROOT) && attempts < 5) {
        accountActionFactory.forAccount(accountToShare).shareWith(primaryAccount);
        attempts++;
      }
    } catch (ServiceException ignored) {
    }
  }

  private void incrementRankings(Account account, String targetEmailAddress, int times)
      throws AddressException, ServiceException {
    for (int i = 0; i < times; i++) {
      ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(targetEmailAddress)));
    }
  }

  private FullAutocompleteResponse performFullAutocompleteRequest(String searchTerm, Account authenticatorAccount,
      ArrayList<Account> orderedAccountIds) throws Exception {
    String orderedAccountIdsStr = orderedAccountIds.stream().map(Account::getId).collect(Collectors.joining(","));
    FullAutocompleteRequest request = new FullAutocompleteRequest(new AutoCompleteRequest(searchTerm));
    request.setOrderedAccountIds(orderedAccountIdsStr);

    return JaxbUtil.elementToJaxb(new FullAutoComplete().handle(JaxbUtil.jaxbToElement(request),
        ServiceTestUtil.getRequestContext(authenticatorAccount)));
  }
}

