package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.CreateAccount.Factory;
import com.zextras.mailbox.util.PortUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.type.Msg;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

	private static Factory createAccountFactory;
	private static AccountAction.Factory accountActionFactory;
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
		createAccountFactory = getCreateAccountFactory();
		accountActionFactory = getAccountActionFactory();
	}
	@BeforeEach
	void beforeEach() {
		greenMail.reset();
	}

	@Test
	void shouldAddForwardeeToCurrentAttendeesWhenForwardingAppointment() throws Exception {
		final Account userA = createAccountFactory.get().withUsername("userA").create();
		final Account userB = createAccountFactory.get().withUsername("userB").create();
		final Account userC = createAccountFactory.get().withUsername("userC").create();
		final Account userD = createAccountFactory.get().withUsername("userD").create();
		createAppointment(userA, List.of(userB, userD));
		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		MimeMessage receivedMessage1 = receivedMessages[0];
		final String ics1 = extractIcsFromMessage(receivedMessage1, 1);
		assertTrue(ics1.contains("ATTENDEE;CN=userb@test.com;ROLE=REQ-PARTICIPANT:mailto:userb@test.com"));
		assertTrue(ics1.contains("ATTENDEE;CN=userd@test.com;ROLE=REQ-PARTICIPANT:mailto:userd@test.com"));
		greenMail.reset();
		final List<CalendarItem> calendarItems = accountActionFactory.forAccount(userB).getCalendarAppointments();
		final CalendarItem userBAppointment = calendarItems.get(0);
		forwardAppointmentSoap(userB, String.valueOf(userBAppointment.getId()), userC.getName());

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final String ics = extractIcsFromMessage(receivedMessage, 2);
		
		assertTrue(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;" +
				"ROLE=REQ-PARTICIPANT:mailto:userc@t\r\n est.com"));
		assertTrue(ics.contains("ATTENDEE;CN=userb@test.com;ROLE=REQ-PARTICIPANT:mailto:userb@test.com"));
		assertTrue(ics.contains("ATTENDEE;CN=userd@test.com;ROLE=REQ-PARTICIPANT:mailto:userd@test.com"));
	}

	@Test
	void shouldSendEmailOnlyToNewAttendee_WhenForwarding() throws Exception {
		final Account userA = createAccountFactory.get().create();
		final Account userB = createAccountFactory.get().create();
		final Account userC = createAccountFactory.get().create();
		createAppointment(userA, List.of(userB, userC));
		greenMail.reset();

		final Account userD = createAccountFactory.get().create();
		final List<CalendarItem> calendarItems = accountActionFactory.forAccount(userB).getCalendarAppointments();
		final CalendarItem userBAppointment = calendarItems.get(0);

		forwardAppointmentSoap(userB, String.valueOf(userBAppointment.getId()), userD.getName());

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
	void should_NOT_NotifyOrganizer_ThatItsAppointmentHasBeenForwarded() throws Exception {
		final Account userA = createAccountFactory.get().create();
		final Account userB = createAccountFactory.get().create();
		final Account userC = createAccountFactory.get().create();
		createAppointment(userA, List.of(userB, userC));
		greenMail.reset();

		final Account userD = createAccountFactory.get().create();
		final List<CalendarItem> calendarItems = accountActionFactory.forAccount(userB).getCalendarAppointments();
		final CalendarItem userBAppointment = calendarItems.get(0);

		forwardAppointmentSoap(userB, String.valueOf(userBAppointment.getId()), userD.getName());
		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		MimeMessage receivedMessage = receivedMessages[0];
		assertEquals(1, receivedMessages.length);

		final String messageContent = new String(receivedMessage.getRawInputStream().readAllBytes());
    assertFalse(messageContent.contains("Your meeting was forwarded"));
    assertFalse(messageContent.contains("<" + userB.getName() + ">  has forwarded your meeting request " +
        "to additional recipients."));
	}

	@Test
	void originalOrganizerMustNotBeInFrom() throws Exception {
		final Account organizer = createAccountFactory.get().create();
		final Account attendee = createAccountFactory.get().create();
		createAppointment(organizer, List.of(attendee));
		greenMail.reset();
		final Account otherUser = createAccountFactory.get().create();
		final List<CalendarItem> calendarItems = accountActionFactory.forAccount(attendee).getCalendarAppointments();
		final CalendarItem attendeeAppointment = calendarItems.get(0);

		forwardAppointmentSoap(attendee, String.valueOf(attendeeAppointment.getId()), otherUser.getName());

		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		MimeMessage receivedMessage = receivedMessages[0];
		assertEquals(1, receivedMessages.length);

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		receivedMessage.writeTo(byteArrayOutputStream);
		final String eml = byteArrayOutputStream.toString();
		assertTrue(eml.contains("From: " + attendee.getName()));
		assertFalse(eml.contains("Sender: "));
	}

	private static CalendarItem getCalendarItemById(Account account,
			CreateAppointmentResponse appointmentResponse) throws ServiceException {
		return mailboxManager.getMailboxByAccount(account)
				.getCalendarItemById(null, Integer.parseInt(appointmentResponse.getCalItemId()));
	}

  private CreateAppointmentResponse createAppointment(Account organizer, List<Account> attendees) throws Exception {
		final Msg invitation = defaultAppointmentMessage(organizer, attendees.stream().map(Account::getName).toList());
		final CreateAppointmentResponse appointmentResponse = createAppointmentSoap(organizer, invitation);
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


}
