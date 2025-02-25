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
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
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

		private int userHttpPort = 8080;
		private int userHttpsPort = 8443;
		private int adminPort = 7071;
		private final String mailboxHome;
		private final String timeZoneFile;

		public ServerSetup(String mailboxHome, String timeZoneFile) {
			this.mailboxHome = mailboxHome;
			this.timeZoneFile = timeZoneFile;
		}

		public ServerSetup withUserPort(int userPort) {
			this.userHttpPort = userPort;
			return this;
		}
		public ServerSetup withUserHttpsPort(int userHttpsPort) {
			this.userHttpsPort = userHttpsPort;
			return this;
		}
		public ServerSetup withAdminPort(int adminPort) {
			this.adminPort = adminPort;
			return this;
		}

		public Server create() throws Exception {
			System.setProperty("zimbra.native.required", "false");

			setupTestKeyStore();

			LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
			LC.ldap_port.setDefault(LDAP_PORT);
			LC.zimbra_home.setDefault(mailboxHome);
			LC.zimbra_log_directory.setDefault(mailboxHome);

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
											ZAttrProvisioning.A_zimbraMailPort, String.valueOf(userHttpPort),
											ZAttrProvisioning.A_zimbraMailSSLPort, String.valueOf(userHttpsPort),
											ZAttrProvisioning.A_zimbraAdminPort, String.valueOf(adminPort),
											ZAttrProvisioning.A_zimbraMailMode, "both",
											ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
											ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE")));

			return new MailboxServer.Builder(provisioning.getConfig(), provisioning.getLocalServer())
					.build();
		}

		private static void setupTestKeyStore() throws Exception {
			final Path keystoreRoot = Files.createTempDirectory("keystore");
			final Path keystorePath = Path.of(keystoreRoot.toAbsolutePath().toString(), "keystore");

			Security.addProvider(new BouncyCastleProvider());
			// Generate RSA Key Pair
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(2048);  // key size 2048 bits
			KeyPair keyPair = keyPairGen.generateKeyPair();

			// Create a self-signed certificate using BouncyCastle
			X500Principal issuer = new X500Principal("CN=Test");
			Date startDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.add(Calendar.YEAR, 1);
			Date endDate = calendar.getTime();

			BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

			X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
					new X500Name(issuer.getName()), serial, startDate, endDate, new X500Name(issuer.getName()), keyPair.getPublic());

			final X509Certificate certificate1 = new JcaX509CertificateConverter().setProvider("BC")
					.getCertificate(certBuilder.build(new JcaContentSignerBuilder("SHA256withRSA")
							.build(keyPair.getPrivate())));

			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			final String keyStoreAbsolutePath = keystorePath.toAbsolutePath().toString();
			final String keyStorePassword = "test123";

			keyStore.setKeyEntry("bmc", keyPair.getPrivate(), keyStorePassword.toCharArray(),
					new java.security.cert.Certificate[]{certificate1});

			try (FileOutputStream fos = new FileOutputStream(keyStoreAbsolutePath)) {
				keyStore.store(fos, keyStorePassword.toCharArray());
			}

			LC.mailboxd_keystore.setDefault(keyStoreAbsolutePath);
			LC.mailboxd_keystore_password.setDefault(keyStorePassword);
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
	}

	public static void main(String[] args) throws Exception {
		final Server server = new ServerSetup("./store",
				"store/src/test/resources/timezones-test.ics").create();
		server.start();
		server.join();
	}

}
