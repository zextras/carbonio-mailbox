/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.SampleLocalMailbox.ServerSetup;
import com.zextras.mailbox.util.SoapClient;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.type.AccountSelector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class MailboxServerAPITest {

	private static Server mailboxServer;
	private static final int USER_HTTP_PORT = 8080;
	private static final int USER_HTTPS_PORT = 8443;
	private static final int ADMIN_PORT = 7071;

	@BeforeAll
	static void setUp() throws Exception {
		final String mailboxHome = MailboxServerAPITest.class.getResource("/").getFile();
		final String timezoneFile = MailboxServerAPITest.class.getResource("/timezones-test.ics")
				.getFile();
		mailboxServer = new ServerSetup(mailboxHome, timezoneFile)
				.withAdminPort(ADMIN_PORT)
				.withUserPort(USER_HTTP_PORT)
				.withUserHttpsPort(USER_HTTPS_PORT)
				.create();
		mailboxServer.start();
	}

	@AfterAll
	static void tearDown() throws Exception {
		mailboxServer.stop();
	}

	private String getUserEndpoint() {
		return "http://localhost:" + USER_HTTP_PORT + "/service/soap";
	}

	private String getUserHttpsEndpoint() {
		return "https://localhost:" + USER_HTTPS_PORT + "/service/soap";
	}

	private String getAdminEndpoint() {
		return "https://localhost:" + ADMIN_PORT + "/service/admin/soap";
	}

	@Test
	void shouldAuthenticateStandardUser() throws Exception {
		try(SoapClient soapClient = new SoapClient(getUserEndpoint())) {
			final SoapResponse soapResponse = soapClient.newRequest()
					.setSoapBody(new AuthRequest(AccountSelector.fromName("test@test.com"), "password"))
					.call();

			Assertions.assertEquals(200, soapResponse.statusCode());
		}
	}

	@Test
	void shouldAuthenticateStandardUser_OnHttpsPort() throws Exception {
		try(SoapClient soapClient = new SoapClient(getUserHttpsEndpoint())) {
			final SoapResponse soapResponse = soapClient.newRequest()
					.setSoapBody(new AuthRequest(AccountSelector.fromName("test@test.com"), "password"))
					.call();

			Assertions.assertEquals(200, soapResponse.statusCode());
		}
	}

	@Test
	void shouldAuthenticateAdminUser() throws Exception {
		try(SoapClient soapClient = new SoapClient(getAdminEndpoint())) {
			final SoapResponse soapResponse =  soapClient.newRequest()
					.setSoapBody(new AuthRequest(AccountSelector.fromName("admin@test.com"), "password"))
					.call();

			Assertions.assertEquals(200, soapResponse.statusCode());
		}
	}

}