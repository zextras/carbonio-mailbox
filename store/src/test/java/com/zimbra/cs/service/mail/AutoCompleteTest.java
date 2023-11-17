// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static com.zextras.mailbox.util.MailboxTestUtil.SERVER_NAME;

import com.zextras.mailbox.util.JettyServerFactory;
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
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.AutoCompleteResponse;
import com.zimbra.soap.mail.message.CreateContactRequest;
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
import org.junit.jupiter.api.Test;



public class AutoCompleteTest {

  private static Server server;
  private static SoapClient soapClient;
  private static AccountCreator.Factory accountCreatorFactory;
  private static AccountAction.Factory accountActionFactory;
  private static final int PORT = 8090;
    public String testName;

  @BeforeAll
  static void beforeAll() throws Exception {
    com.zextras.mailbox.util.MailboxTestUtil.setUp();
    Provisioning provisioning = Provisioning.getInstance();
    soapClient = new SoapClient();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
    accountActionFactory = new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
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

    final ServletHolder firstServlet = new ServletHolder(FirstServlet.class);
    firstServlet.setInitOrder(1);
    final ServletHolder soapServlet = new ServletHolder(SoapServlet.class);
    soapServlet.setInitParameter("engine.handler.0", "com.zimbra.cs.service.mail.MailService");
    soapServlet.setInitOrder(2);
    server = new JettyServerFactory().withPort(PORT)
        .addServlet( "/firstServlet", firstServlet)
        .addServlet(AccountConstants.USER_SERVICE_URI + "*", soapServlet).create();
    server.start();
  }

  @AfterAll
  static void afterAll() throws Exception {
    server.stop();
    com.zextras.mailbox.util.MailboxTestUtil.tearDown();
  }

 @Test
 void test3951() throws Exception {
  Account account = accountCreatorFactory.get().create();
  Element request = new Element.XMLElement(MailConstants.AUTO_COMPLETE_REQUEST);
  request.addAttribute("name", " ");
   final HttpResponse response = soapClient.newRequest().setCaller(account).setSoapBody(request).execute();
   Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
   Assertions.assertTrue(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8).contains("invalid request: name parameter is empty"));
 }

  @Test
  @DisplayName("Account 1, without READ permission on ROOT, requests account 2 contacts, get 500 permission denied.")
  void shouldThrowCannotAccessAccountFolderIfNoReadGrant() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    accountActionFactory.forAccount(account2).grantFolderRightTo(account1, "r",
        Mailbox.ID_FOLDER_CALENDAR);
    soapClient.executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));
    soapClient.executeSoap(account2, new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));
    final Element request = JaxbUtil.jaxbToElement(new AutoCompleteRequest(prefix));
    final HttpResponse execute = soapClient.newRequest()
        .setCaller(account1).setRequestedAccount(account2).setSoapBody(request).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, execute.getStatusLine().getStatusCode());
    Assertions.assertTrue(responseBody.contains("Permission denied: cannot access requested folder"));
  }

  @Test
  @DisplayName("Account 1, without ANY Grants, executes Autocomplete requesting account 2, get 500 permission denied.")
  void shouldThrowCannotAccessAccountIfNoGrantsGiven() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    soapClient.newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    soapClient.newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    final Element request = JaxbUtil.jaxbToElement(new AutoCompleteRequest(prefix));
    final HttpResponse execute = soapClient.newRequest().setCaller(account1).setRequestedAccount(account2).setSoapBody(request).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, execute.getStatusLine().getStatusCode());
    Assertions.assertTrue(responseBody.contains("permission denied: can not access account " + account2.getId()));
  }

  @Test
  @DisplayName("Account 1, with READ Grants on root, executes Autocomplete requesting account 2, gets contacts.")
  void shouldGetContactsOfSharedAccount() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    accountActionFactory.forAccount(account2).grantFolderRightTo(account1, "r", Mailbox.ID_FOLDER_ROOT);
    soapClient.newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    soapClient.newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    final Element request = JaxbUtil.jaxbToElement(new AutoCompleteRequest(prefix));
    final HttpResponse execute = soapClient.newRequest().setCaller(account1).setRequestedAccount(account2).setSoapBody(request).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    final AutoCompleteResponse autoCompleteResponse = JaxbUtil.elementToJaxb(Element.parseXML(responseBody).getElement("Body").getElement(
        MailConstants.AUTO_COMPLETE_RESPONSE), AutoCompleteResponse.class);
    Assertions.assertEquals(2, autoCompleteResponse.getMatches().size());
    Assertions.assertEquals(HttpStatus.SC_OK, execute.getStatusLine().getStatusCode());
  }

}
