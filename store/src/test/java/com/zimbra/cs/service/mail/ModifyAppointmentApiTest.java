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
import com.zimbra.soap.mail.message.FolderActionResponse;
import com.zimbra.soap.mail.message.ModifyAppointmentRequest;
import com.zimbra.soap.mail.message.ModifyAppointmentResponse;
import com.zimbra.soap.mail.message.SendShareNotificationRequest;
import com.zimbra.soap.mail.message.SendShareNotificationResponse;
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
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModifyAppointmentApiTest extends SoapTestSuite {

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

    final Folder userACalendar = getFirstCalendar(userA);
    shareCalendar(userA, userB, userACalendar);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    greenMail.reset();

    final CreateMountpointResponse mountpointResponse = createMountpoint(userB, userACalendar);

    final Msg invitation = appointmentOnSharedCalendar(mountpointResponse, userA, userB,
        List.of(userC));

    final CreateAppointmentResponse appointment = createAppointment(userB, invitation);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    final MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
    final Address[] allRecipients = receivedMessage.getAllRecipients();
    Assertions.assertEquals(1, allRecipients.length);
    Assertions.assertEquals(userC, allRecipients[0].toString());
    greenMail.reset();

    invitation.setSubject("Modified subject");
    final String calInvId = appointment.getCalInvId();
    final String[] userIdAndInviteId = calInvId.split(":");
    Assertions.assertEquals(userA.getId(), userIdAndInviteId[0]);
    modifyAppointment(calInvId, userB, invitation);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    Assertions.assertEquals(1, allRecipients.length);
    Assertions.assertEquals(userC, allRecipients[0].toString());
    greenMail.reset();
  }

  private CreateMountpointResponse createMountpoint(Account onAccount, Folder sharedCalendar)
      throws Exception {
    final NewMountpointSpec newMountpointSpec = new NewMountpointSpec("test shared calendar");
    newMountpointSpec.setDefaultView("appointment");
    newMountpointSpec.setRemoteId(sharedCalendar.getId());
    newMountpointSpec.setOwnerId(sharedCalendar.getAccountId());
    newMountpointSpec.setFolderId("1");
    CreateMountpointRequest createMountpointRequest = new CreateMountpointRequest(newMountpointSpec);
    final HttpResponse response = getSoapClient().executeSoap(onAccount,
        createMountpointRequest);
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

  private Msg appointmentOnSharedCalendar(CreateMountpointResponse mountpointResponse, Account userA, Account userB, List<String> attendees) {
    Msg msg = new Msg();
    msg.setFolderId(String.valueOf(mountpointResponse.getMount().getId()));
    msg.setSubject("Test appointment");

    InvitationInfo invitationInfo = new InvitationInfo();
    final CalOrganizer organizer = new CalOrganizer();
    organizer.setAddress(userA.getName());
    organizer.setSentBy(userB.getName());
    invitationInfo.setOrganizer(organizer);
    attendees.forEach(
        address -> {
          final CalendarAttendee calendarAttendee = new CalendarAttendee();
          calendarAttendee.setAddress(address);
          calendarAttendee.setDisplayName(address);
          calendarAttendee.setRsvp(true);
          calendarAttendee.setRole("REQ");
          invitationInfo.addAttendee(calendarAttendee);
        }
    );

    invitationInfo.setDateTime(Instant.now().toEpochMilli());
    final String dateTime = nextWeek();
    invitationInfo.setDtStart(new DtTimeInfo(dateTime));

    attendees.forEach(
        address -> msg.addEmailAddress(new EmailAddrInfo(address, "t"))
    );
    msg.addEmailAddress(new EmailAddrInfo(userA.getName(), "f"));
    msg.addEmailAddress(new EmailAddrInfo(userB.getName(), "s"));
    msg.setInvite(invitationInfo);

    return msg;
  }

  private static String nextWeek() {
    final int secondsInAWeek = 7 * 24 * 60 * 60;
    final long startDateMillis = (Instant.now().getEpochSecond() + secondsInAWeek)*1000;
    return new DateTime(startDateMillis).toString("YMMdd") + "T120000";
  }

  private ModifyAppointmentResponse modifyAppointment(String appointmentId, Account authenticatedAccount, Msg msg)
      throws Exception {
    final ModifyAppointmentRequest modifyAppointmentRequest = new ModifyAppointmentRequest();
    modifyAppointmentRequest.setId(appointmentId);
    modifyAppointmentRequest.setMsg(msg);

    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        modifyAppointmentRequest);
    final String soapResponse = SoapUtils.getResponse(response);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode(), "ModifyAppointment failed with: \n" + soapResponse);

    return SoapUtils.getSoapResponse(soapResponse, MailConstants.E_MODIFY_APPOINTMENT_RESPONSE,
        ModifyAppointmentResponse.class);
  }

  private SendShareNotificationResponse shareCalendar(Account authenticatedAccount, Account shareWith, Folder calendar)
      throws Exception {

    shareFolderAsManager(authenticatedAccount, shareWith, calendar.getFolderId());
    final SendShareNotificationRequest shareNotificationRequest = new SendShareNotificationRequest();
    shareNotificationRequest.setItem(new Id(calendar.getId()));
    shareNotificationRequest.setEmailAddresses(new ArrayList<>() {{
      add(new EmailAddrInfo(
          shareWith.getName()));
    }});
    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        shareNotificationRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    return SoapUtils.getSoapResponse(response, MailConstants.E_SEND_SHARE_NOTIFICATION_RESPONSE,
        SendShareNotificationResponse.class);
  }

  private FolderActionResponse shareFolderAsManager(Account authenticatedAccount, Account shareWith, int calendarFolderId)
      throws Exception {
    final FolderActionSelector grantRequest = new FolderActionSelector(String.valueOf(
        calendarFolderId),
        "grant");
    final ActionGrantSelector grant = new ActionGrantSelector("rwidx", "usr");
    grant.setDisplayName(shareWith.getName());
    grant.setPassword("");
    grantRequest.setGrant(grant);
    final FolderActionRequest folderActionRequest = new FolderActionRequest(grantRequest);;
    final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
        folderActionRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    return SoapUtils.getSoapResponse(response, MailConstants.E_FOLDER_ACTION_RESPONSE,
        FolderActionResponse.class);
  }

  private static Folder getFirstCalendar(Account user) throws ServiceException {
    final Mailbox mailbox = mailboxManager.getMailboxByAccount(user);
    final List<Folder> calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.get(0);
  }

}