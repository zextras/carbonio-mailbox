/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.mailbox.util.InMemoryLdapServer.Builder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.auth.ZimbraCustomAuth;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.index.IndexStore;
import com.zimbra.cs.index.ZimbraAnalyzer;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.unboundid.UBIDLdapClient;
import com.zimbra.cs.ldap.unboundid.UBIDLdapPoolConfig;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.redolog.DefaultRedoLogProvider;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

public class MailboxSetupHelper {

	private final Path mailboxHome;
	private final Path mailboxTmpDirectory;
  private final String timezoneFilePath;
  private final String datasourceFilePath;
	private final int ldapPort;

	private final InMemoryLdapServer inMemoryLdapServer;

	private MailboxSetupHelper(Path mailboxHome, Path tmpDirectory, String timezoneFilePath, String datasourceFilePath, int ldapPort, InMemoryLdapServer inMemoryLdapServer) {
		this.mailboxHome = mailboxHome;
		this.mailboxTmpDirectory = tmpDirectory;
    this.timezoneFilePath = timezoneFilePath;
    this.datasourceFilePath = datasourceFilePath;
    this.ldapPort = ldapPort;
		this.inMemoryLdapServer = inMemoryLdapServer;
	}

  public static MailboxSetupHelper create() {
    try {
      var ldapPort = PortUtil.findFreePort();
      var inMemoryLdapServer = new Builder().withLdapPort(ldapPort).build();
      final Path mailboxHome1 = Files.createTempDirectory("mailbox_home");
      final Path mailboxTmp = Files.createTempDirectory("mailbox_tmp");
      final String timezoneFilePath = "src/test/resources/timezones-test.ics";
      final String datasourceFilePath = "src/test/resources/datasource-test.xml";
      return new MailboxSetupHelper(
          mailboxHome1,
          mailboxTmp,
          timezoneFilePath,
          datasourceFilePath,
          ldapPort,
          inMemoryLdapServer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

	private String getVolumeDirectory() {
		return LC.zimbra_home.value() + "/build/test";
	}

	public void setUp(MailboxTestData mailboxTestData) throws Exception {
		System.setProperty("zimbra.native.required", "false");
		System.setProperty(
				"zimbra.config",
				Objects.requireNonNull(
						this.getClass().getResource(
										"/localconfig-api-test.xml"))
						.getFile());

		LC.ldap_port.setDefault(ldapPort);
		inMemoryLdapServer.start();

		setUpWithoutLdapStart(mailboxTestData);
	}

	/**
	 * Tears down everything except the LDAP server, then reinitializes.
	 * The LDAP server stays running across test classes to avoid the start/stop cost.
	 */
	public void resetAndSetUp(MailboxTestData mailboxTestData) throws Exception {
		tearDownWithoutLdapStop();
		setUpWithoutLdapStart(mailboxTestData);
	}

	private void setUpWithoutLdapStart(MailboxTestData mailboxTestData) throws Exception {
		if (!Files.exists(mailboxHome)) {
			Files.createDirectory(mailboxHome);
		}
		if (!Files.exists(mailboxTmpDirectory)) {
			Files.createDirectory(mailboxTmpDirectory);
		}
		LC.zimbra_home.setDefault(mailboxHome.toAbsolutePath().toString());
		LC.zimbra_tmp_directory.setDefault(mailboxTmpDirectory.toAbsolutePath().toString());

		LC.timezone_file.setDefault(timezoneFilePath);
		WellKnownTimeZones.loadFromFile(new File(timezoneFilePath));

		LC.data_source_config.setDefault(datasourceFilePath);

		LC.zimbra_class_database.setDefault(HSQLDB.class.getName());

		// Initialize base LDAP entries (bind user, config, COS) before creating Provisioning,
		// which needs the bind user to connect.
		inMemoryLdapServer.initializeBasicData();

		final UBIDLdapPoolConfig poolConfig = UBIDLdapPoolConfig.createNewPool(true);
		final UBIDLdapClient client = UBIDLdapClient.createNew(poolConfig);
		LdapClient.setInstance(client);
		Provisioning.setInstance(new LdapProvisioningWithMockMime(client));
		this.createBaseData(mailboxTestData);
		HSQLDB.createDatabase(getVolumeDirectory());
		DbPool.startup();
		MailboxManager.setInstance(new MailboxManager());
		RedoLogProvider.setInstance(new DefaultRedoLogProvider());
		RedoLogProvider.getInstance().startup();
		StoreManager.getInstance().startup();
		RightManager.getInstance();
		ScheduledTaskManager.startup();
		System.setProperty("broker.disabled", "true");
		ZimbraAnalyzer.setInstance(new ZimbraAnalyzer());
		ZimbraCustomAuth.clear();
	}

  /**
   * Reinitializes LDAP base entries and creates server/domain/config.
   * Safe to call after {@link #clearData()} — restores the full base state.
   */
  public void initData(MailboxTestData mailboxTestData) throws Exception {
    inMemoryLdapServer.initializeBasicData();
    createBaseData(mailboxTestData);
  }

  private void createBaseData(MailboxTestData mailboxTestData) throws Exception {
		var provisioning = Provisioning.getInstance();
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
		var config = provisioning.getConfig();
		config.setDefaultDomainName(mailboxTestData.defaultDomain());
    domain.setId(mailboxTestData.defaultDomainId());
  }

	public void clearData() throws Exception {
		inMemoryLdapServer.clear();
		HSQLDB.clearDatabase();
	}

	public void tearDown() throws Exception {
		tearDownWithoutLdapStop();
		inMemoryLdapServer.shutDown(true);
	}

	private void tearDownWithoutLdapStop() throws Exception {
		inMemoryLdapServer.clear();
		IndexStore.getFactory().destroy();
		if (RedoLogProvider.getInstance() != null) {
			RedoLogProvider.getInstance().shutdown();
			RedoLogProvider.setInstance(null);
		}
		DbPool.shutDownAndClear();
		UBIDLdapClient.shutdown();
		FileUtils.deleteDirectory(mailboxHome.toFile());
		FileUtils.deleteDirectory(mailboxTmpDirectory.toFile());
	}
}
