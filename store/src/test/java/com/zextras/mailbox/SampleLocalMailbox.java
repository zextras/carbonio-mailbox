// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.mailbox.util.InMemoryLdapServer;
import com.zextras.mailbox.util.InMemoryLdapServer.Builder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Server;

/**
 * A Mailbox that can be used for testing. You can spin it up and interact with APIs, except it uses
 * an in memory db {@link HSQLDB} and an in memory LDAP {@link InMemoryLdapServer}.
 * <p>
 * Please run this class with java.library.path pointing to native module target (native/target) so
 * native library is loaded
 */
public class SampleLocalMailbox {

	private static final int LDAP_PORT = 1389;
	private static final String APP_SERVER_NAME = "localhost";

	public static class ServerSetup {

		private final int userPort;
		private final String mailboxHome;
		private final String timeZoneFile;

		public ServerSetup(int userPort, String mailboxHome, String timeZoneFile) {
			this.userPort = userPort;
			this.mailboxHome = mailboxHome;
			this.timeZoneFile = timeZoneFile;
		}

		public Server create() throws Exception {
			System.setProperty("zimbra.native.required", "false");

			final Path keystoreRoot = Files.createTempDirectory("keystore");
			final Path keystorePath = Path.of(keystoreRoot.toAbsolutePath().toString(), "keystore");
			KeyStore ks = KeyStore.getInstance("PKCS12");
			final String keystore_password = "test";
			ks.load(null, keystore_password.toCharArray());
			ks.store(Files.newOutputStream(keystorePath), keystore_password.toCharArray());

			LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
			LC.ldap_port.setDefault(LDAP_PORT);
			LC.zimbra_home.setDefault(mailboxHome);
			LC.mailboxd_keystore_password.setDefault(keystore_password);
			LC.mailboxd_truststore_password.setDefault(keystore_password);
			LC.mailboxd_keystore.setDefault(keystorePath.toAbsolutePath().toString());
			LC.mailboxd_keystore.setDefault(keystorePath.toAbsolutePath().toString());
			LC.zimbra_server_hostname.setDefault(APP_SERVER_NAME);
			LC.timezone_file.setDefault(timeZoneFile);

			final InMemoryLdapServer inMemoryLdapServer = new Builder().
					withLdapPort(LDAP_PORT)
					.build();
			inMemoryLdapServer.start();
			inMemoryLdapServer.initializeBasicData();

			setUpMockData();

			DbPool.startup();
			HSQLDB.createDatabase();
			final Provisioning provisioning = Provisioning.getInstance();
			provisioning
					.getServerByName(APP_SERVER_NAME)
					.modify(
							new HashMap<>(
									Map.of(
											ZAttrProvisioning.A_zimbraMailPort, String.valueOf(userPort),
											ZAttrProvisioning.A_zimbraMailMode, "both",
											ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
											ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE")));

			return new MailboxServer.Builder(provisioning.getConfig(), provisioning.getLocalServer())
					.build();
		}

		private static void setUpMockData() throws ServiceException {
			final Provisioning provisioning = Provisioning.getInstance();
			provisioning.createServer(
					APP_SERVER_NAME,
					new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
			provisioning.createDomain("test.com", new HashMap<>());
			provisioning.createAccount("test@test.com", "password", new HashMap<>());
			provisioning.createAccount("admin@test.com", "password", new HashMap<>() {{
				put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
			}});
		}


		public static void main(String[] args) throws Exception {
			final Server server = new ServerSetup(8080, "./store", "store/src/test/resources/timezones-test.ics").create();
			server.start();
			server.join();
		}


	}

}
