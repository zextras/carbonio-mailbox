// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.soap;

import static com.zextras.mailbox.util.MailboxTestUtil.SERVER_NAME;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Provisioning;
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

/**
 * A Junit 5 extension to start a SOAP server and corresponding SOAP client.
 */
public class SoapExtension implements BeforeAllCallback, AfterAllCallback {


  public SoapClient getSoapClient() {
    return soapClient;
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

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public Builder withBasePath(String basePath) {
      this.basePath = basePath;
      return this;
    }

    public Builder addEngineHandler(String engineHandler) {
      this.engineHandlers.add(engineHandler);
      return this;
    }

    private int port = 8080;
    private String basePath = "/";
    private final List<String> engineHandlers = new ArrayList<>();

    public SoapExtension create() {
      final ServletHolder firstServlet = new ServletHolder(FirstServlet.class);
      firstServlet.setInitOrder(1);
      final ServletHolder soapServlet = new ServletHolder(SoapServlet.class);
      int i = 0;
      for (String engineHandler : engineHandlers) {
        soapServlet.setInitParameter("engine.handler." + i, engineHandler);
        i++;
      }
      soapServlet.setInitOrder(2);
      final Server server =
          new JettyServerFactory()
              .withPort(port)
              .addServlet("/firstServlet", firstServlet)
              .addServlet(basePath + "*", soapServlet)
              .create();
      final SoapClient soapClient = new SoapClient("http://localhost:" + port + basePath);
      return new SoapExtension(port, server, soapClient);
    }
  }


  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (!server.isRunning()) {
      MailboxTestUtil.setUp();
      Provisioning.getInstance()
          .getServerByName(SERVER_NAME)
          .modify(
              new HashMap<>(
                  Map.of(
                      Provisioning.A_zimbraMailPort, String.valueOf(port),
                      ZAttrProvisioning.A_zimbraMailMode, "http",
                      ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
                      ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE")));

      server.start();
      server.getURI();
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    server.stop();
    MailboxTestUtil.tearDown();
  }

}

