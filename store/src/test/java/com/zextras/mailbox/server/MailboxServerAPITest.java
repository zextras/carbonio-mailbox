/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.server;

import com.zextras.mailbox.servlet.ApiCdiContextHandler;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.SoapClient;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.type.AccountSelector;
import java.util.Map;
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

	private static final String PASSWORD = "password";

	@Test
	void shouldAuthenticateStandardUser() throws Exception {
		final Account account = server.getAccountFactory()
				.withPassword(PASSWORD)
				.create();
		SoapClient soapClient = new SoapClient(getUserEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), PASSWORD))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldAuthenticateStandardUser_OnHttpsPort() throws Exception {
		final Account account = server.getAccountFactory()
				.withPassword(PASSWORD)
				.create();
		SoapClient soapClient = new SoapClient(getUserHttpsEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), PASSWORD))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldAuthenticateAdminUser() throws Exception {
		final Account account = server.getAccountFactory()
				.withPassword(PASSWORD)
				.asGlobalAdmin()
				.create();
		SoapClient soapClient = new SoapClient(getAdminEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), PASSWORD))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void internalApiShouldNotBeReachableOnUserHttpPort() throws Exception {
		final Account account = server.getAccountFactory().create();
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/internal/accounts/" + account.getId() + "/info");
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void internalApiShouldNotBeReachableOnUserHttpsPort() throws Exception {
		final Account account = server.getAccountFactory().create();
		var response = server.getHttpClient().get(
				"https://localhost:" + server.getUserHttpsPort() + "/internal/accounts/" + account.getId() + "/info");
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void internalApiShouldNotBeReachableOnAdminPort() throws Exception {
		final Account account = server.getAccountFactory().create();
		var response = server.getHttpClient().get(
				"https://localhost:" + server.getAdminPort() + "/internal/accounts/" + account.getId() + "/info");
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void shouldNotExposeInternalEndpointWhenMatchingHost() throws Exception {
		final Account account = server.getAccountFactory().create();
		var headers = Map.of("Host", ApiCdiContextHandler.INTERNAL_CONNECTOR_NAME);
		var response = server.getHttpClient().get("http://localhost:" + server.getUserHttpPort() + "/internal/accounts/" + account.getId() + "/info", headers);
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void internalApiReachableOnInternalPort() throws Exception {
		final Account account = server.getAccountFactory().create();
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/internal/accounts/" + account.getId() + "/info");
		Assertions.assertEquals(200, response.statusCode());
	}

	@Test
	void healthAnswersAtInternalPort() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/health/ready");
		Assertions.assertEquals(200, response.statusCode());
	}

	@Test
	void healthLiveOnInternalPort() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/health/live");
		Assertions.assertEquals(200, response.statusCode());
	}

	@Test
	void healthOnInternalPortShouldReturnJson() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/health");
		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertTrue(response.body().contains("ready"));
		Assertions.assertTrue(response.body().contains("dependencies"));
	}

	@Test
	void unknownPathOnHttpPortShouldReturn404() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/nonexistent/path");
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void unknownPathOnHttpsPortShouldReturn404() throws Exception {
		var response = server.getHttpClient().get(
				"https://localhost:" + server.getUserHttpsPort() + "/nonexistent/path");
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void unknownPathOnInternalPortShouldReturn404() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/nonexistent/path");
		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	void davEndpointShouldBeServiced() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/dav/");
		Assertions.assertNotEquals(404, response.statusCode());
	}

	@Test
	void homeEndpointShouldBeServiced() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/home/");
		Assertions.assertNotEquals(404, response.statusCode());
	}

	@Test
	void userEndpointShouldBeServiced() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/user/");
		Assertions.assertNotEquals(404, response.statusCode());
	}

	@Test
	void shouldAuthenticateStandardUserOnUserHttpPortUsingFullServicePath() throws Exception {
		final Account account = server.getAccountFactory()
				.withPassword(PASSWORD)
				.create();
		SoapClient soapClient = new SoapClient("http://localhost:" + server.getUserHttpPort() + "/service/soap/");
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), PASSWORD))
				.call();
		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldReturnSuccessForAdminAuthOnHttpsAdminPort() throws Exception {
		final Account account = server.getAccountFactory()
				.withPassword(PASSWORD)
				.asGlobalAdmin()
				.create();
		SoapClient soapClient = new SoapClient("https://localhost:" + server.getAdminPort() + "/service/admin/soap/");
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName(account.getName()), PASSWORD))
				.call();
		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void adminSoapOnUserHttpShouldReturnServerError() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/service/admin/soap/");
		Assertions.assertNotEquals(200, response.statusCode());
	}

	@Test
	void adminSoapOnInternalPortShouldReturnServerError() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/service/admin/soap/");
		Assertions.assertNotEquals(200, response.statusCode());
	}

	@Test
	void internalApiShouldReturn404ForUnknownAccount() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/internal/accounts/nonexistent-id/info");
		Assertions.assertNotEquals(500, response.statusCode());
	}

	@Test
	void serviceHealthShouldBeReachableOnUserHttpPort() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/health/");
		Assertions.assertEquals(200, response.statusCode());
	}

	@Test
	void contentEndpointShouldBeServiced() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/service/content/");
		Assertions.assertNotEquals(404, response.statusCode());
	}

	@Test
	void proxyEndpointShouldBeServiced() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/service/proxy/");
		Assertions.assertNotEquals(404, response.statusCode());
	}

	@Test
	void extensionEndpointShouldNotReturnServerError() throws Exception {
		var response = server.getHttpClient().get(
				"http://localhost:" + server.getUserHttpPort() + "/service/extension/");
		Assertions.assertNotEquals(500, response.statusCode());
	}
}
