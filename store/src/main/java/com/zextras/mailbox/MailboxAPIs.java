/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.servlet.GuiceMailboxServletConfig;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.servlet.DoSFilter;
import com.zimbra.cs.servlet.ZimbraInvalidLoginFilter;
import com.zimbra.cs.servlet.ZimbraQoSFilter;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class MailboxAPIs {

	private final Config configuration;

	public MailboxAPIs(Config configuration) {
		this.configuration = configuration;
	}

	private void addFilters(ServletContextHandler servletContextHandler) {
		servletContextHandler.addFilter(new FilterHolder(GuiceFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder dosFilter = new FilterHolder(DoSFilter.class);
		dosFilter.setInitParameter("delayMs", Integer.toString(configuration.getHttpDosFilterDelayMillis()));
		dosFilter.setInitParameter("maxRequestsPerSec", Integer.toString(configuration.getHttpDosFilterMaxRequestsPerSec()));
		dosFilter.setInitParameter("remotePort", "true");
		dosFilter.setInitParameter("maxRequestMs", "9223372036854775807");
		servletContextHandler.addFilter(dosFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(ZimbraInvalidLoginFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(new FilterHolder(ZimbraQoSFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));
	}
	private void addListeners(ServletContextHandler servletContextHandler) {
		servletContextHandler.addEventListener(new GuiceMailboxServletConfig());
	}

	public ServletContextHandler createServletContextHandler() {
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		addListeners(servletContextHandler);
		addFilters(servletContextHandler);
		return servletContextHandler;
	}


}
