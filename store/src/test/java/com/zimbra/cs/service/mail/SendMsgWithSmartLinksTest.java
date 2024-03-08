// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator.Factory;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SendMsgWithSmartLinksRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SendMsgWithSmartLinksTest extends SoapTestSuite {


  private static Factory accountCreatorFactory;

  @BeforeAll
  static void setUp() {
    accountCreatorFactory = new Factory(Provisioning.getInstance());
  }

  @Test
  void shouldAddSmartLinksInBody() throws Exception {
    final Account account = accountCreatorFactory.get().create();
    final SendMsgWithSmartLinksRequest sendMsgWithSmartLinksRequest = new SendMsgWithSmartLinksRequest();
    final Element element = JaxbUtil.jaxbToElement(sendMsgWithSmartLinksRequest);


    final HttpResponse response = getSoapClient().executeSoap(account, element);
    this.printResponse(response);
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

//    final String soapEnvelope = new String(response.getEntity().getContent().readAllBytes());

//    final Element sendMsgWithSmartLinksElement = Element.parseXML(soapEnvelope).getElement("Body").getElement(
//        MailConstants.E_SEND_MSG_WITH_SMART_LINKS_RESPONSE);
//    final SendMsgResponse sendMsgResponse = JaxbUtil.elementToJaxb(sendMsgWithSmartLinksElement, SendMsgWithSmartLinksResponse.class);
//    Assertions.fail("Please implement the test!");
  }

}