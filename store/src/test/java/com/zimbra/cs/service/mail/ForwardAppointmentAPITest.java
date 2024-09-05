package com.zimbra.cs.service.mail;

import static com.zimbra.client.ZEmailAddress.EMAIL_TYPE_TO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.ForwardAppointmentRequest;
import com.zimbra.soap.mail.message.ForwardAppointmentResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
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

		final Msg invitation = newAppointmentMessage(userA, List.of(userB.getName(), userD.getName()));
		final CreateAppointmentResponse appointmentResponse = createAppointment(userA, invitation);
		final Mailbox mailboxA = mailboxManager.getMailboxByAccount(userA);
		final Invite invite = mailboxA.getCalendarItemById(null, Integer.parseInt(appointmentResponse.getCalItemId())).getInvite(0);

		Assertions.assertEquals(userB.getName(), invite.getAttendees().get(0).getAddress());

		final int invId = mailboxManager.getMailboxByAccount(userB)
				.addInvite(null, invite, getFirstCalendar(userB).getFolderId()).invId;
		greenMail.reset();
		forwardAppointmentTo(userB, String.valueOf(invId), userC.getName());

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final String ics = extractIcsFromMessage(receivedMessage);
		Assertions.assertTrue(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:userc@test.com"));
		Assertions.assertFalse(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:userb@test.com"));
		Assertions.assertFalse(ics.contains("ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:userd@test.com"));
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

	private ForwardAppointmentResponse forwardAppointmentTo(Account authenticatedAccount, String appointmentId, String to)
			throws Exception {

		final ForwardAppointmentRequest forwardAppointmentRequest = new ForwardAppointmentRequest();
		forwardAppointmentRequest.setId(appointmentId);
		final Msg msg = new Msg();
		msg.setEmailAddresses(List.of(new EmailAddrInfo(to, EMAIL_TYPE_TO)));
		forwardAppointmentRequest.setMsg(msg);

		final HttpResponse response = getSoapClient().executeSoap(authenticatedAccount,
				forwardAppointmentRequest);
		String soapResponse = SoapUtils.getResponse(response);
		Assertions.assertEquals(200, response.getStatusLine().getStatusCode(),
				"Forward appointment failed with:\n" + soapResponse);
		return SoapUtils.getSoapResponse(soapResponse, MailConstants.E_FORWARD_APPOINTMENT_RESPONSE,
				ForwardAppointmentResponse.class);
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
