// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.soap;

import static com.zimbra.client.ZEmailAddress.EMAIL_TYPE_TO;

import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.AccountCreator;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.cs.service.mail.MailServiceWithoutTracking;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateAppointmentRequest;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.message.ForwardAppointmentRequest;
import com.zimbra.soap.mail.message.ForwardAppointmentResponse;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.Msg;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test class that starts a SOAP server and client using all mailbox SOAP APIs:
 * Admin, Account, Mail
 */
public class SoapTestSuite {

  @RegisterExtension
  protected static SoapExtension soapExtension = new SoapExtension.Builder()
      .addEngineHandler(AdminService.class.getName())
      .addEngineHandler(AccountService.class.getName())
      .addEngineHandler(MailServiceWithoutTracking.class.getName())
      .create();

  protected static AccountCreator.Factory getCreateAccountFactory() {
    return new Factory(Provisioning.getInstance(),
        soapExtension.getDefaultDomain());
  }
  protected static AccountAction.Factory getAccountActionFactory() throws ServiceException {
    return new AccountAction.Factory(
        MailboxManager.getInstance(), RightManager.getInstance());
  }


  protected static String getDefaultDomainName() {
    return soapExtension.getDefaultDomain();
  }

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

  protected CreateAppointmentResponse createAppointment(Account authenticatedAccount, Msg msg)
      throws Exception {
    final CreateAppointmentRequest createAppointmentRequest = new CreateAppointmentRequest();
    createAppointmentRequest.setMsg(msg);
    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        createAppointmentRequest);
    String soapResponse = SoapUtils.getResponse(response);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode(),
        "Create appointment failed with:\n" + soapResponse);
    return SoapUtils.getSoapResponse(soapResponse, MailConstants.E_CREATE_APPOINTMENT_RESPONSE,
        CreateAppointmentResponse.class);
  }

  protected ForwardAppointmentResponse forwardAppointment(Account authenticatedAccount, String appointmentId, String to)
      throws Exception {

    final ForwardAppointmentRequest forwardAppointmentRequest = new ForwardAppointmentRequest();
    forwardAppointmentRequest.setId(appointmentId);
    final Msg msg = new Msg();
    msg.setEmailAddresses(List.of(new EmailAddrInfo(to, EMAIL_TYPE_TO)));
    forwardAppointmentRequest.setMsg(msg);

    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        forwardAppointmentRequest);
    String soapResponse = SoapUtils.getResponse(response);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode(),
        "Forward appointment failed with:\n" + soapResponse);
    return SoapUtils.getSoapResponse(soapResponse, MailConstants.E_FORWARD_APPOINTMENT_RESPONSE,
        ForwardAppointmentResponse.class);
  }
}
