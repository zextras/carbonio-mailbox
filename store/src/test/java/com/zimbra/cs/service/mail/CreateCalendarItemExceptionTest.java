package com.zimbra.cs.service.mail;

import static com.zimbra.common.mailbox.FolderConstants.ID_FOLDER_CALENDAR;
import static com.zimbra.common.mailbox.FolderConstants.ID_FOLDER_TRASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.calendar.IcalXmlStrMap;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateAppointmentRequest;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.type.IntervalRule;
import com.zimbra.soap.mail.type.RecurrenceInfo;
import com.zimbra.soap.mail.type.SimpleRepeatingRule;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class CreateCalendarItemExceptionTest  extends SoapTestSuite {

  private static MailboxManager mailboxManager;
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void setUpClass() throws Exception {
    var provisioning = Provisioning.getInstance();
    mailboxManager = MailboxManager.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning, soapExtension.getDefaultDomain());
  }

  private static CalendarItem getCalendarItemById(Account account, String id) throws ServiceException {
    return mailboxManager.getMailboxByAccount(account)
        .getCalendarItemById(null, Integer.parseInt(id));
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

    var appointmentData =
        AppointmentData.builder()
            .withEventTitle(eventTitle)
            .withOrganiser(organizer)
            .withAttendee(attendee)
            .withTimezone(timezone)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .withLocation(location)
            .build();

    CreateAppointmentResponse createAppointmentResponse = JaxbUtil.elementToJaxb(
        createRecurringAppointment(appointmentData));

    // accept the created appointment manually (cannot do it from attendee's inbox)
    assert createAppointmentResponse != null;
    getCalendarItemById(organizer, createAppointmentResponse.getCalItemId())
        .getInvite(0).getAttendees().get(0).setPartStat(IcalXmlStrMap.PARTSTAT_ACCEPTED);

    var calendarItem = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());
    assertTrue(calendarItem.getInvite(0).getAttendees().get(0).getPartStat()
        .equalsIgnoreCase(IcalXmlStrMap.PARTSTAT_ACCEPTED));

    AppointmentData updatedAppointmentData = appointmentData.toBuilder()
        .withStartTime("20250907T165000").build();

     createCalendarItemException(updatedAppointmentData, createAppointmentResponse.getCalInvId());
    var calendarItemModified = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());

    assertTrue(
        calendarItemModified.getInvite(0).getAttendees().get(0).getPartStat()
            .equalsIgnoreCase(IcalXmlStrMap.PARTSTAT_NEEDS_ACTION));
  }

  @Test
  void createSimpleAppointment() throws Exception {
    var organizer = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var attendee = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var appointmentData =
        AppointmentData.builder()
            .withEventTitle(eventTitle)
            .withOrganiser(organizer)
            .withAttendee(attendee)
            .withTimezone(timezone)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .withLocation(location)
            .build();

    CreateAppointmentResponse createAppointmentResponse = JaxbUtil.elementToJaxb(
        createRecurringAppointment(appointmentData));

    assert createAppointmentResponse != null;
    var calendarItem = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());
    assert calendarItem != null;
  }

  @Test
  void shouldThrowServiceExceptionWhenEventCreatedInTrashFolder() throws Exception {
    var organizer = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var attendee = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var appointmentData =
        AppointmentData.builder()
            .withFolderId(String.valueOf(ID_FOLDER_TRASH))
            .withEventTitle(eventTitle)
            .withOrganiser(organizer)
            .withAttendee(attendee)
            .withTimezone(timezone)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .withLocation(location)
            .build();

    var exception =
        assertThrows(
            ServiceException.class,
            () -> createRecurringAppointment(appointmentData),
            "Should throw ServiceException when event is created in trash folder");

    assertEquals(
        "invalid request: cannot create a calendar item under trash", exception.getMessage());
  }

  @Test
  void createSimpleAppointmentLongMetaData() throws Exception {
    var organizer = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var attendee = accountCreatorFactory.get().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var longDescription = "Lorem ipsum dolor sit amet. ".repeat(1000);
    var appointmentData =
        AppointmentData.builder()
            .withEventTitle(eventTitle)
            .withOrganiser(organizer)
            .withAttendee(attendee)
            .withTimezone(timezone)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .withLocation(location)
            .withDesc(longDescription)
            .withHtmlDesc(longDescription)
            .build();

    CreateAppointmentResponse createAppointmentResponse = JaxbUtil.elementToJaxb(
        createRecurringAppointment(appointmentData));

    assert createAppointmentResponse != null;
    var calendarItem = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());

    assert calendarItem != null;

    var responseJsonElement =
        JaxbUtil.jaxbToElement(createAppointmentResponse, JSONElement.mFactory);
    var responseJsonElementString = responseJsonElement.toString();
    System.out.println("createAppointmentResponse = " + responseJsonElementString);
  }

  private Element createRecurringAppointment(AppointmentData appointmentData) throws Exception {
    var authToken = AuthProvider.getAuthToken(appointmentData.organiser);
    Map<String, Object> context = new HashMap<>();
    var zsc = new ZimbraSoapContext(
        authToken, appointmentData.organiser.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

    var jsonTemplate = IOUtils.toString(
        Objects.requireNonNull(this.getClass().getResourceAsStream("CreateAppointmentRequestTemplate.json")),
        StandardCharsets.UTF_8
    );

    // fill template
    var createAppointmentRequestPayload = jsonTemplate
        .replace("${organizer_email}", appointmentData.organiser.getName())
        .replace("${organizer_name}", appointmentData.organiser.getName())
        .replace("${invitee1_email}", appointmentData.attendee.getName())
        .replace("${event_title}", appointmentData.eventTitle)
        .replace("${folder_id}", appointmentData.folderId == null ? String.valueOf(ID_FOLDER_CALENDAR) : appointmentData.folderId)
        .replace("${location}", appointmentData.location)
        .replace("${start_time}", appointmentData.startTime)
        .replace("${end_time}", appointmentData.endTime)
        .replace("${timezone}", appointmentData.timezone)
        .replace("${description}", appointmentData.desc == null ? "N/A" : appointmentData.desc)
        .replace("${description_html}", appointmentData.htmlDesc == null ? "N/A" : appointmentData.htmlDesc);

    var createAppointmentElement =
        Element.parseJSON(createAppointmentRequestPayload, MailConstants.CREATE_APPOINTMENT_REQUEST, JSONElement.mFactory);

    // add recurring information to the payload
    CreateAppointmentRequest createAppointmentRequest = JaxbUtil.elementToJaxb(createAppointmentElement);
    RecurrenceInfo recurrenceInfo = new RecurrenceInfo();
    SimpleRepeatingRule recurrenceRule = new SimpleRepeatingRule("DAI");
    recurrenceRule.setInterval(new IntervalRule(1));
    recurrenceInfo.addRule(recurrenceRule);
    assert createAppointmentRequest != null;
    createAppointmentRequest.getMsg().getInvite().getInviteComponent().setRecurrence(recurrenceInfo);

    var createAppointment = new CreateAppointment();
    createAppointment.setResponseQName(MailConstants.CREATE_APPOINTMENT_RESPONSE);

    return createAppointment.handle(JaxbUtil.jaxbToElement(createAppointmentRequest), context);
  }

  @SuppressWarnings("UnusedReturnValue")
  private Element createCalendarItemException(AppointmentData appointmentData, String appointmentId) throws Exception {
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

    var createCalendarItemExceptionElement =
        Element.parseJSON(filledJson, MailConstants.CREATE_APPOINTMENT_EXCEPTION_REQUEST, JSONElement.mFactory);
    var createCalendarItemException = new CreateCalendarItemException();
    createCalendarItemException.setResponseQName(MailConstants.CREATE_APPOINTMENT_EXCEPTION_RESPONSE);

    return createCalendarItemException.handle(createCalendarItemExceptionElement, context);
  }
}