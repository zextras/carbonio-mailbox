/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.service.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.PortUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox.AddInviteData;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.ZAttendee;
import com.zimbra.cs.mailbox.calendar.ZOrganizer;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.SendInviteReplyRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
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
						new ServerSetup[]{
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
	void shouldNotUse_SentBy_FieldOfOrganizer_toSendNotification() throws Exception {
		final Account attendee = getCreateAccountFactory().get().
				withAttribute(ZAttrProvisioning.A_zimbraPrefDeleteInviteOnReply, "FALSE")
				.create();
		final AccountAction onAttendee = getAccountActionFactory().forAccount(attendee);
		Invite invite = createAttendeeInvite();
		invite.setUid(UUID.randomUUID().toString());
		final ZOrganizer organizer = new ZOrganizer("test@domain.com", "Test");
		organizer.setSentBy("otherAddress@otherDomain.com");
		invite.setOrganizer(organizer);
		invite.addAttendee(new ZAttendee(attendee.getName()));
		final AddInviteData addInviteData = onAttendee.storeInvite(invite);
		greenMail.reset();

		final var request = new SendInviteReplyRequest(addInviteData.calItemId
				+ "-" + addInviteData.invId, addInviteData.compNum, "ACCEPT");
		request.setUpdateOrganizer(true);
		this.getSoapClient().executeSoap(attendee, request);

		final MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		final String eml = getEml(receivedMessage);
		Assertions.assertTrue(eml.contains("To: " + "Test <test@domain.com>"));
	}

	private Invite createAttendeeInvite() {
		final TimeZoneMap timeZoneMap = new TimeZoneMap(WellKnownTimeZones.getTimeZoneById("EST"));
		return new Invite("REQUEST", timeZoneMap, false);
	}

	private static String getEml(MimeMessage receivedMessage)
			throws IOException, MessagingException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		receivedMessage.writeTo(byteArrayOutputStream);
		return byteArrayOutputStream.toString();
	}

}