package com.zimbra.cs.service.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.CreateAppointmentRequest;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.message.FolderActionRequest;
import com.zimbra.soap.mail.message.ModifyAppointmentRequest;
import com.zimbra.soap.mail.message.SendShareNotificationRequest;
import com.zimbra.soap.mail.type.ActionGrantSelector;
import com.zimbra.soap.mail.type.CalendarAttendee;
import com.zimbra.soap.mail.type.DtTimeInfo;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.FolderActionSelector;
import com.zimbra.soap.mail.type.InvitationInfo;
import com.zimbra.soap.mail.type.Msg;
import com.zimbra.soap.type.Id;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModifyCalendarItemApiTest extends SoapTestSuite {

  private static MailboxManager mailboxManager;
  private static AccountCreator.Factory accountCreatorFactory;
  private static GreenMail greenMail;

  @BeforeAll
  static void setUpClass() throws Exception {
    greenMail =
        new GreenMail(
            new ServerSetup[] {
                new ServerSetup(
                    SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    Provisioning provisioning = Provisioning.getInstance();
    mailboxManager = MailboxManager.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @Test
  void shouldNotifyAllAttendeesIfAppointmentModifiedByOtherManagerAccount() throws Exception {
    final Account authenticatedAccount = accountCreatorFactory.get().withUsername("mainAccount").create();
    final Account sharedAccount = accountCreatorFactory.get().withUsername("shareTo").create();
    final Account otherAccount = accountCreatorFactory.get().withUsername("otherAccount").create();

    final Folder calendarToShare = getCalendarToShare(authenticatedAccount);

    shareCalendar(authenticatedAccount, sharedAccount, calendarToShare);
    final int calendarId = calendarToShare.getId();
    final List<String> attendees = List.of(otherAccount.getName());
    final Msg msgWithInvitation = createMsgWithInvitation(calendarId, attendees);
    final String calInvId = createAppointment(calendarId,
        authenticatedAccount, msgWithInvitation).getCalInvId();

    msgWithInvitation.setSubject("Modified subject");
    modifyAppointment(authenticatedAccount.getId() + ":" + calInvId, sharedAccount, msgWithInvitation);

    //TODO: check who is notified of the changes
  }

  private CreateAppointmentResponse createAppointment(int calendarId,
      Account authenticatedAccount, Msg msg)
      throws Exception {
    final CreateAppointmentRequest createAppointmentRequest = new CreateAppointmentRequest();
    createAppointmentRequest.setMsg(msg);
    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        createAppointmentRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    return SoapUtils.getSoapResponse(response, MailConstants.E_CREATE_APPOINTMENT_RESPONSE,
        CreateAppointmentResponse.class);
  }

  private static Msg createMsgWithInvitation(int calendarToShare, List<String> attendees) {
    Msg msg = new Msg();
    InvitationInfo invitationInfo = new InvitationInfo();

    final List<CalendarAttendee> calendarAttendees = populateCalendarAttendees(
        attendees);

    invitationInfo.setAttendees(calendarAttendees);
    final long nowMillis = Instant.now().toEpochMilli();
    invitationInfo.setDateTime(nowMillis);
    invitationInfo.setDtStart(new DtTimeInfo("20250702T120000"));
    msg.setInvite(invitationInfo);
    msg.setFolderId(String.valueOf(calendarToShare));
    msg.setSubject("Test appointment");
    return msg;
  }

  private static List<CalendarAttendee> populateCalendarAttendees(List<String> attendees) {
    final List<CalendarAttendee> calendarAttendees = new ArrayList<>();
    for (String address : attendees) {
      final CalendarAttendee calendarAttendee = new CalendarAttendee();
      calendarAttendee.setAddress(address);
      calendarAttendee.setDisplayName(address);
      calendarAttendee.setRsvp(true);
      calendarAttendee.setRole("REQ");
      calendarAttendees.add(calendarAttendee);
    }
    return calendarAttendees;
  }

  private void modifyAppointment(String appointmentId, Account authenticatedAccount, Msg msg)
      throws Exception {
    final ModifyAppointmentRequest modifyAppointmentRequest = new ModifyAppointmentRequest();
    modifyAppointmentRequest.setId(appointmentId);
    modifyAppointmentRequest.setMsg(msg);

    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        modifyAppointmentRequest);
    System.out.println(SoapUtils.getResponse(response));
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
  }

  private void shareCalendar(Account authenticatedAccount, Account sharedAccount, Folder calendar)
      throws Exception {

    shareFolder(authenticatedAccount, sharedAccount, calendar.getFolderId());
    final SendShareNotificationRequest shareNotificationRequest = new SendShareNotificationRequest();
    shareNotificationRequest.setItem(new Id(calendar.getId()));
    shareNotificationRequest.setEmailAddresses(new ArrayList<>() {{
      add(new EmailAddrInfo(
          sharedAccount.getName()));
    }});

    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        shareNotificationRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
  }

  private void shareFolder(Account authenticatedAccount, Account sharedAccount, int folderId)
      throws Exception {
    final FolderActionSelector grantRequest = new FolderActionSelector( String.valueOf(folderId), "grant");
    final ActionGrantSelector grant = new ActionGrantSelector("rwidx", "usr");
    grant.setDisplayName(sharedAccount.getName());
    grant.setPassword("");
    grantRequest.setGrant(grant);
    final FolderActionRequest folderActionRequest = new FolderActionRequest(grantRequest);

    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        folderActionRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
  }

  private static Folder getCalendarToShare(Account authenticatedAccount) throws ServiceException {
    final Mailbox mailbox = mailboxManager.getMailboxByAccount(authenticatedAccount);
    final List<Folder> calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.get(0);
  }
}