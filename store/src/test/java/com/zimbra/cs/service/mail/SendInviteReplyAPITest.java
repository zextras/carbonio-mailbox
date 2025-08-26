/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.service.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.PortUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.CreateAppointmentResponse;
import com.zimbra.soap.mail.message.SendInviteReplyRequest;
import com.zimbra.soap.mail.type.Msg;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class SendInviteReplyAPITest extends SoapTestSuite {

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
	}

	@BeforeEach
	void beforeEach() {
		greenMail.reset();
	}


	@Test
	void shouldNotifyOrganizer() throws Exception {
		final Account organizer = getCreateAccountFactory().get().create();
		final Account attendee = getCreateAccountFactory().get().create();
		final Msg invitation = defaultAppointmentMessage(organizer, List.of(attendee.getName()));

		final CreateAppointmentResponse appointmentResponse = createAppointmentSoap(organizer, invitation);
		// TODO: store invite in attendee's mailbox
		final String calInvId = appointmentResponse.getCalInvId();


		final List<CalendarItem> calendarItems = getAccountActionFactory().forAccount(attendee).getCalendarAppointments();
		final CalendarItem attendeeAppointment = calendarItems.get(0);
		greenMail.reset();


		final var request = new SendInviteReplyRequest("", 0, "ACCEPT");
		this.getSoapClient().executeSoap(attendee, request);

		final MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final String eml = getEml(receivedMessage);

	}

	private static String getEml(MimeMessage receivedMessage)
			throws IOException, MessagingException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		receivedMessage.writeTo(byteArrayOutputStream);
		return byteArrayOutputStream.toString();
	}

}