package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.soap.admin.message.ModifyAccountRequest;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class ModifyAccountTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @Test
  void shouldModifyAccountWhenDomainAdminChangingMailTransport() throws Exception {
    final Account domainAdminAccount = accountCreatorFactory.get()
        .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
    final Account userAccount = accountCreatorFactory.get().create();
    final Domain target = provisioning.getDomain(domainAdminAccount);

    final Set<ZimbraACE> aces = new HashSet<>();
    aces.add(new ZimbraACE(
                  domainAdminAccount.getId(),
                  GranteeType.GT_USER,
                  RightManager.getInstance().getRight(Right.RT_domainAdminRights),
                  RightModifier.RM_CAN_DELEGATE,
                  null));
    ACLUtil.grantRight(provisioning, target, aces);

    final ModifyAccountRequest request =
        new ModifyAccountRequest(userAccount.getId());
    request.addAttr(ZAttrProvisioning.A_zimbraMailTransport, "lmtp:localhost:7025");
    final HttpResponse response = getSoapClient().newRequest().setCaller(domainAdminAccount)
        .setSoapBody(request).execute();
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }
}