package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapExtension;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.message.RenameAccountRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("api")
class RenameAccountTest {
  private static AccountCreator.Factory accountCreatorFactory;

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension(8080, "com.zimbra.cs.service.admin.AdminService",
      AdminConstants.ADMIN_SERVICE_URI);

  @BeforeAll
  static void setUp() throws Exception {
    accountCreatorFactory = new AccountCreator.Factory(Provisioning.getInstance());
  }


  @Test
  void shouldRenameAccount() throws Exception {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    Account userAccount = accountCreatorFactory.get().create();

    RenameAccountRequest request =
        new RenameAccountRequest(userAccount.getId(), "newName@" + userAccount.getDomainName());

    final HttpResponse response =
        soapExtension.getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }
}
