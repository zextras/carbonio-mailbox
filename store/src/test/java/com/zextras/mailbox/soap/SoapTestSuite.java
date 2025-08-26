// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.soap;

import static com.zimbra.client.ZEmailAddress.EMAIL_TYPE_TO;

import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.CreateAccount;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
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
import com.zimbra.soap.mail.type.CalOrganizer;
import com.zimbra.soap.mail.type.CalendarAttendee;
import com.zimbra.soap.mail.type.DtTimeInfo;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.InvitationInfo;
import com.zimbra.soap.mail.type.Msg;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  private static final SoapExtension soapExtension = new SoapExtension.Builder()
      .addEngineHandler(AdminService.class.getName())
      .addEngineHandler(AccountService.class.getName())
      .addEngineHandler(MailServiceWithoutTracking.class.getName())
      .create();

  protected static CreateAccount.Factory getCreateAccountFactory() {
    return new CreateAccount.Factory(Provisioning.getInstance(),
        getDefaultDomainName());
  }
  protected static AccountAction.Factory getAccountActionFactory() throws ServiceException {
    return AccountAction.Factory.getDefault();
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

  protected CreateAppointmentResponse createAppointmentSoap(Account authenticatedAccount, Msg msg)
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

  protected ForwardAppointmentResponse forwardAppointmentSoap(Account authenticatedAccount, String appointmentId, String to)
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

  protected Msg defaultAppointmentMessage(Account organizer, List<String> attendees) {
    Msg msg = new Msg();
    msg.setSubject("Test appointment");

    InvitationInfo invitationInfo = new InvitationInfo();
    final CalOrganizer calOrganizer = new CalOrganizer();
    calOrganizer.setAddress(organizer.getName());
    invitationInfo.setOrganizer(calOrganizer);
    attendees.forEach(
        address -> {
          final CalendarAttendee calendarAttendee = new CalendarAttendee();
          calendarAttendee.setAddress(address);
          calendarAttendee.setDisplayName(address);
          calendarAttendee.setRsvp(true);
          calendarAttendee.setRole("REQ");
          invitationInfo.addAttendee(calendarAttendee);
        });
    invitationInfo.setDateTime(Instant.now().toEpochMilli());
    final String dateTime = nextWeek();
    invitationInfo.setDtStart(new DtTimeInfo(dateTime));

    attendees.forEach(
        address -> msg.addEmailAddress(new EmailAddrInfo(address, "t")));
    msg.addEmailAddress(new EmailAddrInfo(organizer.getName(), "f"));
    msg.setInvite(invitationInfo);

    return msg;
  }

  private static String nextWeek() {
    final LocalDateTime now = LocalDateTime.now();
    return now.plusDays(7L).format(DateTimeFormatter.ofPattern("yMMdd"));
  }
}
