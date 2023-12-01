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
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SoapExtension implements BeforeAllCallback, AfterAllCallback {


  public SoapClient getSoapClient() {
    return soapClient;
  }

  private static SoapClient soapClient;
  private static Server server;

  private final int port;
  private final String basePath;
  private final String engineHandler;

  private SoapExtension(int port, String basePath, String engineHandler) {
    this.port = port;
    this.engineHandler = engineHandler;
    this.basePath = basePath;
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

    private int port = 8080;
    private String basePath = "/";
    private final String engineHandler;

    public Builder(String engineHandler) {
      this.engineHandler = engineHandler;
    }

    public SoapExtension create() {
      return new SoapExtension(port, basePath, engineHandler);
    }
  }


  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
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
    final ServletHolder firstServlet = new ServletHolder(FirstServlet.class);
    firstServlet.setInitOrder(1);
    final ServletHolder soapServlet = new ServletHolder(SoapServlet.class);
    soapServlet.setInitParameter("engine.handler.0", engineHandler);
    soapServlet.setInitOrder(2);
    server =
        new JettyServerFactory()
            .withPort(port)
            .addServlet("/firstServlet", firstServlet)
            .addServlet( basePath + "*", soapServlet)
            .create();
    server.start();
    soapClient = new SoapClient(server.getURI().toString() + basePath);
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    server.stop();
    MailboxTestUtil.tearDown();
  }

}

