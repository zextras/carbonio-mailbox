// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.util;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** Test utility class to create a {@link Server} instance with custom port and servlets. */
public class JettyServerFactory {

  private Map<String, ServletHolder> servlets = new HashMap<>();
  private int port = 7070;

  public JettyServerFactory withPort(int port) {
    this.port = port;
    return this;
  }
  public JettyServerFactory addServlet(String path, ServletHolder servletHolder) {
    this.servlets.put(path, servletHolder);
    return this;
  }

  public JettyServerFactory() {}

  /**
   * Creates a server instance (not yet started).
   *
   * @param port listening port of the server
   * @param servlets {@link Map} of servlets with key path of servlet, value {@link ServletHolder}
   * @return {@link Server}
   * @deprecated use {@link #create()} instance method
   * @throws Exception
   */
  public static Server create(int port, Map<String, ServletHolder> servlets) throws Exception {
    final Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    ServletContextHandler servletHandler = new ServletContextHandler();
    servlets.forEach((path, servlet) -> servletHandler.addServlet(servlet, path));
    server.setHandler(servletHandler);
    server.setConnectors(new Connector[] {connector});
    return server;
  }

  public Server create() throws Exception {
    return create(this.port, this.servlets);
  }
}
