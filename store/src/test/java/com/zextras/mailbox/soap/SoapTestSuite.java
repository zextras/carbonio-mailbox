// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.soap;

import com.zextras.mailbox.util.SoapClient;
import org.junit.jupiter.api.extension.RegisterExtension;

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

}
