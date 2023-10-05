package com.zextras.mailbox.util;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This utility class allows easy setup for the Mailbox environment using {@link #setUp()} method.
 * To clean up th environment remember to call {@link #tearDown()}. It uses an {@link
 * InMemoryDirectoryServer} and an in memory database using {@link HSQLDB} as dependencies.
 */
public class MailboxTestUtil {
  public static final int LDAP_PORT = 1389;
  public static final String SERVER_NAME = "localhost";
  public static final String DEFAULT_DOMAIN = "test.com";
  private static InMemoryDirectoryServer inMemoryDirectoryServer;

  private MailboxTestUtil() {}

  /**
   * Sets up all possible environment variables to make the mailbox operate. - loads native library
   * - Uses localconfig-api-test.xml as source for {@link LC} - creates a server with name {@link
   * #SERVER_NAME} - creates a domain with name {@link #DEFAULT_DOMAIN} Starts LDAP and the database
   * and all possible dependencies. If you find some are missing add them.
   *
   * @throws Exception
   */
  public static void setUp() throws Exception {
    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(
                com.zimbra.cs.mailbox.MailboxTestUtil.class.getResource(
                    "/localconfig-api-test.xml"))
            .getFile());

    inMemoryDirectoryServer = InMemoryLdapServerTestUtil.createInMemoryDirectoryServer(LDAP_PORT);
    inMemoryDirectoryServer.startListening();

    LC.ldap_port.setDefault(LDAP_PORT);
    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());

    DbPool.startup();
    HSQLDB.createDatabase("");

    final Provisioning provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    provisioning.createServer(
        SERVER_NAME,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    provisioning.createDomain(DEFAULT_DOMAIN, new HashMap<>());

    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();
    RightManager.getInstance();
    ScheduledTaskManager.startup();
  }

  /**
   * Stops the {@link #inMemoryDirectoryServer} and {@link RedoLogProvider} The goal is to cleanup
   * the system before starting another one. If some clean task is missing consider adding it.
   */
  public static void tearDown() throws ServiceException {
    inMemoryDirectoryServer.clear();
    RedoLogProvider.getInstance().shutdown();
    inMemoryDirectoryServer.shutDown(true);
  }

  /**
   * Creates a basic account with domain {@link #DEFAULT_DOMAIN} and {@link #SERVER_NAME}. You can
   * pass extra attributes if needed, for example to make the user an admin. If you pass a {@link
   * ZAttrProvisioning#A_zimbraMailHost} you will override the default server of the user.
   *
   * @param extraAttrs attributes to add on top of default one
   * @return created account
   * @throws ServiceException
   */
  public static Account createAccountDefaultDomain(Map<String, Object> extraAttrs)
      throws ServiceException {
    final HashMap<String, Object> attrs =
        new HashMap<>(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME));
    attrs.putAll(extraAttrs);
    return Provisioning.getInstance()
        .createAccount(UUID.randomUUID() + "@" + MailboxTestUtil.DEFAULT_DOMAIN, "password", attrs);
  }
}
