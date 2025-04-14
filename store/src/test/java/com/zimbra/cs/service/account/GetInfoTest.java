package com.zimbra.cs.service.account;

import static com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.account.message.GetInfoRequest;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("api")
class GetInfoTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private Account account;

  @BeforeAll
  static void init() {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning,
        soapExtension.getDefaultDomain());
  }

  @BeforeEach
  void setUp() throws Exception {
    account = accountCreatorFactory.get().create();
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
        accountCreatorFactory
            .get()
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
}
