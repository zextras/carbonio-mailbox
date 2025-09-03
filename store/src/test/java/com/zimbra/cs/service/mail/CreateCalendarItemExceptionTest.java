package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.CreateAccount;
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
import com.zimbra.cs.service.mail.ModifyAppointmentTest.AppointmentData;
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
  

  @BeforeAll
  static void setUpClass() throws Exception {
    var provisioning = Provisioning.getInstance();
    mailboxManager = MailboxManager.getInstance();
    
  }

  @Test
  void should_reset_participationStatus_when_dateTime_is_modified() throws Exception {
    var organizer = getCreateAccountFactory().withUsername(UUID.randomUUID().toString()).create();
    var attendee = getCreateAccountFactory().withUsername(UUID.randomUUID().toString()).create();
    var eventTitle = "Event Title";
    var timezone = "Asia/Calcutta";
    var startTime = "20250907T163000";
    var endTime = "20250907T180000";
    var location = "";

    var appointmentData = new AppointmentData(eventTitle, organizer, attendee, timezone, startTime,
        endTime, location);

    CreateAppointmentResponse createAppointmentResponse = JaxbUtil.elementToJaxb(
        createRecurringAppointment(appointmentData));

    // accept the created appointment manually (cannot do it from attendee's inbox)
    assert createAppointmentResponse != null;
    getCalendarItemById(organizer, createAppointmentResponse.getCalItemId())
        .getInvite(0).getAttendees().get(0).setPartStat(IcalXmlStrMap.PARTSTAT_ACCEPTED);

    var calendarItem = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());
    assertTrue(calendarItem.getInvite(0).getAttendees().get(0).getPartStat()
        .equalsIgnoreCase(IcalXmlStrMap.PARTSTAT_ACCEPTED));

    appointmentData.startTime = "20250907T165000";
    createCalendarItemException(appointmentData, createAppointmentResponse.getCalInvId());
    var calendarItemModified = getCalendarItemById(organizer, createAppointmentResponse.getCalItemId());

    assertTrue(
        calendarItemModified.getInvite(0).getAttendees().get(0).getPartStat()
            .equalsIgnoreCase(IcalXmlStrMap.PARTSTAT_NEEDS_ACTION));
  }

  private static CalendarItem getCalendarItemById(Account account, String id) throws ServiceException {
    return mailboxManager.getMailboxByAccount(account)
        .getCalendarItemById(null, Integer.parseInt(id));
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