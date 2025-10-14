/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.server;

import com.zextras.mailbox.MailboxEnvironmentSetupHelper;
import com.zextras.mailbox.util.PortUtil;
import com.zextras.mailbox.util.SoapClient;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.localconfig.LC;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.type.AccountSelector;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("api")
@Disabled
class MailboxServerAPITest {

	private static MailboxServer mailboxServer;
	private static final int USER_HTTP_PORT = PortUtil.findFreePort();
	private static final int USER_HTTPS_PORT = PortUtil.findFreePort();
	private static final int ADMIN_PORT = PortUtil.findFreePort();
	@TempDir
	private static File tmpDir;

	@BeforeAll
	static void setUp() throws Exception {
		final String mailboxHome = MailboxServerAPITest.class.getResource("/").getFile();
		final String timezoneFile = MailboxServerAPITest.class.getResource("/timezones-test.ics")
				.getFile();

		var localConfigFilePath = MailboxServerAPITest.class.getResource("/localconfig-test.xml")
				.getPath();

		var localConfigFile = new File(localConfigFilePath);
		var tmpLocalConfigFile = new File(tmpDir, "localconfig-test.xml");
		Files.copy(localConfigFile.toPath(), tmpLocalConfigFile.toPath());

		System.setProperty("zimbra.config", tmpLocalConfigFile.getAbsolutePath());
		System.setProperty("org.eclipse.jetty.util.log.announce", "false");
		// For some reason these localconfig values are used by SoapProvisioning/ZClient
		LC.zimbra_admin_service_scheme.setDefault("https://");
		LC.zimbra_zmprov_default_soap_server.setDefault("localhost");
		LC.zimbra_admin_service_port.setDefault(ADMIN_PORT);
		mailboxServer = new MailboxEnvironmentSetupHelper(mailboxHome, timezoneFile)
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
		SoapClient soapClient = new SoapClient(getUserEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName("test@test.com"), "password"))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldAuthenticateStandardUser_OnHttpsPort() throws Exception {
		SoapClient soapClient = new SoapClient(getUserHttpsEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName("test@test.com"), "password"))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

	@Test
	void shouldAuthenticateAdminUser() throws Exception {
		SoapClient soapClient = new SoapClient(getAdminEndpoint());
		final SoapResponse soapResponse = soapClient.newRequest()
				.setSoapBody(new AuthRequest(AccountSelector.fromName("admin@test.com"), "password"))
				.call();

		Assertions.assertEquals(200, soapResponse.statusCode());
	}

}