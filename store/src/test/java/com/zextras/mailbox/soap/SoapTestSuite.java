// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.soap;

import org.junit.jupiter.api.extension.RegisterExtension;

public class SoapTestSuite {

  @RegisterExtension
  protected static SoapExtension soapExtension = new SoapExtension.Builder()
      .addEngineHandler("com.zimbra.cs.service.admin.AdminService").create();

}
