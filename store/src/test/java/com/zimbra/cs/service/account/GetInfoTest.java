package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.account.message.DistributionListActionRequest;
import com.zimbra.soap.account.message.GetInfoRequest;

import java.util.HashMap;
import java.util.List;

import com.zimbra.soap.account.type.DistributionListAction;
import com.zimbra.soap.account.type.DistributionListGranteeSelector;
import com.zimbra.soap.account.type.DistributionListRightSpec;
import com.zimbra.soap.type.DistributionListBy;
import com.zimbra.soap.type.DistributionListGranteeBy;
import com.zimbra.soap.type.DistributionListSelector;
import com.zimbra.soap.type.GranteeType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
 import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("api")
class GetInfoTest extends SoapTestSuite {

   private Account account;

  @BeforeEach
  void setUp() throws Exception {
    account = createAccount().create();
  }

  @Test
  void getAllSections() throws Exception {
    final var request = new GetInfoRequest();

    final var response = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, response.statusCode());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "mbox",
        "prefs",
        "attrs",
        "zimlets",
        "props",
        "idents",
        "sigs",
        "dsrcs",
        "children"
      })
  void getSpecificSection(String section) throws Exception {
    final var request = new GetInfoRequest().addSection(section);

    final var response = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, response.statusCode());
  }

  @Test
  void attributesSectionProvidesAmavisLists() throws Exception {
    final var account =
        createAccount()
            .withAttribute(ZAttrProvisioning.A_amavisWhitelistSender, "foo1@bar.com")
            .withAttribute(ZAttrProvisioning.A_amavisBlacklistSender, "foo2@bar.com")
            .create();
    final var response = getAttributesSection(account);

    containsAttributes(response, List.of("amavisWhitelistSender","amavisBlacklistSender"));
  }

  @Test
  void attributesSectionProvidesExternalVirtualAccountWhenSet() throws Exception {
    final var account =
        createAccount()
            .withAttribute(Provisioning.A_zimbraIsExternalVirtualAccount, "FALSE")
            .create();
    final var response = getAttributesSection(account);

    containsAttribute(response, Provisioning.A_zimbraIsExternalVirtualAccount);
  }

  private static void containsAttributes(SoapResponse response, List<String> attributes) {
    assertEquals(HttpStatus.SC_OK, response.statusCode());
    final var body = response.body();
    attributes.forEach(attr -> assertTrue(body.contains(attr)));
  }

  private static void containsAttribute(SoapResponse response, String attribute) {
    containsAttributes(response, List.of(attribute));
  }

  private SoapResponse getAttributesSection(Account account) throws Exception {
    final var request = new GetInfoRequest().addSection("attrs");
		return getSoapClient().executeSoap(account, request);
  }

  private SoapResponse getResponseWithRights(Account account) throws Exception {
    final var request = new GetInfoRequest().setRights("sendAs","sendAsDistList","viewFreeBusy","sendOnBehalfOf","sendOnBehalfOfDistList");
    return getSoapClient().executeSoap(account, request);
  }

  @Test
  void attributesSectionProvidesAccountStatusAttribute() throws Exception {
    final var account =
        createAccount()
            .withAttribute(Provisioning.A_zimbraAccountStatus, AccountStatus.active.toString())
            .create();
    final var response = getAttributesSection(account);

    containsAttribute(response,"zimbraAccountStatus");
  }

  @Test
  void getInfo_shouldReturnAlsoDeprecatedAttributes() throws Exception {
    final AttributeInfo featureMailEnabled = AttributeManager.getInst()
        .getAttributeInfo("zimbraFeatureMailEnabled");
    Assertions.assertTrue(featureMailEnabled.isDeprecated());

    final var account =
        createAccount()
            .withAttribute(featureMailEnabled.getName(), "FALSE")
            .create();
    final var response = getAttributesSection(account);

    assertEquals(HttpStatus.SC_OK, response.statusCode());
    final var body = response.body();
    assertTrue(body.contains("<attr name=\"zimbraFeatureMailEnabled\">FALSE</attr>"));
  }

  @Test
  void getInfo_shouldContainsRightsSection_sendAsDistList_singleAccount() throws Exception {
    final AttributeInfo featureMailEnabled = AttributeManager.getInst()
            .getAttributeInfo("zimbraFeatureMailEnabled");
    Assertions.assertTrue(featureMailEnabled.isDeprecated());

    final var admin =
            createAccount()
                    .asGlobalAdmin()
                    .create();
    final var grantee =  createAccount().create();

    var dl = getProvisioning().createDistributionList("dl@test.com", new HashMap<>());
    assertNotNull( getProvisioning().get(Key.DistributionListBy.id, dl.getId()) );

    DistributionListAction distributionListAction = new DistributionListAction(DistributionListAction.Operation.grantRights);
    DistributionListRightSpec distributionListRightSpec = new DistributionListRightSpec("sendAsDistList");
    distributionListRightSpec.addGrantee(new DistributionListGranteeSelector(
            GranteeType.email, DistributionListGranteeBy.name, grantee.getName()
    ));
    distributionListAction.setRights(List.of(distributionListRightSpec));

    getSoapClient().executeSoap(admin, new DistributionListActionRequest(new DistributionListSelector(DistributionListBy.id, dl.getId() ), distributionListAction) );

    final var response = getResponseWithRights(grantee);

    assertEquals(HttpStatus.SC_OK, response.statusCode());
    final var body = response.body();

    assertTrue(body.contains("""
            <rights><targets right="sendAsDistList"><target type="dl"><email addr="dl@test.com"/></target></targets></rights>"""));
  }

  @Test
  void getInfo_shouldContainsRightsSection_sendAsDistList_distributionList() throws Exception {
    final AttributeInfo featureMailEnabled = AttributeManager.getInst()
            .getAttributeInfo("zimbraFeatureMailEnabled");
    Assertions.assertTrue(featureMailEnabled.isDeprecated());

    final var admin =
            createAccount()
                    .asGlobalAdmin()
                    .create();
    final var member =  createAccount().create();

    var targetDl = getProvisioning().createDistributionList("target-dl@test.com", new HashMap<>());
    var granteeDl = getProvisioning().createDistributionList("grantee-dl@test.com", new HashMap<>());

    getProvisioning().addGroupMembers(granteeDl, new String[] { member.getName() });
    assertNotNull( getProvisioning().get(Key.DistributionListBy.id, targetDl.getId()) );

    DistributionListAction distributionListAction = new DistributionListAction(DistributionListAction.Operation.grantRights);
    DistributionListRightSpec distributionListRightSpec = new DistributionListRightSpec("sendAsDistList");
    distributionListRightSpec.addGrantee(new DistributionListGranteeSelector(
            GranteeType.email, DistributionListGranteeBy.name, granteeDl.getName()
    ));
    distributionListAction.setRights(List.of(distributionListRightSpec));
    getSoapClient().executeSoap(admin, new DistributionListActionRequest(new DistributionListSelector(DistributionListBy.id, targetDl.getId() ), distributionListAction) );

    final var response = getResponseWithRights(member);

    assertEquals(HttpStatus.SC_OK, response.statusCode());
    final var body = response.body();

    assertTrue(body.contains("""
            <rights><targets right="sendAsDistList"><target type="dl"><email addr="target-dl@test.com"/></target></targets></rights>"""));
  }

  @Test
  void getInfo_shouldContainsRightsSection_sendAsDistList_distributionLists() throws Exception {
    final AttributeInfo featureMailEnabled = AttributeManager.getInst()
            .getAttributeInfo("zimbraFeatureMailEnabled");
    Assertions.assertTrue(featureMailEnabled.isDeprecated());

    final var admin =
            createAccount()
                    .asGlobalAdmin()
                    .create();
    final var member =  createAccount().create();


    var targetDl = getProvisioning().createDistributionList("target-1-dl@test.com", new HashMap<>());
    var granteeDl = getProvisioning().createDistributionList("grantee-1-dl@test.com", new HashMap<>());
    var otherGranteeDl = getProvisioning().createDistributionList("other-grantee-1-dl@test.com", new HashMap<>());

    getProvisioning().addGroupMembers(granteeDl, new String[] { member.getName() });

    assertNotNull( getProvisioning().get(Key.DistributionListBy.id, targetDl.getId()) );

    DistributionListAction distributionListAction = new DistributionListAction(DistributionListAction.Operation.grantRights);
    DistributionListRightSpec distributionListRightSpec = new DistributionListRightSpec("sendAsDistList");
    distributionListRightSpec.addGrantee(new DistributionListGranteeSelector(
            GranteeType.email, DistributionListGranteeBy.name, granteeDl.getName()
    ));
    distributionListAction.setRights(List.of(distributionListRightSpec));
    getSoapClient().executeSoap(admin, new DistributionListActionRequest(new DistributionListSelector(DistributionListBy.id, targetDl.getId() ), distributionListAction) );

    DistributionListAction otherDistributionListAction = new DistributionListAction(DistributionListAction.Operation.grantRights);
    DistributionListRightSpec otherDistributionListRightSpec = new DistributionListRightSpec("sendOnBehalfOfDistList");
    otherDistributionListRightSpec.addGrantee(new DistributionListGranteeSelector(
            GranteeType.email, DistributionListGranteeBy.name, otherGranteeDl.getName()
    ));
    otherDistributionListAction.setRights(List.of(otherDistributionListRightSpec));
    getSoapClient().executeSoap(admin, new DistributionListActionRequest(new DistributionListSelector(DistributionListBy.id, targetDl.getId() ), otherDistributionListAction) );

    final var response = getResponseWithRights(member);

    assertEquals(HttpStatus.SC_OK, response.statusCode());
    final var body = response.body();

    assertTrue(body.contains("""
            <rights><targets right="sendAsDistList"><target type="dl"><email addr="target-1-dl@test.com"/></target></targets></rights>"""));
  }
}



