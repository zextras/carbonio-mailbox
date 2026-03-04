/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxEnvironmentSetupHelper;
import com.zextras.mailbox.server.MailboxServer;
import com.zextras.mailbox.util.PortUtil;
import com.zextras.mailbox.util.TestHttpClient;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.common.localconfig.LC;
import java.io.File;
import java.nio.file.Files;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("e2e")
class InternalApiServerIT {

	private static MailboxServer mailboxServer;
	private static final int USER_HTTP_PORT = PortUtil.findFreePort();
	private static final int USER_HTTPS_PORT = PortUtil.findFreePort();
	private static final int ADMIN_PORT = PortUtil.findFreePort();
	private static final int INTERNAL_API_PORT = PortUtil.findFreePort();
	private static TestHttpClient httpClient;

	@TempDir
	private static File tmpDir;

	@BeforeAll
	static void setUp() throws Exception {
		final String mailboxHome = InternalApiServerIT.class.getResource("/").getFile();
		final String timezoneFile = InternalApiServerIT.class.getResource("/timezones-test.ics")
				.getFile();

		var localConfigFilePath = InternalApiServerIT.class.getResource("/localconfig-test.xml")
				.getPath();

		var localConfigFile = new File(localConfigFilePath);
		var tmpLocalConfigFile = new File(tmpDir, "localconfig-test.xml");
		Files.copy(localConfigFile.toPath(), tmpLocalConfigFile.toPath());

		System.setProperty("zimbra.config", tmpLocalConfigFile.getAbsolutePath());
		System.setProperty("org.eclipse.jetty.util.log.announce", "false");

		LC.zimbra_admin_service_scheme.setDefault("https://");
		LC.zimbra_zmprov_default_soap_server.setDefault("localhost");
		LC.zimbra_admin_service_port.setDefault(ADMIN_PORT);
		LC.mailbox_internal_api_port.setDefault(INTERNAL_API_PORT);
		LC.mailbox_internal_api_bind_address.setDefault("localhost");

		mailboxServer = new MailboxEnvironmentSetupHelper(mailboxHome, timezoneFile)
				.withAdminPort(ADMIN_PORT)
				.withUserPort(USER_HTTP_PORT)
				.withUserHttpsPort(USER_HTTPS_PORT)
				.create();
		mailboxServer.start();
		httpClient = new TestHttpClient();
	}

	@AfterAll
	static void tearDown() throws Exception {
		if (httpClient != null) {
			httpClient.close();
		}
		mailboxServer.stop();
		LC.reload();
	}

	@Test
	void pingShouldReturn200() throws Exception {
		final Response response = httpClient.execute(
				new HttpGet("http://localhost:" + INTERNAL_API_PORT + "/api/v1/ping"));

		assertEquals(200, response.statusCode());
		assertEquals("{\"status\":\"pong\"}", response.body());
	}

	@Test
	void openApiJsonShouldBeServed() throws Exception {
		final Response response = httpClient.execute(
				new HttpGet("http://localhost:" + INTERNAL_API_PORT + "/api/v1/openapi.json"));

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"openapi\""), "Response should contain OpenAPI spec");
	}
}
