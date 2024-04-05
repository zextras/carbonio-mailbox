// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.ContactRankings;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
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
    String domain = "something.com";
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
    String domain = "something.com";
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
    String domain = "something.com";

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
  void should_return_matches_without_duplicates_ordered_by_ranking_and_alphabetically_when_matches_have_same_ranking()
      throws Exception {
    String searchTerm = "fac";
    String domain = "something.com";

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
    String domain = "something.com";

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
    Account account = accountCreatorFactory.get().create();
    for (String contactEmail : emails) {
      getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(contactEmail)));
    }
    return account;
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

