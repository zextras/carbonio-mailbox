// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
    String email1 = searchTerm + UUID.randomUUID() + "_email1@something.com";
    String email2 = searchTerm + UUID.randomUUID() + "_email2@something.com";
    String email3 = searchTerm + UUID.randomUUID() + "_email3@something.com";
    String email4 = searchTerm + UUID.randomUUID() + "_email4@something.com";
    String email5 = searchTerm + UUID.randomUUID() + "_email5@something.com";

    // create accounts
    Account account = accountCreatorFactory.get().withUsername(searchTerm + "user1-" + UUID.randomUUID()).create();
    Account account2 = accountCreatorFactory.get().withUsername(searchTerm + "user2-" + UUID.randomUUID()).create();
    Account account3 = accountCreatorFactory.get().withUsername(searchTerm + "user3-" + UUID.randomUUID()).create();

    // create contacts account
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email1)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email2)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email3)));

    // create contacts account2
    getSoapClient().executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(email1)));
    getSoapClient().executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(email2)));
    getSoapClient().executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(email3)));

    // create contacts account3
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(email1)));
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(email2)));
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(email3)));
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(email4)));
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(email5)));

    // share accounts with primary account (account)
    accountActionFactory.forAccount(account2).shareWith(account);
    accountActionFactory.forAccount(account3).shareWith(account);

    // increment the ranking
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email1)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email1)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email3)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email3)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email3)));

    ContactRankings.increment(account2.getId(), Collections.singleton(new InternetAddress(email2)));
    ContactRankings.increment(account2.getId(), Collections.singleton(new InternetAddress(email2)));
    ContactRankings.increment(account2.getId(), Collections.singleton(new InternetAddress(email2)));
    ContactRankings.increment(account2.getId(), Collections.singleton(new InternetAddress(email3)));
    ContactRankings.increment(account2.getId(), Collections.singleton(new InternetAddress(email3)));

    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email1)));
    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email1)));
    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email4)));
    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email4)));
    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email4)));
    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email4)));

    FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(
        new AutoCompleteRequest(searchTerm));
    fullAutocompleteRequest.setOrderedAccountIds(account.getId() + "," + account2.getId() + "," + account3.getId());

    Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    String responseBody = new String(execute.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

    Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);

    assertEquals(5, fullAutocompleteResponse.getMatches().size());

    List<Integer> expectedRanking = List.of(3, 2, 0, 4, 0);
    List<String> expectedMatchedEmailAddresses = List.of(email3, email1, email2, email4, email5);
    for (int i = 0; i < fullAutocompleteResponse.getMatches().size(); i++) {
      AutoCompleteMatch autoCompleteMatch = fullAutocompleteResponse.getMatches().get(i);
      assertEquals(expectedRanking.get(i), autoCompleteMatch.getRanking());
      assertEquals("<" + expectedMatchedEmailAddresses.get(i) + ">", autoCompleteMatch.getEmail());
    }
  }

  @Test
  void should_order_relevant_matches_by_ranking_and_alphabetically_when_matches_have_same_ranking() throws Exception {
    String searchTerm = "test";
    String email1 = searchTerm + "_email1@something.com";
    String email2 = searchTerm + "_email2@something.com";
    String email3 = searchTerm + "_email3@something.com";
    String email4 = searchTerm + "_email4@something.com";
    String email5 = searchTerm + "_email5@something.com";
    String email6 = searchTerm + "_email6@something.com";
    String email7 = searchTerm + "_email7@something.com";
    String email8 = UUID.randomUUID() + "_email8@something.com";

    // create accounts
    Account account = accountCreatorFactory.get()
        .withUsername(searchTerm + "user1-" + UUID.randomUUID()).create();
    Account account2 = accountCreatorFactory.get()
        .withUsername(searchTerm + "user2-" + UUID.randomUUID()).create();
    Account account3 = accountCreatorFactory.get()
        .withUsername(searchTerm + "user3-" + UUID.randomUUID()).create();

    // create contacts for account
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email1)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email2)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email3)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email4)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(email8)));

    // create contacts for account2
    getSoapClient().executeSoap(account2,
        new CreateContactRequest(new ContactSpec().addEmail(email5)));

    // create contacts for account3
    getSoapClient().executeSoap(account3,
        new CreateContactRequest(new ContactSpec().addEmail(email6)));
    getSoapClient().executeSoap(account3,
        new CreateContactRequest(new ContactSpec().addEmail(email7)));

    // share accounts with primary account (account)
    accountActionFactory.forAccount(account2).shareWith(account);
    accountActionFactory.forAccount(account3).shareWith(account);

    // increment the ranking
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email1)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email1)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email3)));
    ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress(email3)));

    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email6)));
    ContactRankings.increment(account3.getId(), Collections.singleton(new InternetAddress(email6)));

    FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(
        new AutoCompleteRequest(searchTerm));
    fullAutocompleteRequest.setOrderedAccountIds(
        account.getId() + "," + account2.getId() + "," + account3.getId());

    Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request)
        .execute();
    String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);

    Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);
    System.out.println("responseBody = " + responseBody);

    assertEquals(7, fullAutocompleteResponse.getMatches().size());
    List<Integer> expectedRanking = List.of(2, 2, 0, 0, 2, 0, 0);
    List<String> expectedMatchedEmailAddresses = List.of(email1, email3, email2, email4, email6, email5, email7);
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
}

