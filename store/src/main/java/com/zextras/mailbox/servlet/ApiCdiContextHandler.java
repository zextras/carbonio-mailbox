// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.api.InternalApiApplication;
import com.zimbra.common.localconfig.LC;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.ee8.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee8.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee8.servlet.FilterHolder;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.weld.environment.servlet.EnhancedListener;

/**
 * Single CDI-enabled servlet context that hosts every JAX-RS API, so there is exactly <b>one</b>
 * Weld bootstrap (one {@link EnhancedListener}, one {@code BeanManager}) — avoiding the
 * multiple-container clash you get from a per-context Weld in an embedded, single-classloader server.
 *
 * <p>Root context path ({@code "/"}) placed last in the server's handler sequence, after the
 * {@code /service} context, so it serves {@code /health/*} and {@code /internal/*} without shadowing
 * {@code /service}. The internal API keeps its loopback-only exposure via {@link PortRestrictionFilter}
 * on {@code /internal/*} (the internal connector still binds a loopback address); health stays
 * reachable on all connectors.
 */
public class ApiCdiContextHandler {

  /** Name of the dedicated (loopback) connector the internal API is reachable on. */
  public static final String INTERNAL_CONNECTOR_NAME = "internalApiConnector";

  private ApiCdiContextHandler() {}

  public static ServletContextHandler create() {
    final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    // --- The single Weld bootstrap for the whole JAX-RS layer ---
    context.setInitParameter(
        CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
    context.addServletContainerInitializer(new CdiServletContainerInitializer());
    context.addServletContainerInitializer(new EnhancedListener());

    // Health API — reachable on all connectors.
    context.addServlet(dispatcher(HealthApplication.class, "/health"), "/health/*");

    // Internal API — restricted to the internal (loopback) port, fail-closed.
    context.addServlet(dispatcher(InternalApiApplication.class, "/internal"), "/internal/*");
    context.addFilter(
        new FilterHolder(new PortRestrictionFilter(LC.mailbox_internal_api_port.intValue())),
        "/internal/*",
        EnumSet.of(DispatcherType.REQUEST));

    return context;
  }

  private static ServletHolder dispatcher(Class<?> application, String mappingPrefix) {
    final ServletHolder holder = new ServletHolder(new HttpServletDispatcher());
    holder.setInitParameter("javax.ws.rs.Application", application.getName());
    holder.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");
    // The dispatcher is mapped under a sub-path of this "/" context (e.g. /internal/*), so RESTEasy
    // must strip that prefix before matching @Path — otherwise every request 404s.
    holder.setInitParameter("resteasy.servlet.mapping.prefix", mappingPrefix);
    return holder;
  }
}
