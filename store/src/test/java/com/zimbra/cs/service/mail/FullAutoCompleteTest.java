// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static com.zextras.mailbox.util.MailboxTestUtil.SERVER_NAME;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.ContactSpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class FullAutoCompleteTest {

  private static Server server;
  private static SoapClient soapClient;
  private static AccountAction.Factory accountActionFactory;
  private static AccountCreator.Factory accountCreatorFactory;
  private static final int PORT = 8090;

  @BeforeAll
  static void beforeAll() throws Exception {
    MailboxTestUtil.setUp();
    Provisioning provisioning = Provisioning.getInstance();
    accountActionFactory = new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
    soapClient = new SoapClient();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
    provisioning.getServerByName(SERVER_NAME).modify(
        new HashMap<>(
            Map.of(
                Provisioning.A_zimbraMailPort, String.valueOf(PORT),
                ZAttrProvisioning.A_zimbraMailMode, "http",
                ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
                ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE"
            )
        )
    );

    // See web.xml, Soap servlet calls Zimbra#startup which waits for FirstServlet initialization
    // We should consider refactoring the startup process

    final ServletHolder firstServlet = new ServletHolder(FirstServlet.class);
    firstServlet.setInitOrder(1);
    final ServletHolder soapServlet = new ServletHolder(SoapServlet.class);
    soapServlet.setInitParameter("engine.handler.0", "com.zimbra.cs.service.mail.MailService");
    soapServlet.setInitOrder(2);
    server = JettyServerFactory.create(PORT,
        Map.of( "/firstServlet", firstServlet,
            AccountConstants.USER_SERVICE_URI + "*", soapServlet));
    server.start();
  }

  @AfterAll
  static void afterAll() throws Exception {
    server.stop();
    MailboxTestUtil.tearDown();
  }


  @Test
  void shouldReturnContactsOfAuthenticatedUserOnly() throws Exception {
    final String domain = "abc.com";
    final String prefix = "test-";
    final Account account = MailboxTestUtil.createRandomAccountForDefaultDomain();
    soapClient.executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "@" + domain)));
    soapClient.executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "@" + domain)));
    final AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(prefix);
    final FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(autoCompleteRequest);

    final Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    final HttpResponse execute = soapClient.newRequest().setCaller(account).setSoapBody(request).execute();
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
  @DisplayName("Account has account 2, account 3 shared with it. Execute FullAutocomplete, get also matching contacts from account2 and 3.")
  void shouldReturnContactsOfAuthenticatedUserAndRequestedAccounts() throws Exception {


    final String prefix = "test-";
    final Account account = accountCreatorFactory.get().withUsername(prefix + "user1-" + UUID.randomUUID()).create();
    soapClient.executeSoap(account, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));

    final Account account2 = accountCreatorFactory.get().withUsername(prefix + "user2-" + UUID.randomUUID()).create();
    accountActionFactory.forAccount(account2).shareWith(account);
    soapClient.executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));

    final Account account3 = accountCreatorFactory.get().withUsername(prefix + "user3-" + UUID.randomUUID()).create();
    accountActionFactory.forAccount(account3).shareWith(account);
    soapClient.executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));
    soapClient.executeSoap(account3, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));

    final AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(prefix);

    final FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(autoCompleteRequest);
    fullAutocompleteRequest.addAccount(account2.getId());
    fullAutocompleteRequest.addAccount(account3.getId());

    // make the call
    final Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    final HttpResponse execute = soapClient.newRequest().setCaller(account).setSoapBody(request).execute();
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




}