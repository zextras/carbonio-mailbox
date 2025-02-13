/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.SampleLocalMailbox.ServerSetup;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.type.AccountSelector;
import org.apache.http.HttpResponse;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MailboxServerIT {

	private static Server mailboxServer;
	private static final int USER_PORT = 8080;

	@BeforeAll
	static void setUp() throws Exception {
		final String mailboxHome = MailboxServerIT.class.getResource("/").getFile();
		final String timezoneFile = MailboxServerIT.class.getResource("/timezones-test.ics")
				.getFile();
		mailboxServer = new ServerSetup(USER_PORT, mailboxHome, timezoneFile).create();
		mailboxServer.start();
	}

	@AfterAll
	static void tearDown() throws Exception {
		mailboxServer.stop();
	}

	private String getUserEndpoint() {
		return "http://localhost:" + USER_PORT + "/service/soap";
	}
	@Test
	void shouldAuthenticateStandardUser() throws Exception {
		try(SoapClient soapClient = new SoapClient(getUserEndpoint())) {
			final HttpResponse httpResponse =  soapClient.newRequest()
					.setSoapBody(new AuthRequest(AccountSelector.fromName("test@test.com"), "password"))
					.execute();

			Assertions.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
		}
	}

}