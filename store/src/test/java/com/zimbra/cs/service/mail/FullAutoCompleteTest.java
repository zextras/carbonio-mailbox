// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.ContactSpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    final String domain = "abc.com";
    final String prefix = "test-";
    final Account account = accountCreatorFactory.get().create();
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "@" + domain)));
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "@" + domain)));
    final AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(prefix);
    final FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(autoCompleteRequest);

    final Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    final HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    Assertions.assertEquals(HttpStatus.SC_OK, execute.getStatusLine().getStatusCode());
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    final Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    final FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);
    System.out.println("Received: " + responseBody);
    Assertions.assertEquals(2, fullAutocompleteResponse.getMatches().size());
  }

  @Test
  @DisplayName("Account has account 2, account 3 shared with it. "
      + "Execute FullAutocomplete, get also matching contacts from account2 and 3. No duplicates.")
  void shouldReturnContactsOfAuthenticatedUserAndRequestedAccounts() throws Exception {


    final String prefix = "test-";
    final String commonMail = prefix + UUID.randomUUID() + "something.com";
    final Account account = accountCreatorFactory.get().withUsername(prefix + "user1-" + UUID.randomUUID()).create();
    getSoapClient().executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(commonMail)));

    final Account account2 = accountCreatorFactory.get().withUsername(prefix + "user2-" + UUID.randomUUID()).create();
    accountActionFactory.forAccount(account2).shareWith(account);
    getSoapClient().executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(commonMail)));
    getSoapClient().executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));

    final Account account3 = accountCreatorFactory.get().withUsername(prefix + "user3-" + UUID.randomUUID()).create();
    accountActionFactory.forAccount(account3).shareWith(account);
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));
    getSoapClient().executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));

    final AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(prefix);

    final FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(autoCompleteRequest);
    fullAutocompleteRequest.addAccount(account2.getId());
    fullAutocompleteRequest.addAccount(account3.getId());

    // make the call
    final Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    final HttpResponse execute = getSoapClient().newRequest().setCaller(account).setSoapBody(request).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    System.out.println("Received: " + responseBody);
    Assertions.assertEquals(HttpStatus.SC_OK, execute.getStatusLine().getStatusCode());
    final Element rootElement = Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    final FullAutocompleteResponse fullAutocompleteResponse = JaxbUtil.elementToJaxb(rootElement,
        FullAutocompleteResponse.class);
    Assertions.assertEquals(4, fullAutocompleteResponse.getMatches().size());
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

    FullAutocompleteRequest request = new FullAutocompleteRequest(new AutoCompleteRequest(searchTerm));
    request.addAccount(account2.getId());
    FullAutoComplete fullAutoComplete = Mockito.spy(FullAutoComplete.class);
    JaxbUtil.elementToJaxb(
        fullAutoComplete.handle(JaxbUtil.jaxbToElement(request),
            ServiceTestUtil.getRequestContext(account)));

    verify(fullAutoComplete, times(1)).proxyRequestInternal(Mockito.any(), Mockito.any(), Mockito.anyMap());
  }
}