// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.util;

import java.util.Map;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** Test utility class to create a {@link Server} instance with custom port and servlets. */
public class JettyServerFactory {

  private JettyServerFactory() {}

  /**
   * @param port listening port of the server
   * @param servlets {@link Map} of servlets with key path of servlet, value {@link ServletHolder}
   * @return {@link Server}
   * @throws Exception
   */
  public static Server create(int port, Map<String, ServletHolder> servlets) throws Exception {
    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    ServletContextHandler servletHandler = new ServletContextHandler();
    servlets.forEach((path, servlet) -> servletHandler.addServlet(servlet, path));
    server.setHandler(servletHandler);
    server.setConnectors(new Connector[] {connector});
    return server;
  }
}
