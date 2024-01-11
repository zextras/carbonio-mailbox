// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

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
import com.zimbra.soap.admin.message.ModifyDomainRequest;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class ModifyDomainTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }


  /**
   * Tests Domain Admin modify attributes.
   * Please add any other attributes you feel like needs test (they are a lot, not all are included).
   *
   * @throws Exception
   */
  @Test
  void domainAdminShouldBeAbleToModifyDomainAttributes() throws Exception {
    final Account domainAdminAccount = accountCreatorFactory.get()
        .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
    final Domain targetDomain = provisioning.getDomain(domainAdminAccount);
    final Set<ZimbraACE> aces = new HashSet<>();
    aces.add(new ZimbraACE(
        domainAdminAccount.getId(),
        GranteeType.GT_USER,
        RightManager.getInstance().getRight(Right.RT_domainAdminRights),
        RightModifier.RM_CAN_DELEGATE,
        null));
    ACLUtil.grantRight(provisioning, targetDomain, aces);

    final ModifyDomainRequest request =
        new ModifyDomainRequest(targetDomain.getId());
    request.addAttr(ZAttrProvisioning.A_zimbraPublicServiceHostname, "myDomain1." + targetDomain.getName());
    request.addAttr(ZAttrProvisioning.A_zimbraVirtualHostname, "myDomain.test.com");
    request.addAttr(ZAttrProvisioning.A_zimbraSSLCertificate, "/tmp/myCertificate.crt");
    request.addAttr(ZAttrProvisioning.A_zimbraSSLPrivateKey, "/tmp/myKey.pub");
    request.addAttr(ZAttrProvisioning.A_zimbraPublicServicePort, "8080");

    final HttpResponse response = getSoapClient().newRequest().setCaller(domainAdminAccount)
        .setSoapBody(request).execute();
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }

}
