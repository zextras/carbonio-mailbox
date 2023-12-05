// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.AutoCompleteResponse;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.type.ContactSpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("api")
public class AutoCompleteTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;
  private static AccountAction.Factory accountActionFactory;
  public String testName;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
    accountActionFactory = new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
  }
  
  @Test
  void test3951() throws Exception {
    Account account = accountCreatorFactory.get().create();
    Element request = new Element.XMLElement(MailConstants.AUTO_COMPLETE_REQUEST);
    request.addAttribute("name", " ");
    final HttpResponse response = getSoapClient().newRequest().setCaller(account).setSoapBody(request)
        .execute();
    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
        response.getStatusLine().getStatusCode());
    Assertions.assertTrue(new String(response.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8).contains("invalid request: name parameter is empty"));
  }

  @Test
  @DisplayName("Account 1, without READ permission on ROOT, requests account 2 contacts, get 500 permission denied.")
  void shouldThrowCannotAccessAccountFolderIfNoReadGrant() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    accountActionFactory.forAccount(account2).grantFolderRightTo(account1, "r",
        Mailbox.ID_FOLDER_CALENDAR);
    getSoapClient().executeSoap(account2, new CreateContactRequest(
        new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));
    getSoapClient().executeSoap(account2, new CreateContactRequest(
        new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com")));
    final Element request = JaxbUtil.jaxbToElement(new AutoCompleteRequest(prefix));
    final HttpResponse execute = getSoapClient().newRequest()
        .setCaller(account1).setRequestedAccount(account2).setSoapBody(request).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
        execute.getStatusLine().getStatusCode());
    Assertions.assertTrue(
        responseBody.contains("Permission denied: cannot access requested folder"));
  }

  @Test
  @DisplayName("Account 1, without ANY Grants, executes Autocomplete requesting account 2, get 500 permission denied.")
  void shouldThrowCannotAccessAccountIfNoGrantsGiven() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    final HttpResponse execute = getSoapClient().newRequest().setCaller(account1)
        .setRequestedAccount(account2).setSoapBody(new AutoCompleteRequest(prefix)).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
        execute.getStatusLine().getStatusCode());
    Assertions.assertTrue(
        responseBody.contains("permission denied: can not access account " + account2.getId()));
  }

  @Test
  @DisplayName("Account 1, with READ Grants on root, executes Autocomplete requesting account 2, gets contacts.")
  void shouldGetContactsOfSharedAccount() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    accountActionFactory.forAccount(account2)
        .grantFolderRightTo(account1, "r", Mailbox.ID_FOLDER_ROOT);
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    final HttpResponse execute = getSoapClient().newRequest().setCaller(account1)
        .setRequestedAccount(account2).setSoapBody(new AutoCompleteRequest(prefix)).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    final AutoCompleteResponse autoCompleteResponse = JaxbUtil.elementToJaxb(
        Element.parseXML(responseBody).getElement("Body").getElement(
            MailConstants.AUTO_COMPLETE_RESPONSE), AutoCompleteResponse.class);
    Assertions.assertEquals(2, autoCompleteResponse.getMatches().size());
    Assertions.assertEquals(HttpStatus.SC_OK, execute.getStatusLine().getStatusCode());
  }

  @Test
  @DisplayName("Account 1, with READ Grants on Contacts and Emailed Contacts, requests autocomplete on account2 7 and 13 folder, gets contacts.")
  void shouldGetContactsOfSharedAccountWhenSettingFolders() throws Exception {
    final String prefix = "test-";
    final Account account1 = accountCreatorFactory.get().create();
    final Account account2 = accountCreatorFactory.get().create();
    accountActionFactory.forAccount(account2)
        .grantFolderRightTo(account1, "r", Mailbox.ID_FOLDER_ROOT);
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();
    getSoapClient().newRequest()
        .setCaller(account2).setSoapBody(new CreateContactRequest(
            new ContactSpec().addEmail(prefix + UUID.randomUUID() + "something.com"))).execute();

    final AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(prefix);
    autoCompleteRequest.setFolderList(
        Mailbox.ID_FOLDER_AUTO_CONTACTS + "," + Mailbox.ID_FOLDER_CONTACTS);
    final HttpResponse execute = getSoapClient().newRequest().setCaller(account1)
        .setRequestedAccount(account2).setSoapBody(autoCompleteRequest).execute();
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    final AutoCompleteResponse autoCompleteResponse = JaxbUtil.elementToJaxb(
        Element.parseXML(responseBody).getElement("Body").getElement(
            MailConstants.AUTO_COMPLETE_RESPONSE), AutoCompleteResponse.class);
    Assertions.assertEquals(3, autoCompleteResponse.getMatches().size());
    Assertions.assertEquals(HttpStatus.SC_OK, execute.getStatusLine().getStatusCode());
  }

}
