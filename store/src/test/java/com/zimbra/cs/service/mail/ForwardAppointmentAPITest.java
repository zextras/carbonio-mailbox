package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.PortUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.type.CalOrganizer;
import com.zimbra.soap.mail.type.CalendarAttendee;
import com.zimbra.soap.mail.type.DtTimeInfo;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.InvitationInfo;
import com.zimbra.soap.mail.type.Msg;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class ForwardAppointmentAPITest extends SoapTestSuite {

	private static MailboxManager mailboxManager;

	private static Folder getFirstCalendar(Account user) throws ServiceException {
    final Mailbox mailbox = mailboxManager.getMailboxByAccount(user);
    final List<Folder> calendarFolders = mailbox.getCalendarFolders(null, SortBy.DATE_DESC);
    return calendarFolders.get(0);
  }

	
	private static GreenMail greenMail;

	@BeforeAll
	static void beforeAll() throws Exception {
		var smtpPort = PortUtil.findFreePort();
		greenMail =
				new GreenMail(
						new ServerSetup[] {
								new ServerSetup(smtpPort, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
						});
		greenMail.start();
		Provisioning provisioning = Provisioning.getInstance();
		provisioning.getLocalServer().setSmtpPort(smtpPort);
		mailboxManager = MailboxManager.getInstance();
		
	}
	@BeforeEach
	void beforeEach() {
		greenMail.reset();
	}

	@Test
	void shouldAddForwardeeToCurrentAttendeesWhenForwardingAppointment() throws Exception {
		final Account userA = getCreateAccountFactory().withUsername("userA").create();
		final Account userB = getCreateAccountFactory().withUsername("userB").create();
		final Account userC = getCreateAccountFactory().withUsername("userC").create();
		final Account userD = getCreateAccountFactory().withUsername("userD").create();
		createAppointment(userA, List.of(userB, userD));
		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		MimeMessage receivedMessage1 = receivedMessages[0];
		final String ics1 = extractIcsFromMessage(receivedMessage1, 1);
		assertTrue(ics1.contains("ATTENDEE;CN=userb@test.com;ROLE=REQ-PARTICIPANT:mailto:userb@test.com"));
		assertTrue(ics1.contains("ATTENDEE;CN=userd@test.com;ROLE=REQ-PARTICIPANT:mailto:userd@test.com"));
		greenMail.reset();
		final List<CalendarItem> calendarItems = getCalendarAppointments(userB);
		final CalendarItem userBAppointment = calendarItems.get(0);
		forwardAppointment(userB, String.valueOf(userBAppointment.getId()), userC.getName());

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final String ics = extractIcsFromMessage(receivedMessage, 2);
		
		assertTrue(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;" +
				"ROLE=REQ-PARTICIPANT:mailto:userc@t\r\n est.com"));
		assertTrue(ics.contains("ATTENDEE;CN=userb@test.com;ROLE=REQ-PARTICIPANT:mailto:userb@test.com"));
		assertTrue(ics.contains("ATTENDEE;CN=userd@test.com;ROLE=REQ-PARTICIPANT:mailto:userd@test.com"));
	}

	@Test
	void shouldSendEmailOnlyToNewAttendeeWhenForwarding() throws Exception {
		final Account userA = getCreateAccountFactory().create();
		final Account userB = getCreateAccountFactory().create();
		final Account userC = getCreateAccountFactory().create();
		createAppointment(userA, List.of(userB, userC));
		greenMail.reset();

		final Account userD = getCreateAccountFactory().create();
		final List<CalendarItem> calendarItems = getCalendarAppointments(userB);
		final CalendarItem userBAppointment = calendarItems.get(0);

		forwardAppointment(userB, String.valueOf(userBAppointment.getId()), userD.getName());

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final Address[] recipients = receivedMessage.getRecipients(RecipientType.TO);
		assertEquals(1, recipients.length);
		assertEquals(userD.getName(), recipients[0].toString());
	}

	/**
	 * Since CO-1668 we don't notify the organizer that its appointment has been forwarded
	 * All the notifications logic was removed in this process
	 * @throws Exception if any
	 */
	@Test
	void shouldNotNotifyOrganizerThatItsAppointmentHasBeenForwarded() throws Exception {
		final Account userA = getCreateAccountFactory().create();
		final Account userB = getCreateAccountFactory().create();
		final Account userC = getCreateAccountFactory().create();
		createAppointment(userA, List.of(userB, userC));
		greenMail.reset();

		final Account userD = getCreateAccountFactory().create();
		final List<CalendarItem> calendarItems = getCalendarAppointments(userB);
		final CalendarItem userBAppointment = calendarItems.get(0);

		forwardAppointment(userB, String.valueOf(userBAppointment.getId()), userD.getName());
		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		MimeMessage receivedMessage = receivedMessages[0];
		assertEquals(1, receivedMessages.length);

		final String messageContent = new String(receivedMessage.getRawInputStream().readAllBytes());
    assertFalse(messageContent.contains("Your meeting was forwarded"));
    assertFalse(messageContent.contains("<" + userB.getName() + ">  has forwarded your meeting request " +
        "to additional recipients."));
	}

	private static List<CalendarItem> getCalendarAppointments(Account userB) throws ServiceException {
		return mailboxManager.getMailboxByAccount(userB)
				.getCalendarItems(null, Type.APPOINTMENT, getFirstCalendar(userB).getFolderId());
	}

	private static CalendarItem getCalendarItemById(Account account,
			CreateAppointmentResponse appointmentResponse) throws ServiceException {
		return mailboxManager.getMailboxByAccount(account)
				.getCalendarItemById(null, Integer.parseInt(appointmentResponse.getCalItemId()));
	}

	@SuppressWarnings("UnusedReturnValue")
  private CreateAppointmentResponse createAppointment(Account organizer, List<Account> attendees) throws Exception {
		final Msg invitation = newAppointmentMessage(organizer, attendees.stream().map(Account::getName).toList());
		final CreateAppointmentResponse appointmentResponse = createAppointment(organizer, invitation);
		final Invite invite = getCalendarItemById(organizer, appointmentResponse).getInvite(0);
		for(Account attendee: attendees) {
			mailboxManager.getMailboxByAccount(attendee)
					.addInvite(null, invite, getFirstCalendar(attendee).getFolderId());
		}
		return appointmentResponse;
	}
	private static String extractIcsFromMessage(MimeMessage receivedMessage, int bodyPart)
			throws IOException, MessagingException {
		return new String(
				((MimeMultipart) receivedMessage.getContent()).getBodyPart(bodyPart).getInputStream()
						.readAllBytes());
	}

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

}
