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
import com.zimbra.soap.mail.message.CreateMountpointRequest;
import com.zimbra.soap.mail.message.CreateMountpointResponse;
import com.zimbra.soap.mail.message.FolderActionRequest;
import com.zimbra.soap.mail.message.ModifyAppointmentRequest;
import com.zimbra.soap.mail.message.SendShareNotificationRequest;
import com.zimbra.soap.mail.type.ActionGrantSelector;
import com.zimbra.soap.mail.type.CalOrganizer;
import com.zimbra.soap.mail.type.CalendarAttendee;
import com.zimbra.soap.mail.type.DtTimeInfo;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.FolderActionSelector;
import com.zimbra.soap.mail.type.InvitationInfo;
import com.zimbra.soap.mail.type.Msg;
import com.zimbra.soap.mail.type.NewMountpointSpec;
import com.zimbra.soap.type.Id;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
  void shouldNotifyAllAttendees_WhenUpdatingAppointment_OnASharedCalendar() throws Exception {
    final Account userA = accountCreatorFactory.get().withUsername("userA").create();
    final Account userB = accountCreatorFactory.get().withUsername("userB").create();
    final String userC = "userC@test.com";

    final Folder calendarToShare = getCalendarToShare(userA);
    shareCalendar(userA, userB, calendarToShare);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    greenMail.reset();

    final CreateMountpointResponse mountpointResponse = createMountpointForSharedCalendar(
        userA, userB, calendarToShare);

    final Msg msgWithInvitation = createMsgOnSharedCalendar(mountpointResponse, userA, userB,
        List.of(userC));

    final CreateAppointmentResponse appointment = createAppointment(userB, msgWithInvitation);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    final MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
    final Address[] allRecipients = receivedMessage.getAllRecipients();
    Assertions.assertEquals(1, allRecipients.length);
    Assertions.assertEquals(userC, allRecipients[0].toString());
    greenMail.reset();

    msgWithInvitation.setSubject("Modified subject");
    final String calInvId = appointment.getCalInvId();
    final String[] userIdAndInviteId = calInvId.split(":");
    Assertions.assertEquals(userA.getId(), userIdAndInviteId[0]);
    modifyAppointment(calInvId, userB, msgWithInvitation);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    Assertions.assertEquals(1, allRecipients.length);
    Assertions.assertEquals(userC, allRecipients[0].toString());
    greenMail.reset();
  }

  private CreateMountpointResponse createMountpointForSharedCalendar(Account userA, Account userB, Folder calendarToShare)
      throws Exception {
    final NewMountpointSpec newMountpointSpec = new NewMountpointSpec("test shared calendar");
    newMountpointSpec.setDefaultView("appointment");
    newMountpointSpec.setRemoteId(calendarToShare.getId());
    newMountpointSpec.setOwnerId(userA.getId());
    newMountpointSpec.setFolderId("1");
    CreateMountpointRequest createMountpointRequest = new CreateMountpointRequest(newMountpointSpec);
    final HttpResponse response = getSoapClient().executeSoap(userB,
        createMountpointRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    return SoapUtils.getSoapResponse(response, MailConstants.E_CREATE_MOUNTPOINT_RESPONSE,
        CreateMountpointResponse.class);
  }

  private CreateAppointmentResponse createAppointment(Account authenticatedAccount, Msg msg)
      throws Exception {
    final CreateAppointmentRequest createAppointmentRequest = new CreateAppointmentRequest();
    createAppointmentRequest.setMsg(msg);
    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        createAppointmentRequest);
    String soapResponse = SoapUtils.getResponse(response);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode(), "Create appointment failed with:\n" + soapResponse);
    return SoapUtils.getSoapResponse(soapResponse, MailConstants.E_CREATE_APPOINTMENT_RESPONSE,
        CreateAppointmentResponse.class);
  }

  private Msg createMsgOnSharedCalendar(CreateMountpointResponse mountpointResponse, Account userA, Account userB, List<String> attendees) {
    Msg msg = new Msg();
    InvitationInfo invitationInfo = new InvitationInfo();

    final List<CalendarAttendee> calendarAttendees = populateCalendarAttendees(
        attendees);
    invitationInfo.setAttendees(calendarAttendees);
    final CalOrganizer calOrganizer = new CalOrganizer();
    calOrganizer.setAddress(userA.getName());
    calOrganizer.setSentBy(userB.getName());
    invitationInfo.setOrganizer(calOrganizer);
    final long nowMillis = Instant.now().toEpochMilli();
    invitationInfo.setDateTime(nowMillis);
    invitationInfo.setDtStart(new DtTimeInfo("20250702T120000"));
    final List<EmailAddrInfo> emailAddrInfos = sendAppointmentTo(attendees);

    final EmailAddrInfo from = new EmailAddrInfo(userA.getName());
    from.setAddressType("f");
    emailAddrInfos.add(from);
    final EmailAddrInfo emailAddrInfo = new EmailAddrInfo(userB.getName());
    emailAddrInfo.setAddressType("s");
    emailAddrInfos.add(emailAddrInfo);

    msg.setEmailAddresses(emailAddrInfos);
    msg.setInvite(invitationInfo);
    msg.setFolderId(String.valueOf(mountpointResponse.getMount().getId()));
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

  private static List<EmailAddrInfo> sendAppointmentTo(List<String> attendees) {
    final List<EmailAddrInfo> emailAddrInfos = new ArrayList<>();
    for (String address : attendees) {
      final EmailAddrInfo emailTo = new EmailAddrInfo(address);
      emailTo.setAddressType("t");
      emailAddrInfos.add(emailTo);
    }
    return emailAddrInfos;
  }

  private void modifyAppointment(String appointmentId, Account authenticatedAccount, Msg msg)
      throws Exception {
    final ModifyAppointmentRequest modifyAppointmentRequest = new ModifyAppointmentRequest();
    modifyAppointmentRequest.setId(appointmentId);
    modifyAppointmentRequest.setMsg(msg);

    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        modifyAppointmentRequest);
    final String soapResponse = SoapUtils.getResponse(response);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode(), "ModifyAppointment failed with: \n" + soapResponse);
  }

  private void shareCalendar(Account authenticatedAccount, Account sharedAccount, Folder calendar)
      throws Exception {

    shareFolderAsManager(authenticatedAccount, sharedAccount, calendar.getFolderId());
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

  private void shareFolderAsManager(Account authenticatedAccount, Account sharedAccount, int folderId)
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

  private static Folder getCalendarToShare(Account user) throws ServiceException {
    final List<Folder> calendarFolders = getAllCalendars(
        user);
    return calendarFolders.get(0);
  }

  private static List<Folder> getAllCalendars(Account authenticatedAccount) throws ServiceException {
    final Mailbox mailbox = mailboxManager.getMailboxByAccount(authenticatedAccount);
    return mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
  }
}