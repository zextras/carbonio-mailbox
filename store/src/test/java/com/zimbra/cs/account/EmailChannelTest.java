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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


@Tag("flaky")
class EmailChannelTest extends MailboxTestSuite {

	private static GreenMail mta;
	private static final String RECOVERY_CODE = "123";
	private static final String RECOVERY_ADDRESS = "recoveryAddress@test.com";

	@BeforeAll
	static void setUp() {
		mta =
				new GreenMail(
						new ServerSetup[]{
								new ServerSetup(
										SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
						});
		mta.start();
	}
	@BeforeEach
	void cleanup() {
		mta.reset();
	}

	@AfterAll
	static void tearDown() {
		mta.stop();
	}

	@Test
	void shouldSendResetPasswordURL_ToRecoveryAddress() throws Exception {
		final Account account = createAccount().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);

		EmailChannel.sendAndStoreResetPasswordURL(zsc, account,
				getRecoveryMap());

		MimeMessage[] receivedMessages = mta.getReceivedMessages();
		Assertions.assertEquals(1, receivedMessages.length);
		final MimeMessage receivedMessage = receivedMessages[0];

		final String recipient = receivedMessage.getAllRecipients()[0].toString();
		Assertions.assertEquals(RECOVERY_ADDRESS, recipient);
	}

	@Test
	void shouldSendEmailWithResetPasswordURL() throws Exception {
		final Account account = createAccount().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);

		EmailChannel.sendAndStoreResetPasswordURL(zsc, account,
				getRecoveryMap());

		final String eml = getReceivedMailBody();
		Assertions.assertTrue(eml.contains("Kindly click on the link to set your password : https://localhost"));
	}

	@Test
	void shouldSendEmailWithRecoveryCode() throws Exception {
		final Account account = createAccount().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);

		new EmailChannel().sendAndStoreSetRecoveryAccountCode(account, MailboxManager.getInstance()
				.getMailboxByAccount(account), getRecoveryMap(), zsc, null, new HashMap<>());

		final String receivedMailBody = getReceivedMailBody();
		Assertions.assertTrue(receivedMailBody.contains("Recovery email verification code: " + RECOVERY_CODE));
	}

	@Test
	void shouldSendEmailWithResetPasswordRecoveryCode() throws Exception {
		final Account account = createAccount().create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);

		new EmailChannel().sendAndStoreResetPasswordRecoveryCode(zsc, account, getRecoveryMap());

		final String receivedMailBody = getReceivedMailBody();
		Assertions.assertTrue(receivedMailBody.contains("Temporary Access Code: " + RECOVERY_CODE));
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
		final Account account = createAccount().withAttribute(Provisioning.A_zimbraPrefTimeZoneId, timeZone)
				.create();
		ZimbraSoapContext zsc = ServiceTestUtil.getSoapContext(account);

		EmailChannel.sendAndStoreResetPasswordURL(zsc, account,
				getRecoveryMap());

		MimeMessage[] receivedMessages = mta.getReceivedMessages();
		Assertions.assertEquals(1, receivedMessages.length);

		Pattern pattern = Pattern.compile("The link expires by: " + expected, Pattern.DOTALL);
		final String eml = getReceivedMailBody();
		Matcher matcher = pattern.matcher(eml);
		Assertions.assertTrue(matcher.find());
	}

	private static HashMap<String, String> getRecoveryMap() {
		return new HashMap<>(Map.of(
				CodeConstants.EXPIRY_TIME.toString(), "1000",
				CodeConstants.EMAIL.toString(), RECOVERY_ADDRESS,
				CodeConstants.CODE.toString(), RECOVERY_CODE
		));
	}

	private String getReceivedMailBody() throws MessagingException, IOException {
		MimeMessage[] receivedMessages = mta.getReceivedMessages();
		Assertions.assertEquals(1, receivedMessages.length);
		final MimeMessage receivedMessage = receivedMessages[0];
		return new String(receivedMessage.getInputStream().readAllBytes());
	}
}