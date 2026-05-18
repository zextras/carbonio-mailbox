package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
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
  void addDistributionListMemberByDomainAdmin() throws Exception {
    final Account domainAdminAccount = createAccount()
        .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
    final Account userAccount = createAccount().create();
    final Domain target = provisioning.getDomain(domainAdminAccount);

    grantRights(domainAdminAccount, Right.RT_domainAdminRights, target);

    var dl = provisioning.createDistributionList("admin-group-dl1@" + getDefaultDomainName(), new HashMap<>(
            Map.of(ZAttrProvisioning.A_zimbraIsAdminGroup, "TRUE")
    ));

    var request = new AddDistributionListMemberRequest(
            dl.getId(),
            List.of(userAccount.getName())
    );

    final SoapResponse response = getSoapClient().newRequest()
            .setCaller(domainAdminAccount)
            .setSoapBody(request)
            .execute();

    Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());

    var members = provisioning.getGroupMembers(dl);
    Assertions.assertEquals(1, members.length);
    Assertions.assertEquals(userAccount.getName(), members[0]);
  }

  @Test
  void addDistributionListMemberByDelegatedAdmin() throws Exception {
    final Account delegatedAdmin = createAccount()
            .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
    final Account userAccount = createAccount().create();
    final Domain target = provisioning.getDomain(delegatedAdmin);

    grantRights(delegatedAdmin, Right.RT_domainAdminDistributionListRights, target);

    var dl = provisioning.createDistributionList("admin-group-dl2@" + getDefaultDomainName(), new HashMap<>(
            Map.of(ZAttrProvisioning.A_zimbraIsAdminGroup, "TRUE")
    ));

    var request = new AddDistributionListMemberRequest(
            dl.getId(),
            List.of(userAccount.getName())
    );

    final SoapResponse response = getSoapClient().newRequest()
            .setCaller(delegatedAdmin)
            .setSoapBody(request)
            .execute();

    Assertions.assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, response.statusCode());

    var members = provisioning.getGroupMembers(dl);
    Assertions.assertEquals(0, members.length);
  }

  private static void grantRights(Account delegatedAdmin, String rights, Domain target) throws ServiceException {
    final Set<ZimbraACE> aces = new HashSet<>();
    aces.add(new ZimbraACE(
            delegatedAdmin.getId(),
            GranteeType.GT_USER,
            RightManager.getInstance().getRight(rights),
            RightModifier.RM_CAN_DELEGATE,
            null));
    ACLUtil.grantRight(provisioning, target, aces);
  }
}
