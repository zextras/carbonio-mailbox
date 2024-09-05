package com.zimbra.cs.service.mail;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator.Factory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.mail.message.CreateAppointmentRequest;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.type.CalOrganizer;
import com.zimbra.soap.mail.type.CalendarAttendee;
import com.zimbra.soap.mail.type.DtTimeInfo;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.InvitationInfo;
import com.zimbra.soap.mail.type.Msg;

@Tag("api")
class ForwardAppointmentAPITest extends SoapTestSuite {

  private static MailboxManager mailboxManager;

  private static Folder getFirstCalendar(Account user) throws ServiceException {
    final Mailbox mailbox = mailboxManager.getMailboxByAccount(user);
    final List<Folder> calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.get(0);
  }

  private static Factory accountCreatorFactory;

  private static String nextWeek() {
    final LocalDateTime now = LocalDateTime.now();
    return now.plusDays(7L).format(DateTimeFormatter.ofPattern("yMMdd"));
  }

  private Msg newAppointmentMessage(Account organizer, List<String> attendees) {
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

  private CreateAppointmentResponse createAppointment(Account authenticatedAccount, Msg msg)
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

  @BeforeAll
  static void beforeAll() throws Exception {
    Provisioning provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @Test
  void testForwardAppointment() throws Exception {

    // create 3 accounts (A, B, C)
    final Account userA = accountCreatorFactory.get().withUsername("userA").create();
    final Account userB = accountCreatorFactory.get().withUsername("userB").create();
    final Account userC = accountCreatorFactory.get().withUsername("userC").create();

    // create an appointment in A's calendar with B as attendee
    final Msg invitation = newAppointmentMessage(userA, List.of(userB.getName()));
    final CreateAppointmentResponse appointment = createAppointment(userA, invitation);

    // B forwards the appointment to C
    // get the appointment from B's calendar
    // assert that C received the appointment
    // assert that C is the only attendee
  }

}
