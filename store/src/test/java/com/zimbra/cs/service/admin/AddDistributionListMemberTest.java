package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
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
import com.zimbra.soap.admin.message.AddDistributionListMemberRequest;
import com.zimbra.soap.admin.message.ModifyAccountRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag("api")
class AddDistributionListMemberTest extends SoapTestSuite {

  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() {
    provisioning = Provisioning.getInstance();
    
  }

  @Test
  void addDistributionListMember() throws Exception {
    final Account domainAdminAccount = createAccount()
        .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
    final Account userAccount = createAccount().create();
    final Domain target = provisioning.getDomain(domainAdminAccount);

    final Set<ZimbraACE> aces = new HashSet<>();
    aces.add(new ZimbraACE(
                  domainAdminAccount.getId(),
                  GranteeType.GT_USER,
                  RightManager.getInstance().getRight(Right.RT_domainAdminRights),
                  RightModifier.RM_CAN_DELEGATE,
                  null));
    ACLUtil.grantRight(provisioning, target, aces);

    var dl = provisioning.createDistributionList("admin-group-dl@" + getDefaultDomainName(), new HashMap<>(
            Map.of(ZAttrProvisioning.A_zimbraIsAdminGroup, "TRUE")
    ));

    var request = new AddDistributionListMemberRequest(
            dl.getId(),
            List.of(userAccount.getId())
    );

    final SoapResponse response = getSoapClient().newRequest()
            .setCaller(domainAdminAccount)
            .setSoapBody(request)
            .execute();

    Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());
  }
}