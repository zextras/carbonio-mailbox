/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.nginx;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.JettyServerFactory.ServerWithConfiguration;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionUtil;
import java.net.URI;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class NginxLookupExtensionTest extends MailboxTestSuite {

	private static String endpoint;
	private static Server server;
	private static Account account;
	private static final String AUTH_STATUS_HEADER = "Auth-Status";

	@BeforeAll
	static void setup() throws Exception {
		ExtensionUtil.initAll();
		final var extensionDispatcherServlet = new ServletHolder(ExtensionDispatcherServlet.class);
		extensionDispatcherServlet.setName("ExtensionDispatcherServlet");
		var servlet = new JettyServerFactory().addServlet("/", extensionDispatcherServlet);
		final ServerWithConfiguration serverConfig = servlet.create();
		account = createAccount().create();
		endpoint = "http://localhost:" + serverConfig.serverPort() + "/service/extension/nginx-lookup";
		server = serverConfig.server();
		server.start();
	}

	@AfterAll
	static void teardown() throws Exception {
		server.stop();
	}

	@Test
	void shouldReturnAuthStatusOK_WhenAccountExists() throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpGet request = baseRequest();
			request.addHeader("Auth-User", account.getName());
			request.addHeader("Auth-Method", "passwd");
			request.addHeader("Auth-Protocol", "http");

			final CloseableHttpResponse response = client.execute(request);

			final Header authStatus = response.getFirstHeader(AUTH_STATUS_HEADER);
			Assertions.assertEquals("OK", authStatus.getValue());
		}
	}

	@Test
	void shouldReturnAuthStatusError_WhenAuthUserMissing() throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpGet request = baseRequest();
			request.addHeader("Auth-Method", "passwd");
			request.addHeader("Auth-Protocol", "http");

			final CloseableHttpResponse response = client.execute(request);

			final Header authStatus = response.getFirstHeader(AUTH_STATUS_HEADER);
			Assertions.assertEquals("missing header field Auth-User", authStatus.getValue());
		}
	}

	@Test
	void shouldReturnAuthStatusError_WhenAuthMethodMissing() throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpGet request = baseRequest();
			request.addHeader("Auth-User", account.getName());
			request.addHeader("Auth-Protocol", "http");

			final CloseableHttpResponse response = client.execute(request);

			final Header authStatus = response.getFirstHeader(AUTH_STATUS_HEADER);
			Assertions.assertEquals("missing header field Auth-Method", authStatus.getValue());
		}
	}

	@Test
	void shouldReturnAuthStatusError_WhenAuthProtocolMissing() throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpGet request = baseRequest();
			request.addHeader("Auth-User", account.getName());
			request.addHeader("Auth-Method", "passwd");

			final CloseableHttpResponse response = client.execute(request);

			final Header authStatus = response.getFirstHeader(AUTH_STATUS_HEADER);
			Assertions.assertEquals("missing header field Auth-Protocol", authStatus.getValue());
		}
	}

	private static HttpGet baseRequest() {
		final HttpGet request = new HttpGet();
		request.setURI(URI.create(endpoint));
		return request;
	}

}