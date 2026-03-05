/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.server;

import com.zextras.mailbox.api.InternalApiContextHandler;
import com.zextras.mailbox.util.CreateAccount.Factory;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.SoapClient;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zextras.mailbox.util.TestHttpClient;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.type.AccountSelector;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class MailboxServerAPITest {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private String getUserEndpoint() {
		return "http://localhost:" + server.getUserHttpPort() + "/service/soap";
	}

	private String getUserHttpsEndpoint() {
		return "https://localhost:" + server.getUserHttpsPort() + "/service/soap";
	}

	private String getAdminEndpoint() {
		return "https://localhost:" + server.getAdminPort() + "/service/admin/soap";
	}

	@Test
	void shouldAuthenticateStandardUser() throws Exception {
		final Account account = server.getAccountFactory().get().create();
		SoapClient soapClient = new SoapClient(getUserEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), "password"))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldAuthenticateStandardUser_OnHttpsPort() throws Exception {
		final Account account = server.getAccountFactory().get().create();
		SoapClient soapClient = new SoapClient(getUserHttpsEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), "password"))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldAuthenticateAdminUser() throws Exception {
		final Account account = server.getAccountFactory().get().asGlobalAdmin().create();
		SoapClient soapClient = new SoapClient(getAdminEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), "password"))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void internalApiPingShouldNotBeReachableOnUserHttpPort() throws Exception {
		try (TestHttpClient client = new TestHttpClient()) {
			final Response response = client.execute(
					new HttpGet("http://localhost:" + server.getUserHttpPort() + "/internal/ping"));

			Assertions.assertEquals(404, response.statusCode());
		}
	}
	@Test
	void internalApiPingShouldNotBeReachableOnUserHttpsPort() throws Exception {
		try (TestHttpClient client = new TestHttpClient()) {
			final Response response = client.execute(
					new HttpGet("https://localhost:" + server.getUserHttpsPort() + "/internal/ping"));

			Assertions.assertEquals(404, response.statusCode());
		}
	}

	@Test
	void internalApiPingShouldNotBeReachableOnAdminPort() throws Exception {
		try (TestHttpClient client = new TestHttpClient()) {
			final Response response = client.execute(
					new HttpGet("https://localhost:" + server.getAdminPort() + "/internal/ping"));

			Assertions.assertEquals(404, response.statusCode());
		}
	}

	@Test
	void shouldNotExposeInternalEndpointWhenMatchingHost() throws Exception {
		var request = new HttpGet("http://localhost:" + server.getUserHttpPort() + "/internal/ping");
		request.setHeader("Host", InternalApiContextHandler.CONNECTOR_NAME);
		try (TestHttpClient client = new TestHttpClient()) {
			final Response response = client.execute(request);

			Assertions.assertEquals(404, response.statusCode());
		}
	}

	@Test
	void internalApiReachableOnInternalPort() throws Exception {
		try (TestHttpClient client = new TestHttpClient()) {
			final Response response = client.execute(
					new HttpGet("http://localhost:" + server.getInternalApiPort() + "/internal/ping"));

			Assertions.assertEquals(200, response.statusCode());
		}
	}

}
