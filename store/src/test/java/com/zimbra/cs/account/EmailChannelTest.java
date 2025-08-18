/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ForgetPasswordEnums.CodeConstants;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EmailChannelTest extends MailboxTestSuite {

	private static GreenMail mta;

	@BeforeAll
	public static void setUp() throws Exception {
		mta =
				new GreenMail(
						new ServerSetup[]{
								new ServerSetup(
										SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
						});
		mta.start();
	}
	@BeforeEach
	public void cleanup() throws Exception {
		mta.reset();
	}

	private String getReceivedMailBody() throws MessagingException, IOException {
		MimeMessage[] receivedMessages = mta.getReceivedMessages();
		Assertions.assertEquals(1, receivedMessages.length);
		final MimeMessage receivedMessage = receivedMessages[0];
		return new String(receivedMessage.getInputStream().readAllBytes());
	}

	@Test
	void shouldSendResetPasswordURL_ToRecoveryAddress() throws Exception {
		final Account account = this.getAccountCreator().get().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);
		final String recoveryAddress = "recoveryAddress@test.com";

		EmailChannel.sendAndStoreResetPasswordURL(zsc, account,
				getRecoveryAddressCodeMap(recoveryAddress));

		MimeMessage[] receivedMessages = mta.getReceivedMessages();
		Assertions.assertEquals(1, receivedMessages.length);
		final MimeMessage receivedMessage = receivedMessages[0];

		final String recipient = receivedMessage.getAllRecipients()[0].toString();
		Assertions.assertEquals(recoveryAddress, recipient);
	}

	@Test
	void shouldSendEmailWithResetPasswordURL() throws Exception {
		final Account account = this.getAccountCreator().get().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);
		final String recoveryAddress = "recoveryAddress@test.com";

		EmailChannel.sendAndStoreResetPasswordURL(zsc, account,
				getRecoveryAddressCodeMap(recoveryAddress));

		final String eml = getReceivedMailBody();
		Assertions.assertTrue(eml.contains("Kindly click on the link to set your password : https://localhost"));
	}

	private static HashMap<String, String> getRecoveryAddressCodeMap(String recoveryAddress) {
		return new HashMap<>(Map.of(
				CodeConstants.EXPIRY_TIME.toString(), "1000",
				CodeConstants.EMAIL.toString(), recoveryAddress
		));
	}
	private static HashMap<String, String> getRecoveryCodeMap(String recoveryAddress, String code) {
		return new HashMap<>(Map.of(
				CodeConstants.EXPIRY_TIME.toString(), "1000",
				CodeConstants.EMAIL.toString(), recoveryAddress,
				CodeConstants.CODE.toString(), code
		));
	}

	@Test
	void shouldSendEmailWithRecoveryCode() throws Exception {
		final Account account = this.getAccountCreator().get().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);
		final String recoveryAddress = "recoveryAddress@test.com";

		new EmailChannel().sendAndStoreSetRecoveryAccountCode(account, MailboxManager.getInstance()
				.getMailboxByAccount(account), getRecoveryCodeMap(recoveryAddress, "123"), zsc, null, new HashMap<>());

		final String receivedMailBody = getReceivedMailBody();
		System.out.println(receivedMailBody);
		Assertions.assertTrue(receivedMailBody.contains("Recovery email verification code: 123"));
	}

	private static Stream<Arguments> dateTestCases() {
		return Stream.of(
				Arguments.of(("Europe/Berlin"), "Thu, 1 Jan 1970 (.*) CET"),
				Arguments.of(("Europe/Paris"), "Thu, 1 Jan 1970 (.*) CET"),
				Arguments.of(("Europe/Rome"), "Thu, 1 Jan 1970 (.*) CET"),
				Arguments.of(("America/Los_Angeles"), "Wed, 31 Dec 1969 (.*) PST"),
				Arguments.of(("Asia/Singapore"), "Thu, 1 Jan 1970 (.*) SGT"),
				Arguments.of(("Africa/Nairobi"), "Thu, 1 Jan 1970 (.*) EAT"),
				Arguments.of(("Pacific/Auckland"), "Thu, 1 Jan 1970 (.*) NZST")
		);
	}

	@ParameterizedTest
	@MethodSource("dateTestCases")
	void shouldSendResetPasswordURLMail_WithAccountPrefTimezone(String timeZone, String expected) throws Exception {
		final Account account = this.getAccountCreator().get().withAttribute(Provisioning.A_zimbraPrefTimeZoneId, timeZone)
				.create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);
		final String recoveryAddress = "recoveryAddress@test.com";

		EmailChannel.sendAndStoreResetPasswordURL(zsc, account,
				getRecoveryAddressCodeMap(recoveryAddress));

		MimeMessage[] receivedMessages = mta.getReceivedMessages();
		Assertions.assertEquals(1, receivedMessages.length);

		Pattern pattern = Pattern.compile("The link expires by: " + expected, Pattern.DOTALL);
		final String eml = getReceivedMailBody();
		Matcher matcher = pattern.matcher(eml);
		Assertions.assertTrue(matcher.find());
	}
}