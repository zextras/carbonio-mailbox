// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.soap;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.JettyServerFactory.ServerWithConfiguration;
import com.zextras.mailbox.util.MailboxTestData;
import com.zextras.mailbox.util.MailboxSetupHelper;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.SoapServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** A Junit 5 extension to start a SOAP server and corresponding SOAP client. */
public class SoapExtension implements BeforeAllCallback, AfterAllCallback {
  private static final MailboxTestData testData = new MailboxTestData( "localhost", "test.com", "f4806430-b434-4e93-9357-a02d9dd796b8");
  private static final MailboxSetupHelper mailboxSetupHelper =  MailboxSetupHelper.create();

  public String getServerName() {
    return testData.serverName();
  }
  public String getDefaultDomain() {
    return testData.defaultDomain();
  }

  public SoapClient getSoapClient() {
    return soapClient;
  }

  public void clearData() throws Exception {
    mailboxSetupHelper.clearData();
  }

  public int getPort() {
    return this.port;
  }

  public void initData() throws Exception {
    mailboxSetupHelper.initData(testData);
  }

  private final SoapClient soapClient;
  private final Server server;
  private final int port;

  private SoapExtension(int port, Server server, SoapClient soapClient) {
    this.server = server;
    this.port = port;
    this.soapClient = soapClient;
  }

  public static class Builder {

    public Builder withBasePath(String basePath) {
      this.basePath = basePath;
      return this;
    }

    public Builder addEngineHandler(String engineHandler) {
      this.engineHandlers.add(engineHandler);
      return this;
    }

    private String basePath = "/";
    private final List<String> engineHandlers = new ArrayList<>();

    public SoapExtension create() {
      final var firstServlet = createFirstServlet();
      final var soapServlet = createSecondServlet();
      final ServerWithConfiguration serverWithConfiguration = new JettyServerFactory()
          .addServlet("/firstServlet", firstServlet)
          .addServlet(basePath + "*", soapServlet)
          .create();
      final var server =
          serverWithConfiguration.server();
      final int serverPort = serverWithConfiguration.serverPort();
      final var soapClient = new SoapClient("http://localhost:" + serverPort + basePath);
      return new SoapExtension(serverPort, server, soapClient);
    }

    private static ServletHolder createFirstServlet() {
      final var firstServlet = new ServletHolder(FirstServlet.class);
      firstServlet.setInitOrder(1);
      return firstServlet;
    }

    private ServletHolder createSecondServlet() {
      final var soapServlet = new ServletHolder(SoapServlet.class);
      int i = 0;
      for (var engineHandler : engineHandlers) {
        soapServlet.setInitParameter("engine.handler." + i, engineHandler);
        i++;
      }
      soapServlet.setInitOrder(2);
      return soapServlet;
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    LC.zimbra_class_provisioning.setDefault(LdapProvisioning.class.getName());

    if (!server.isRunning()) {
      mailboxSetupHelper.setUp(testData);
      Provisioning.getInstance()
          .getServerByName(testData.serverName())
          .modify(
              new HashMap<>(
                  Map.of(
                      Provisioning.A_zimbraMailPort, String.valueOf(port),
                      ZAttrProvisioning.A_zimbraMailMode, "http",
                      ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
                      ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE")));

      server.start();
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    soapClient.close();
    server.stop();
    mailboxSetupHelper.tearDown();
  }
}
