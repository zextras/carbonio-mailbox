/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.PortUtil;
import com.zextras.mailbox.util.TestHttpClient;
import com.zextras.mailbox.util.TestHttpClient.Response;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InternalApiContextHandlerTest {

	private Server server;
	private int port;
	private TestHttpClient httpClient;

	@BeforeEach
	void setUp() throws Exception {
		port = PortUtil.findFreePort();
		server = new Server();

		final ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		connector.setHost("localhost");
		connector.setName(InternalApiContextHandler.CONNECTOR_NAME);
		server.addConnector(connector);

		final ServletContextHandler handler = InternalApiContextHandler.create();
		server.setHandler(handler);

		server.start();
		httpClient = new TestHttpClient();
	}

	@AfterEach
	void tearDown() throws Exception {
		if (httpClient != null) {
			httpClient.close();
		}
		if (server != null) {
			server.stop();
		}
	}

	@Test
	void pingShouldReturn200WithPong() throws Exception {
		final Response response = httpClient.execute(
				new HttpGet("http://localhost:" + port + "/api/v1/ping"));

		assertEquals(200, response.statusCode());
		assertEquals("{\"status\":\"pong\"}", response.body());
	}

	@Test
	void openApiJsonShouldBeServed() throws Exception {
		final Response response = httpClient.execute(
				new HttpGet("http://localhost:" + port + "/api/v1/openapi.json"));

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"openapi\""), "Response should contain OpenAPI spec");
		assertTrue(response.body().contains("/ping"), "OpenAPI spec should document /ping endpoint");
	}
}
