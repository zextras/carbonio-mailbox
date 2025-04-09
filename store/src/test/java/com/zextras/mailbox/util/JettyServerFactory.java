// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


/** Test utility class to create a {@link Server} instance with custom port and servlets. */
public class JettyServerFactory {

  public record ServerWithConfiguration(Server server, int serverPort){}

  private final Map<String, ServletHolder> servlets = new HashMap<>();
  private final Map<String, FilterHolder> filters = new HashMap<>();
  private final List<EventListener> listeners = new ArrayList<>();
  private final int port = PortUtil.findFreePort();

  public JettyServerFactory addServlet(String path, ServletHolder servletHolder) {
    this.servlets.put(path, servletHolder);
    return this;
  }
  public JettyServerFactory addFilter(String path, FilterHolder filterHolder) {
    this.filters.put(path, filterHolder);
    return this;
  }

  public JettyServerFactory addListener(EventListener listener) {
    this.listeners.add(listener);
    return this;
  }

  public JettyServerFactory() {}

  /**
   * Creates a server instance (not yet started).
   *
   * @return {@link Server}
   */
  public ServerWithConfiguration create() {
    final Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    connector.setHost("localhost");
    ServletContextHandler servletContextHandler = new ServletContextHandler();
    listeners.forEach(servletContextHandler::addEventListener);
    filters.forEach((path, filterHolder) -> servletContextHandler.addFilter(filterHolder, path, EnumSet.of(DispatcherType.REQUEST)));
    servlets.forEach((path, servlet) -> servletContextHandler.addServlet(servlet, path));
    server.setHandler(servletContextHandler);
    server.setConnectors(new Connector[] {connector});
    return new ServerWithConfiguration(server, port);
  }
}
