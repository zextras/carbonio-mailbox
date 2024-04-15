// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.mailbox.ldap.InMemoryLdapServer;
import com.zextras.mailbox.ldap.InMemoryLdapServer.Builder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class SampleLocalMailbox {

  private static final int LDAP_PORT = 1389;
  private static final String APP_SERVER_NAME = "localhost";
  private static final int APP_SERVER_PORT = 7070;


  public static void main(String[] args) throws Exception {
    System.setProperty("zimbra.native.required", "false");
    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
    LC.ldap_port.setDefault(LDAP_PORT);
    LC.zimbra_home.setDefault("./store");
    LC.mailboxd_keystore_password.setDefault("zextras");
    LC.zimbra_server_hostname.setDefault(APP_SERVER_NAME);
    LC.timezone_file.setDefault("store/src/test/resources/timezones-test.ics");

    final InMemoryLdapServer inMemoryLdapServer = new Builder().
        withLdapPort(LDAP_PORT)
        .build();
    inMemoryLdapServer.start();
    inMemoryLdapServer.initializeBasicData();

    setUpMockData();

    DbPool.startup();
    HSQLDB.createDatabase("store/");
    Provisioning.getInstance()
        .getServerByName(APP_SERVER_NAME)
        .modify(
            new HashMap<>(
                Map.of(
                    Provisioning.A_zimbraMailPort, String.valueOf(APP_SERVER_PORT),
                    ZAttrProvisioning.A_zimbraMailMode, "http",
                    ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
                    ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE")));

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setDescriptor("store/conf/web-dev.xml");
    webAppContext.setResourceBase("/");
    webAppContext.setContextPath("/service");

    Server server = new Server(APP_SERVER_PORT);
    server.setHandler(webAppContext);
    server.start();
    server.join();
  }

  private static void setUpMockData() throws ServiceException {
    final Provisioning provisioning = Provisioning.getInstance();
    provisioning.createServer(
        APP_SERVER_NAME,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    provisioning.createDomain("test.com", new HashMap<>());
  }

}
