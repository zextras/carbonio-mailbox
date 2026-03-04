/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class InternalApiContextHandler {

	private InternalApiContextHandler() {}

	public static final String CONNECTOR_NAME = "internalApiConnector";

	/**
	 * Creates a {@link ServletContextHandler} that serves the internal REST API at {@code /api/v1/*}.
	 * The handler is restricted to the connector named {@value #CONNECTOR_NAME} via virtual host
	 * binding, so it is not reachable on user-facing or admin connectors.
	 */
	public static ServletContextHandler create() {
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/api/v1");
		// NOTE: strictly match connector name, no spoofing. Hosts and wildcard hosts may be followed with
		// From the docs: * '@connectorname', in which case they will match only if the the {@link Connector#getName()} for the request also matches.
		// See also: https://www.javadoc.io/static/org.eclipse.jetty/jetty-server/9.4.57.v20241219/org/eclipse/jetty/server/handler/ContextHandler.html
		context.setVirtualHosts(new String[]{"@" + CONNECTOR_NAME});

		final ServletHolder jerseyServlet = new ServletHolder(
				new ServletContainer(new InternalApiApplication()));
		context.addServlet(jerseyServlet, "/*");

		return context;
	}
}
