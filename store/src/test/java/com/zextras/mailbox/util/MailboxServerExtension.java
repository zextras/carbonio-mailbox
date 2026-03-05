/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import com.zextras.mailbox.MailboxEnvironmentSetupHelper;
import com.zextras.mailbox.server.MailboxServer;
import com.zextras.mailbox.util.CreateAccount.Factory;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that boots a full {@link MailboxServer} (LDAP + DB + Jetty + internal API)
 * for integration tests. Replaces the duplicated {@code @BeforeAll/@AfterAll} boilerplate
 * across test classes.
 *
 * <p>Usage:
 * <pre>{@code
 * @RegisterExtension
 * static final MailboxServerExtension server = new MailboxServerExtension();
 * }</pre>
 */
public class MailboxServerExtension implements BeforeAllCallback, AfterAllCallback {

	private final int userHttpPort;
	private final int userHttpsPort;
	private final int adminPort;
	private final int internalApiPort;
	private static final String DOMAIN = "test.com";

	private MailboxServer mailboxServer;
	private TestHttpClient httpClient;
	private File tmpDir;

	public MailboxServerExtension() {
		this.userHttpPort = PortUtil.findFreePort();
		this.userHttpsPort = PortUtil.findFreePort();
		this.adminPort = PortUtil.findFreePort();
		this.internalApiPort = PortUtil.findFreePort();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		tmpDir = Files.createTempDirectory("mailbox-server-ext").toFile();
		tmpDir.deleteOnExit();

		final Class<?> testClass = context.getRequiredTestClass();
		final String mailboxHome = testClass.getResource("/").getFile();
		final String timezoneFile = testClass.getResource("/timezones-test.ics").getFile();

		var localConfigFile = new File(testClass.getResource("/localconfig-test.xml").getPath());
		var tmpLocalConfigFile = new File(tmpDir, "localconfig-test.xml");
		Files.copy(localConfigFile.toPath(), tmpLocalConfigFile.toPath());

		System.setProperty("zimbra.config", tmpLocalConfigFile.getAbsolutePath());
		System.setProperty("org.eclipse.jetty.util.log.announce", "false");

		LC.zimbra_admin_service_scheme.setDefault("https://");
		LC.zimbra_zmprov_default_soap_server.setDefault("localhost");
		LC.zimbra_admin_service_port.setDefault(adminPort);
		LC.mailbox_internal_api_port.setDefault(internalApiPort);
		LC.mailbox_internal_api_bind_address.setDefault("localhost");
		LC.support_timer.setDefault(false);

		mailboxServer = new MailboxEnvironmentSetupHelper(mailboxHome, timezoneFile)
				.withAdminPort(adminPort)
				.withDomain(DOMAIN)
				.withUserPort(userHttpPort)
				.withUserHttpsPort(userHttpsPort)
				.create();
		mailboxServer.start();
		httpClient = new TestHttpClient();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		if (httpClient != null) {
			httpClient.close();
		}
		if (mailboxServer != null) {
			mailboxServer.stop();
		}
		LC.reload();
	}

	public int getUserHttpPort() {
		return userHttpPort;
	}

	public int getUserHttpsPort() {
		return userHttpsPort;
	}

	public int getAdminPort() {
		return adminPort;
	}

	public int getInternalApiPort() {
		return internalApiPort;
	}

	public String getInternalApiEndpoint() {
		return "http://localhost:" + internalApiPort + "/internal";
	}

	public TestHttpClient getHttpClient() {
		return httpClient;
	}

	public CreateAccount getAccountFactory() {
		return new Factory(Provisioning.getInstance(), DOMAIN).get();
	}
}
