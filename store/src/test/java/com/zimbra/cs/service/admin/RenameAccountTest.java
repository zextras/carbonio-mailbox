package com.zimbra.cs.service.admin;

import static com.zimbra.common.util.Constants.ERROR_CODE_NO_SUCH_DOMAIN;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.message.RenameAccountRequest;
import java.util.HashMap;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class RenameAccountTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }


  @Test
  void shouldRenameAccountByChangingUsername() throws Exception {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    Account userAccount = accountCreatorFactory.get().create();

    RenameAccountRequest request =
        new RenameAccountRequest(userAccount.getId(), "newName@" + userAccount.getDomainName());
    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }

  @Test
  void shouldRenameAccountByChangingOnlyDomain() throws Exception {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    final String accountName = UUID.randomUUID().toString();
    Account userAccount = accountCreatorFactory.get().withUsername(accountName).create();
    final Domain newDomain = provisioning.createDomain("myDomain.com", new HashMap<>());

    RenameAccountRequest request =
        new RenameAccountRequest(userAccount.getId(), accountName + "@" + newDomain.getName());
    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }

  @Test
  void shouldThrowNoSuchDomainWhenRenamingToNotExistingDomain() throws Exception {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    final String accountName = UUID.randomUUID().toString();
    Account userAccount = accountCreatorFactory.get().withUsername(accountName).create();

    RenameAccountRequest request =
        new RenameAccountRequest(userAccount.getId(), accountName + "@" + UUID.randomUUID() + ".com");
    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    final String responseBody = EntityUtils.toString(response.getEntity());
    Assertions.assertTrue(responseBody.contains(ERROR_CODE_NO_SUCH_DOMAIN));
  }
}
