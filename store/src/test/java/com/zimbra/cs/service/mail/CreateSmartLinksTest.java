package com.zimbra.cs.service.mail;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateSmartLinksTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }


  @Test
  void shouldNotFail() throws Exception {
    Account account = accountCreatorFactory.get().create();
    String xml = "<CreateSmartLinksRequest xmlns=\"urn:zimbraMail\"><attachments draftId=\"3453453-54353\" partName=\"part1\"/><attachments draftId=\"3453453-54353\" partName=\"part2\"/></CreateSmartLinksRequest>";

    HttpResponse resp = getSoapClient().executeSoap(account, parseXML(xml));

    assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
    final String xmlResponse = getResponse(resp);
    assertFalse(xmlResponse.contains("Fault"));
  }

}