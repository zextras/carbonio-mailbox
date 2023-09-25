package com.zextras.mailbox.usecase;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MailboxTestUtil {
  public static final int LDAP_PORT = 1389;
  public static final String SERVER_NAME = "localhost";
  public static final String DEFAULT_DOMAIN = "test.com";
  private static InMemoryDirectoryServer inMemoryDirectoryServer;

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

    Provisioning provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    provisioning.createServer(
        SERVER_NAME,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    provisioning.createDomain(DEFAULT_DOMAIN, new HashMap<>());
    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();
  }

  public static void tearDown() {
    inMemoryDirectoryServer.clear();
    inMemoryDirectoryServer.shutDown(true);
  }
}
