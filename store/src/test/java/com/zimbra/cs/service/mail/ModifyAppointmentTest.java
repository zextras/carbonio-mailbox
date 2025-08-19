package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.AccountCreator;
import com.zextras.mailbox.util.PortUtil;
import com.zimbra.common.mailbox.FolderConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.calendar.IcalXmlStrMap;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class ModifyAppointmentTest extends SoapTestSuite {

  private static MailboxManager mailboxManager;
  private static AccountCreator.Factory accountCreatorFactory;
  private static GreenMail greenMail;

  @BeforeAll
  static void setUpClass() throws Exception {
    final int smtpPort = PortUtil.findFreePort();
    greenMail =
        new GreenMail(
            new ServerSetup[]{
                new ServerSetup(smtpPort, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    var provisioning = Provisioning.getInstance();
    provisioning.getLocalServer().setSmtpPort(smtpPort);
    mailboxManager = MailboxManager.getInstance();
    accountCreatorFactory = getCreateAccountFactory();
  }

  private static CalendarItem getCalendarItemById(Account account, String id) throws ServiceException {
    return mailboxManager.getMailboxByAccount(account)
        .getCalendarItemById(null, Integer.parseInt(id));
  }

  private static String nextWeek() {
    var now = LocalDateTime.now();
    return now.plusDays(7L).format(DateTimeFormatter.ofPattern("yMMdd"));
  }

  private static Folder getFirstCalendar(Account user) throws ServiceException {
    var mailbox = mailboxManager.getMailboxByAccount(user);
    var calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.get(0);
  }

  @Test
  void should_reset_participationStatus_when_dateTime_is_modified() throws Exception {
    var organizer = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var attendee = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var appointmentData = new AppointmentData(eventTitle, organizer, attendee, timezone, startTime,
        endTime, location);

    CreateAppointmentResponse createAppointmentResponse = JaxbUtil.elementToJaxb(
        createSimpleAppointment(appointmentData));

    // accept the created appointment manually (cannot do it from attendee's inbox)
    assert createAppointmentResponse != null;
    getCalendarItemById(organizer, createAppointmentResponse.getCalItemId())
        .getInvite(0).getAttendees().get(0).setPartStat(IcalXmlStrMap.PARTSTAT_ACCEPTED);

    var calendarItem = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());
    assertTrue(calendarItem.getInvite(0).getAttendees().get(0).getPartStat()
        .equalsIgnoreCase(IcalXmlStrMap.PARTSTAT_ACCEPTED));

    appointmentData.startTime = "20250907T165000";
    modifyCalendarAppointment(appointmentData, createAppointmentResponse.getCalInvId());
    var calendarItemModified = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());

    assertTrue(
        calendarItemModified.getInvite(0).getAttendees().get(0).getPartStat()
            .equalsIgnoreCase(IcalXmlStrMap.PARTSTAT_NEEDS_ACTION));
  }

  @Test
  void modify_appointment_should_throw_no_such_item_exception_when_item_not_found() throws Exception {
    var organizer = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var attendee = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var appointmentData = new AppointmentData(eventTitle, organizer, attendee, timezone, startTime,
        endTime, location);

    var mailServiceException = assertThrows(MailServiceException.class,
        () -> modifyCalendarAppointment(appointmentData, "456"));

    assertTrue(mailServiceException.getMessage().contains("no such item: 456"));
    assertSame(MailServiceException.NO_SUCH_ITEM, mailServiceException.getCode());
  }

  @Test
  void modify_appointment_should_throw_invalid_reuqest_exception_when_item_is_in_trash() throws Exception {
    var organizer = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var attendee = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var appointmentData = new AppointmentData(eventTitle, organizer, attendee, timezone, startTime,
        endTime, location);

    CreateAppointmentResponse createAppointmentResponse = JaxbUtil.elementToJaxb(
        createSimpleAppointment(appointmentData));

    assert createAppointmentResponse != null;
    var calendarItem = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());
    moveItemToFolderForAccount(organizer, String.valueOf(calendarItem.getId()), FolderConstants.ID_FOLDER_TRASH );

    var serviceException = assertThrows(ServiceException.class,
        () -> modifyCalendarAppointment(appointmentData, createAppointmentResponse.getCalInvId()));

    assertTrue(serviceException.getMessage().contains("cannot modify a calendar item under trash"));
    assertSame(ServiceException.INVALID_REQUEST, serviceException.getCode());
  }

  @Test
  void shouldNotifyAllAttendees_WhenUpdatingAppointment_OnASharedCalendar() throws Exception {
    var userA = accountCreatorFactory.get().withUsername("userA").create();
    var userB = accountCreatorFactory.get().withUsername("userB").create();
    var userC = "userC@test.com";

    var userACalendar = getFirstCalendar(userA);
    shareCalendar(userA, userB, userACalendar);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    greenMail.reset();

    var mountpointResponse = createMountpoint(userB, userACalendar);

    var invitationMsg = appointmentOnSharedCalendar(mountpointResponse, userA, userB,
        List.of(userC));

    var appointment = createAppointment(userB, invitationMsg);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    var receivedMessage = greenMail.getReceivedMessages()[0];
    var allRecipients = receivedMessage.getAllRecipients();
    Assertions.assertEquals(1, allRecipients.length);
    Assertions.assertEquals(userC, allRecipients[0].toString());
    greenMail.reset();

    invitationMsg.setSubject("Modified subject");
    var calInvId = appointment.getCalInvId();
    var userIdAndInviteId = calInvId.split(":");
    Assertions.assertEquals(userA.getId(), userIdAndInviteId[0]);
    modifyAppointment(calInvId, userB, invitationMsg);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);
    Assertions.assertEquals(1, allRecipients.length);
    Assertions.assertEquals(userC, allRecipients[0].toString());
    greenMail.reset();
  }

  private CreateMountpointResponse createMountpoint(Account onAccount, Folder sharedCalendar)
      throws Exception {
    var newMountpointSpec = new NewMountpointSpec("test shared calendar");
    newMountpointSpec.setDefaultView("appointment");
    newMountpointSpec.setRemoteId(sharedCalendar.getId());
    newMountpointSpec.setOwnerId(sharedCalendar.getAccountId());
    newMountpointSpec.setFolderId("1");
    var createMountpointRequest = new CreateMountpointRequest(newMountpointSpec);
    var response = getSoapClient().executeSoap(onAccount,
        createMountpointRequest);
    return SoapUtils.getSoapResponse(response, MailConstants.E_CREATE_MOUNTPOINT_RESPONSE,
        CreateMountpointResponse.class);
  }

  private Element createSimpleAppointment(AppointmentData appointmentData) throws Exception {
    var authToken = AuthProvider.getAuthToken(appointmentData.organiser);
    Map<String, Object> context = new HashMap<>();
    var zsc = new ZimbraSoapContext(
        authToken, appointmentData.organiser.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

    var jsonTemplate = IOUtils.toString(
        Objects.requireNonNull(this.getClass().getResourceAsStream("CreateAppointmentRequestTemplate.json")),
        StandardCharsets.UTF_8
    );

    // Dynamic values
    var organizerEmail = appointmentData.organiser.getName();
    var organizerName = appointmentData.organiser.getName();
    var invitee1Email = appointmentData.attendee.getName();

    var filledJson = jsonTemplate
        .replace("${organizer_email}", organizerEmail)
        .replace("${organizer_name}", organizerName)
        .replace("${invitee1_email}", invitee1Email)
        .replace("${event_title}", appointmentData.eventTitle)
        .replace("${location}", appointmentData.location)
        .replace("${start_time}", appointmentData.startTime)
        .replace("${end_time}", appointmentData.endTime)
        .replace("${timezone}", appointmentData.timezone);

    var createAppointmentElement =
        Element.parseJSON(filledJson, MailConstants.CREATE_APPOINTMENT_REQUEST, JSONElement.mFactory);
    var createAppointment = new CreateAppointment();
    createAppointment.setResponseQName(MailConstants.CREATE_APPOINTMENT_RESPONSE);

    return createAppointment.handle(createAppointmentElement, context);
  }

  @SuppressWarnings("UnusedReturnValue")
  private Element modifyCalendarAppointment(AppointmentData appointmentData, String appointmentId) throws Exception {
    var authToken = AuthProvider.getAuthToken(appointmentData.organiser);
    Map<String, Object> context = new HashMap<>();
    var zsc = new ZimbraSoapContext(
        authToken, appointmentData.organiser.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

    var jsonTemplate = IOUtils.toString(
        Objects.requireNonNull(this.getClass().getResourceAsStream("ModifyAppointmentRequestTemplate.json")),
        StandardCharsets.UTF_8
    );

    // Dynamic values
    var organizerEmail = appointmentData.organiser.getName();
    var organizerName = appointmentData.organiser.getName();
    var invitee1Email = appointmentData.attendee.getName();

    var filledJson = jsonTemplate
        .replace("${id}", appointmentId)
        .replace("${organizer_email}", organizerEmail)
        .replace("${organizer_name}", organizerName)
        .replace("${invitee1_email}", invitee1Email)
        .replace("${event_title}", appointmentData.eventTitle)
        .replace("${location}", appointmentData.location)
        .replace("${start_time}", appointmentData.startTime)
        .replace("${end_time}", appointmentData.endTime)
        .replace("${timezone}", appointmentData.timezone);

    var modifyAppointmentElement =
        Element.parseJSON(filledJson, MailConstants.MODIFY_APPOINTMENT_REQUEST, JSONElement.mFactory);
    var modifyAppointment = new ModifyAppointment();
    modifyAppointment.setResponseQName(MailConstants.MODIFY_APPOINTMENT_RESPONSE);

    return modifyAppointment.handle(modifyAppointmentElement, context);
  }

  private Msg appointmentOnSharedCalendar(CreateMountpointResponse mountpointResponse, Account userA, Account userB,
      List<String> attendees) {
    var msg = new Msg();
    msg.setFolderId(String.valueOf(mountpointResponse.getMount().getId()));
    msg.setSubject("Test appointment");

    var invitationInfo = new InvitationInfo();
    var organizer = new CalOrganizer();
    organizer.setAddress(userA.getName());
    organizer.setSentBy(userB.getName());
    invitationInfo.setOrganizer(organizer);
    attendees.forEach(
        address -> {
          var calendarAttendee = new CalendarAttendee();
          calendarAttendee.setAddress(address);
          calendarAttendee.setDisplayName(address);
          calendarAttendee.setRsvp(true);
          calendarAttendee.setRole("REQ");
          invitationInfo.addAttendee(calendarAttendee);
        }
    );

    var dateTime = nextWeek();
    invitationInfo.setDtStart(new DtTimeInfo(dateTime));

    attendees.forEach(
        address -> msg.addEmailAddress(new EmailAddrInfo(address, "t"))
    );
    msg.addEmailAddress(new EmailAddrInfo(userA.getName(), "f"));
    msg.addEmailAddress(new EmailAddrInfo(userB.getName(), "s"));
    msg.setInvite(invitationInfo);

    return msg;
  }

  @SuppressWarnings("UnusedReturnValue")
  private ModifyAppointmentResponse modifyAppointment(String appointmentId, Account authenticatedAccount, Msg msg)
      throws Exception {
    var modifyAppointmentRequest = new ModifyAppointmentRequest();
    modifyAppointmentRequest.setId(appointmentId);
    modifyAppointmentRequest.setEcho(true);
    modifyAppointmentRequest.setMsg(msg);

    var response = getSoapClient().executeSoap(authenticatedAccount,
        modifyAppointmentRequest);
    var soapResponse = SoapUtils.getResponse(response);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode(),
        "ModifyAppointment failed with: \n" + soapResponse);

    return SoapUtils.getSoapResponse(soapResponse, MailConstants.E_MODIFY_APPOINTMENT_RESPONSE,
        ModifyAppointmentResponse.class);
  }

  @SuppressWarnings("UnusedReturnValue")
  private SendShareNotificationResponse shareCalendar(Account authenticatedAccount, Account shareWith, Folder calendar)
      throws Exception {

    shareFolderAsManager(authenticatedAccount, shareWith, calendar.getFolderId());
    var shareNotificationRequest = new SendShareNotificationRequest();
    shareNotificationRequest.setItem(new Id(calendar.getId()));
    shareNotificationRequest.setEmailAddresses(new ArrayList<>() {{
      add(new EmailAddrInfo(
          shareWith.getName()));
    }});
    var response = getSoapClient().executeSoap(authenticatedAccount,
        shareNotificationRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    return SoapUtils.getSoapResponse(response, MailConstants.E_SEND_SHARE_NOTIFICATION_RESPONSE,
        SendShareNotificationResponse.class);
  }

  @SuppressWarnings("UnusedReturnValue")
  private FolderActionResponse shareFolderAsManager(Account authenticatedAccount, Account shareWith,
      int calendarFolderId)
      throws Exception {
    var grantRequest = new FolderActionSelector(String.valueOf(
        calendarFolderId),
        "grant");
    var grant = new ActionGrantSelector("rwidx", "usr");
    grant.setDisplayName(shareWith.getName());
    grant.setPassword("");
    grantRequest.setGrant(grant);
    var folderActionRequest = new FolderActionRequest(grantRequest);
    var response = getSoapClient().executeSoap(authenticatedAccount,
        folderActionRequest);
    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
    return SoapUtils.getSoapResponse(response, MailConstants.E_FOLDER_ACTION_RESPONSE,
        FolderActionResponse.class);
  }

  static class AppointmentData {

    public String eventTitle;
    public Account organiser;
    public Account attendee;
    public String timezone;
    public String startTime;
    public String endTime;
    public String location;

    public AppointmentData(String eventTitle, Account organiser, Account attendee, String timezone,
        String startTime, String endTime, String location) {
      this.eventTitle = eventTitle;
      this.organiser = organiser;
      this.attendee = attendee;
      this.timezone = timezone;
      this.startTime = startTime;
      this.endTime = endTime;
      this.location = location;
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void moveItemToFolderForAccount(Account targetAccount, String itemId, int targetFolderId)
      throws ServiceException {
    Element itemActionSelectorElement = new XMLElement(MailConstants.E_ACTION);
    itemActionSelectorElement.addAttribute(MailConstants.A_ID, itemId);
    itemActionSelectorElement.addAttribute(MailConstants.A_OPERATION, FolderAction.OP_MOVE);
    itemActionSelectorElement.addAttribute(MailConstants.A_FOLDER, targetFolderId);

    Element itemActionRequestElement = new XMLElement(MailConstants.E_ITEM_ACTION_REQUEST);
    itemActionRequestElement.addUniqueElement(itemActionSelectorElement);

    var itemActionHandler = new ItemAction();
    itemActionHandler.handle(itemActionRequestElement, getSoapContextForAccount(targetAccount, false));
  }

}