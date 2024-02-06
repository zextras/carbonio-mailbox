// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.soap;

import com.zextras.mailbox.util.SoapClient;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test class that starts a SOAP server and client using all mailbox SOAP APIs:
 * Admin, Account, Mail
 */
public class SoapTestSuite {

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension.Builder()
      .addEngineHandler("com.zimbra.cs.service.admin.AdminService")
      .addEngineHandler("com.zimbra.cs.service.account.AccountService")
      .addEngineHandler("com.zimbra.cs.service.mail.MailService")
      .create();

  public SoapClient getSoapClient() {
    return soapExtension.getSoapClient();
  }

  public void printResponse(HttpResponse response) throws Exception {
    System.out.println(new String (response.getEntity().getContent().readAllBytes()));
  }

}
