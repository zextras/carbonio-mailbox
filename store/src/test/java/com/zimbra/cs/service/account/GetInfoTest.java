package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.account.message.GetInfoRequest;
import java.util.List;
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

  @Test
  void attributesSectionProvidesAccountStatusAttribute() throws Exception {
    final var account =
        createAccount()
            .withAttribute(Provisioning.A_zimbraAccountStatus, AccountStatus.active)
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
}



