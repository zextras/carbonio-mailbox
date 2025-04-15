/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.mailbox.messagebroker.MessageBrokerFactory;
import com.zextras.mailbox.util.InMemoryLdapServer.Builder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;

public class MailboxSetupHelper {

	private final Path mailboxHome;
	private final Path mailboxTmpDirectory;
	private final int ldapPort;

	private final InMemoryLdapServer inMemoryLdapServer;

	private MailboxSetupHelper(Path mailboxHome, Path tmpDirectory, int ldapPort, InMemoryLdapServer inMemoryLdapServer) {
		this.mailboxHome = mailboxHome;
		this.mailboxTmpDirectory = tmpDirectory;
		this.ldapPort = ldapPort;
		this.inMemoryLdapServer = inMemoryLdapServer;
	}

	public static MailboxSetupHelper create() {
		try {
			var ldapPort = PortUtil.findFreePort();
			var inMemoryLdapServer = new Builder()
					.withLdapPort(ldapPort)
					.build();
			final Path mailboxHome1 = Files.createTempDirectory("mailbox_home");
			final Path mailboxTmp = Files.createTempDirectory("mailbox_tmp");
			return new MailboxSetupHelper(mailboxHome1, mailboxTmp, ldapPort, inMemoryLdapServer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void mockMessageBrokerClient() {
			var messageBrokerClient = Mockito.mock(MessageBrokerClient.class);
			var mockedMessageBrokerFactory = Mockito.mockStatic(MessageBrokerFactory.class,
					Mockito.CALLS_REAL_METHODS);
			mockedMessageBrokerFactory.when(MessageBrokerFactory::getMessageBrokerClientInstance)
					.thenReturn(messageBrokerClient);
	}

	public void setUp(MailboxTestData mailboxTestData) throws Exception {
		System.setProperty("zimbra.native.required", "false");
		System.setProperty(
				"zimbra.config",
				Objects.requireNonNull(
						this.getClass().getResource(
										"/localconfig-api-test.xml"))
						.getFile());

		LC.zimbra_home.setDefault(mailboxHome.toAbsolutePath().toString());
		LC.zimbra_tmp_directory.setDefault(mailboxTmpDirectory.toAbsolutePath().toString());

		LC.ldap_port.setDefault(ldapPort);

		inMemoryLdapServer.start();
		inMemoryLdapServer.initializeBasicData();

		LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
		this.initData(mailboxTestData);

		DbPool.startup();
		HSQLDB.createDatabase();

		RedoLogProvider.getInstance().startup();
		StoreManager.getInstance().startup();
		RightManager.getInstance();
		ScheduledTaskManager.startup();
		mockMessageBrokerClient();
	}

	public void initData(MailboxTestData mailboxTestData) throws Exception {
		inMemoryLdapServer.initializeBasicData();
		var provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
		var lmtpPort = PortUtil.findFreePort();

		final var server =
				provisioning.createServer(
						mailboxTestData.serverName(),
						new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
		server.setLmtpBindPort(lmtpPort);

		server.setPop3SSLServerEnabled(false);
		server.setPop3ServerEnabled(false);

		server.setImapSSLServerEnabled(false);
		server.setImapServerEnabled(false);
		var domain = provisioning.createDomain(mailboxTestData.defaultDomain(), new HashMap<>());
		domain.setId(mailboxTestData.defaultDomainId());
	}

	public void clearData() throws Exception {
		inMemoryLdapServer.clear();
		HSQLDB.clearDatabase();
	}

	public void tearDown() throws Exception {
		inMemoryLdapServer.clear();
		RedoLogProvider.getInstance().shutdown();
		inMemoryLdapServer.shutDown(true);
		FileUtils.deleteDirectory(mailboxHome.toFile());
		FileUtils.deleteDirectory(mailboxTmpDirectory.toFile());
	}
}
