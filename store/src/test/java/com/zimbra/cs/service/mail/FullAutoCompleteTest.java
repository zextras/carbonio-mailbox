// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.ContactRankings;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.AutoCompleteMatch;
import com.zimbra.soap.mail.type.ContactSpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class FullAutoCompleteTest extends SoapTestSuite {

  private static AccountAction.Factory accountActionFactory;
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountActionFactory = new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @Test
  void shouldReturnContactsOfAuthenticatedUserOnly() throws Exception {
    String domain = "abc.com";
    String prefix = "test-";
    Account account = accountCreatorFactory.get().create();
    getSoapClient().executeSoap(account,
        new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "@" + domain)));
    getSoapClient().executeSoap(account,
        new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "@" + domain)));
    AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(prefix);
    FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(autoCompleteRequest);

    Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);
    assertEquals(2, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void shouldReturnContactsOfAuthenticatedUserRespectingCOSAutoCompleteMaxResultsLimit() throws Exception {
    String searchTerm = "test-";
    String commonMail = searchTerm + UUID.randomUUID() + "@something.com";
    Account account = accountCreatorFactory.get().withUsername(searchTerm + "user1-" + UUID.randomUUID())
        .create();
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(commonMail)));
    getSoapClient().executeSoap(account,
        new CreateContactRequest(new ContactSpec().addEmail(searchTerm + UUID.randomUUID() + "@something.com")));
    FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(
        new AutoCompleteRequest(searchTerm));
    Provisioning.getInstance().getCOS(account).setContactAutoCompleteMaxResults(1);

    Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);
    assertEquals(1, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void shouldReturnContactsOfAuthenticatedUserRespectingAccountAutoCompleteMaxResultsLimit() throws Exception {
    String searchTerm = "test-";
    String commonMail = searchTerm + UUID.randomUUID() + "@something.com";
    Account account = accountCreatorFactory.get().withUsername(searchTerm + "user1-" + UUID.randomUUID())
        .create();
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(commonMail)));
    getSoapClient().executeSoap(account,
        new CreateContactRequest(new ContactSpec().addEmail(searchTerm + UUID.randomUUID() + "@something.com")));
    FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(
        new AutoCompleteRequest(searchTerm));
    account.setContactAutoCompleteMaxResults(1);

    Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);
    assertEquals(1, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void shouldReturnContactsOfPreferredAccountAndOtherPreferredAccountsRespectingAccountAutoCompleteMaxResultsLimit()
      throws Exception {
    String searchTerm = "test-";
    String commonMail = searchTerm + UUID.randomUUID() + "@something.com";
    Account account = accountCreatorFactory.get().withUsername(searchTerm + "user1-" + UUID.randomUUID())
        .create();
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(commonMail)));

    Account account2 = accountCreatorFactory.get().withUsername(searchTerm + "user2-" + UUID.randomUUID())
        .create();
    accountActionFactory.forAccount(account2).shareWith(account);
    getSoapClient().executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(commonMail)));
    getSoapClient().executeSoap(account2,
        new CreateContactRequest(new ContactSpec().addEmail(searchTerm + UUID.randomUUID() + "@something.com")));

    Account account3 = accountCreatorFactory.get().withUsername(searchTerm + "user3-" + UUID.randomUUID())
        .create();
    accountActionFactory.forAccount(account3).shareWith(account);
    getSoapClient().executeSoap(account3,
        new CreateContactRequest(new ContactSpec().addEmail(searchTerm + UUID.randomUUID() + "@something.com")));
    getSoapClient().executeSoap(account3,
        new CreateContactRequest(new ContactSpec().addEmail(searchTerm + UUID.randomUUID() + "@something.com")));

    FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(
        new AutoCompleteRequest(searchTerm));
    fullAutocompleteRequest.setOrderedAccountIds(account2.getId() + "," + account3.getId());
    account.setContactAutoCompleteMaxResults(3);

    Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);

    assertEquals(3, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  void should_return_relevant_matches_ordered_by_ranking_without_duplicates() throws Exception {
    String searchTerm = "test-";
    String domain = "something.com";
    String userName = searchTerm + "_email";

    String contactEmail1 = userName + "1" + "@" + domain;
    String contactEmail2 = userName + "2" + "@" + domain;
    String contactEmail3 = userName + "3" + "@" + domain;
    String contactEmail4 = userName + "4" + "@" + domain;
    String contactEmail5 = userName + "5" + "@" + domain;

    Account account = createAccountWithContacts(contactEmail1, contactEmail2, contactEmail3);
    Account account2 = createAccountWithContacts(contactEmail1, contactEmail2, contactEmail3);
    Account account3 = createAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4,
        contactEmail5);

    shareAccountWithPrimary(account2, account);
    shareAccountWithPrimary(account3, account);

    incrementRankings(account, contactEmail1, 2);
    incrementRankings(account, contactEmail3, 3);
    incrementRankings(account2, contactEmail2, 3);
    incrementRankings(account2, contactEmail3, 2);
    incrementRankings(account3, contactEmail1, 2);
    incrementRankings(account3, contactEmail4, 4);

    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account, account,
        account2,
        account3);

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
  void should_order_relevant_matches_by_ranking_and_alphabetically_when_matches_have_same_ranking() throws Exception {
    String searchTerm = "test";
    String domain = "something.com";
    String userName = searchTerm + "_email";

    String contactEmail1 = userName + "1" + "@" + domain;
    String contactEmail2 = userName + "2" + "@" + domain;
    String contactEmail3 = userName + "3" + "@" + domain;
    String contactEmail4 = userName + "4" + "@" + domain;
    String contactEmail5 = userName + "5" + "@" + domain;
    String contactEmail6 = userName + "6" + "@" + domain;
    String contactEmail7 = userName + "7" + "@" + domain;

    Account account = createAccountWithContacts(contactEmail1, contactEmail2, contactEmail3, contactEmail4);
    Account account2 = createAccountWithContacts(contactEmail5);
    Account account3 = createAccountWithContacts(contactEmail6, contactEmail7);

    shareAccountWithPrimary(account2, account);
    shareAccountWithPrimary(account3, account);

    incrementRankings(account, contactEmail1, 2);
    incrementRankings(account, contactEmail3, 2);
    incrementRankings(account3, contactEmail6, 2);

    FullAutocompleteResponse fullAutocompleteResponse = performFullAutocompleteRequest(searchTerm, account, account,
        account2,
        account3);

    assertEquals(7, fullAutocompleteResponse.getMatches().size());
    List<Integer> expectedRanking = List.of(2, 2, 0, 0, 2, 0, 0);
    List<String> expectedMatchedEmailAddresses = Arrays.asList(contactEmail1, contactEmail3, contactEmail2,
        contactEmail4, contactEmail6,
        contactEmail5, contactEmail7);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  @Disabled("Behavior needed to be discussed")
  void should_do_something_when_request_misses_OrderedAccountIds() {
    // should return AutoComplete matches from authenticated
  }


  private Account createAccountWithContacts(String... emails) throws Exception {
    Account account = accountCreatorFactory.get().withUsername("user-" + UUID.randomUUID()).create();
    for (String contactEmail : emails) {
      getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(contactEmail)));
    }
    return account;
  }

  private void shareAccountWithPrimary(Account accountToShare, Account primaryAccount) throws ServiceException {
    accountActionFactory.forAccount(accountToShare).shareWith(primaryAccount);
  }

  private void incrementRankings(Account account, String email, int times) throws AddressException, ServiceException {
    for (int i = 0; i < times; i++) {
      ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email)));
    }
  }

  private FullAutocompleteResponse performFullAutocompleteRequest(String searchTerm, Account authenticatorAccount,
      Account... orderedAccountIds)
      throws Exception {
    FullAutocompleteRequest request = new FullAutocompleteRequest(new AutoCompleteRequest(searchTerm));
    String orderedAccountIdsStr = Arrays.stream(orderedAccountIds).map(Account::getId).collect(Collectors.joining(","));
    request.setOrderedAccountIds(orderedAccountIdsStr);
    Element requestElement = JaxbUtil.jaxbToElement(request);
    HttpResponse response = getSoapClient().newRequest().setCaller(authenticatorAccount).setSoapBody(requestElement)
        .execute();
    String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

    System.out.println("responseBody = " + responseBody);

    Element rootElement = Element.parseXML(responseBody).getElement("Body")
        .getElement(MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    return JaxbUtil.elementToJaxb(rootElement, FullAutocompleteResponse.class);
  }
}

