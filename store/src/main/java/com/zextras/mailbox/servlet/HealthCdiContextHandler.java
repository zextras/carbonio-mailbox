// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import org.eclipse.jetty.ee8.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee8.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.weld.environment.servlet.EnhancedListener;

/**
 * Builds a {@link ServletContextHandler} serving the CDI-managed health endpoint at {@code /health/*}.
 *
 * <p>CDI is bootstrapped in embedded Jetty by combining Jetty's {@link CdiServletContainerInitializer}
 * (in {@link CdiDecoratingListener#MODE}, so Weld decorates Jetty-created objects) with Weld's own
 * {@link EnhancedListener} (which discovers beans and creates the {@code BeanManager}). RESTEasy then
 * resolves resources from that {@code BeanManager} via {@code CdiInjectorFactory}.
 */
public class HealthCdiContextHandler {

  private HealthCdiContextHandler() {}

  public static ServletContextHandler create() {
    final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/health");

    // Jetty <-> CDI integration: Weld provides the decorator for CDI injection into the context.
    context.setInitParameter(
        CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
    context.addServletContainerInitializer(new CdiServletContainerInitializer());
    context.addServletContainerInitializer(new EnhancedListener());

    // RESTEasy 4 dispatcher; resource instances come from the CDI BeanManager.
    final ServletHolder servlet = new ServletHolder(new HttpServletDispatcher());
    servlet.setInitParameter("javax.ws.rs.Application", HealthApplication.class.getName());
    servlet.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");
    context.addServlet(servlet, "/*");

    return context;
  }
}
