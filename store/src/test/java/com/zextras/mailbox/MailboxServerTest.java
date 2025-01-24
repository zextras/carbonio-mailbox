/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.util.SoapClient;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.type.AccountSelector;
import org.apache.http.HttpResponse;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MailboxServerTest {

	private static Server mailboxServer;

	@BeforeAll
	static void setUp() throws Exception {
		final String timezoneFile = MailboxServerTest.class.getResource("/timezones-test.ics")
				.getFile();
		final String mailboxHome = MailboxServerTest.class.getResource("/").getFile();
		mailboxServer = SampleLocalMailbox.setUpServer(mailboxHome, timezoneFile);
		mailboxServer.start();
	}

	@AfterAll
	static void tearDown() throws Exception {
		mailboxServer.stop();
	}

	@Test
	void shouldStartMailboxServerListeningOnPort() throws Exception {
		try(SoapClient soapClient = new SoapClient("http://localhost:8080/service/soap")) {
			final HttpResponse httpResponse =  soapClient.newRequest()
					.setSoapBody(new AuthRequest(AccountSelector.fromName("test@test.com"), "password"))
					.directCall();

			Assertions.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
		}
	}

}