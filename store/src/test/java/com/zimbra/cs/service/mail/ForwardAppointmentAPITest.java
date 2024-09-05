package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator.Factory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
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
	private static GreenMail greenMail;

	@BeforeAll
	static void beforeAll() throws Exception {
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
	void shouldReplaceAttendeesWithForwardeesWhenForwardingAppointment() throws Exception {
		final Account userA = accountCreatorFactory.get().withUsername("userA").create();
		final Account userB = accountCreatorFactory.get().withUsername("userB").create();
		final Account userC = accountCreatorFactory.get().withUsername("userC").create();
		final Account userD = accountCreatorFactory.get().withUsername("userD").create();
		createAppointment(userA, List.of(userB, userD));
		greenMail.reset();
		final List<CalendarItem> calendarItems = getCalendarAppointments(userB);
		final CalendarItem userBAppointment = calendarItems.get(0);
		forwardAppointment(userB, String.valueOf(userBAppointment.getId()), userC.getName());

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final String ics = extractIcsFromMessage(receivedMessage);
		Assertions.assertTrue(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:userc@test.com"));
		Assertions.assertFalse(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:userb@test.com"));
		Assertions.assertFalse(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:userd@test.com"));
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

	private CreateAppointmentResponse createAppointment(Account organizer, List<Account> attendees) throws Exception {
		final Msg invitation = newAppointmentMessage(organizer, attendees.stream().map(Account::getName).collect(
				Collectors.toList()));
		final CreateAppointmentResponse appointmentResponse = createAppointment(organizer, invitation);
		final Invite invite = getCalendarItemById(organizer, appointmentResponse).getInvite(0);
		for(Account attendee: attendees) {
			mailboxManager.getMailboxByAccount(attendee)
					.addInvite(null, invite, getFirstCalendar(attendee).getFolderId());
		}
		return appointmentResponse;
	}
	private static String extractIcsFromMessage(MimeMessage receivedMessage)
			throws IOException, MessagingException {
		return new String(
				((MimeMultipart) receivedMessage.getContent()).getBodyPart(2).getInputStream()
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
