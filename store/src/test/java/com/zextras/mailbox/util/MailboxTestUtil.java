package com.zextras.mailbox.util;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.mailbox.messagebroker.MessageBrokerFactory;
import com.zextras.mailbox.util.InMemoryLdapServer.Builder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.mockito.Mockito;


/**
 *
 * @deprecated  use {@link  com.zextras.mailbox.MailboxTestSuite}
 */
@Deprecated(since = "25.6.0")
public class MailboxTestUtil {

  public static InMemoryLdapServer getInMemoryLdapServer() {
    return inMemoryLdapServer;
  }

  private static InMemoryLdapServer inMemoryLdapServer;

  private static MessageBrokerClient mockedMessageBrokerClient;

  private MailboxTestUtil() {}


  /**
   * Sets up all possible environment variables to make the mailbox work:
   * - loads native library
   * - Uses localconfig-api-test.xml as source for {@link LC}
   * - creates a server given server name
   * - creates a domain with given domain name
   * - Starts LDAP and the database and all possible dependencies. If you find some are missing add them.
   *
   * @throws Exception
   */
  public static void setUp(MailboxTestData testData) throws Exception {
    System.setProperty("zimbra.native.required", "false");
    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(
                com.zimbra.cs.mailbox.MailboxTestUtil.class.getResource(
                    "/localconfig-api-test.xml"))
            .getFile());

    final var mailboxHome = Files.createTempDirectory("mailbox_home_");
    LC.zimbra_home.setDefault(mailboxHome.toAbsolutePath().toString());

    var ldapPort = PortUtil.findFreePort();

    LC.ldap_port.setDefault(ldapPort);

    inMemoryLdapServer = new Builder()
        .withLdapPort(ldapPort)
        .build();
    inMemoryLdapServer.start();
    inMemoryLdapServer.initializeBasicData();

    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
    initData(testData);

    DbPool.startup();
    HSQLDB.createDatabase();

    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();
    RightManager.getInstance();
    ScheduledTaskManager.startup();
  }

  /**
   * Stops the {@link #inMemoryLdapServer} and {@link RedoLogProvider} The goal is to cleanup
   * the system before starting another one. If some clean task is missing consider adding it.
   */
  public static void tearDown() throws ServiceException {
    inMemoryLdapServer.clear();
    RedoLogProvider.getInstance().shutdown();
    inMemoryLdapServer.shutDown(true);
  }

  public static void clearData() throws Exception {
    inMemoryLdapServer.clear();
    HSQLDB.clearDatabase();
  }

  public static void initData(MailboxTestData testData) throws Exception {
    inMemoryLdapServer.initializeBasicData();
    var provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    var lmtpPort = PortUtil.findFreePort();

    final var server =
        provisioning.createServer(
            testData.serverName(),
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    server.setLmtpBindPort(lmtpPort);

    server.setPop3SSLServerEnabled(false);
    server.setPop3ServerEnabled(false);

    server.setImapSSLServerEnabled(false);
    server.setImapServerEnabled(false);
    var domain = provisioning.createDomain(testData.defaultDomain(), new HashMap<>());
    domain.setId(testData.defaultDomainId());
    mockMessageBrokerClient();
  }

  private static void mockMessageBrokerClient() {
    if(mockedMessageBrokerClient == null) {
      var messageBrokerClient = Mockito.mock(MessageBrokerClient.class);
      var mockedMessageBrokerFactory = Mockito.mockStatic(MessageBrokerFactory.class, Mockito.CALLS_REAL_METHODS);
      mockedMessageBrokerFactory.when(MessageBrokerFactory::getMessageBrokerClientInstance).thenReturn(messageBrokerClient);
      mockedMessageBrokerClient = messageBrokerClient;
    }
  }

}
