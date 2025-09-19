package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.soap.SoapTestSuite;
 import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.soap.account.message.GetInfoRequest;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
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

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
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

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }

  @Test
  void attributesSectionProvidesAmavisLists() throws Exception {
    final var account =
        createAccount()
            .withAttribute(ZAttrProvisioning.A_amavisWhitelistSender, "foo1@bar.com")
            .withAttribute(ZAttrProvisioning.A_amavisBlacklistSender, "foo2@bar.com")
            .create();
    final var request = new GetInfoRequest().addSection("attrs");

    final var response = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final var body = EntityUtils.toString(response.getEntity());
    assertTrue(body.contains("amavisWhitelistSender"));
    assertTrue(body.contains("amavisBlacklistSender"));
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
    final var request = new GetInfoRequest().addSection("attrs");

    final var response = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final var body = EntityUtils.toString(response.getEntity());
    assertTrue(body.contains("<attr name=\"zimbraFeatureMailEnabled\">FALSE</attr>"));
  }
}



