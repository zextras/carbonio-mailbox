// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.soap;

import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
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
      .addEngineHandler("com.zimbra.cs.service.mail.MailServiceWithoutTracking")
      .create();

  /**
   * @return {@link SoapClient} that can execute SOAP http requests
   */
  public SoapClient getSoapClient() {
    return soapExtension.getSoapClient();
  }

  /**
   * @param account {@link Account} who will make the SOAP request using the returned {@link ZimbraSoapContext}
   * @param isAdmin whether the account is admin or not
   * @return  {@code HashMap<String, Object>} holding {@link ZimbraSoapContext} as item
   * @throws ServiceException if context creation fails
   */
  public static HashMap<String, Object> getSoapContextForAccount(Account account, boolean isAdmin)
      throws ServiceException {
    return new HashMap<>() {
      {
        put(
            SoapEngine.ZIMBRA_CONTEXT,
            new ZimbraSoapContext(
                AuthProvider.getAuthToken(account, isAdmin),
                account.getId(),
                SoapProtocol.Soap12,
                SoapProtocol.Soap12));
      }
    };
  }

}
