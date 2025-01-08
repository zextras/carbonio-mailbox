/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.servlet.GuiceMailboxServletConfig;
import com.zimbra.common.filters.Base64Filter;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.servlet.ContextPathBasedThreadPoolBalancerFilter;
import com.zimbra.cs.servlet.CsrfFilter;
import com.zimbra.cs.servlet.DoSFilter;
import com.zimbra.cs.servlet.ETagHeaderFilter;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.cs.servlet.RequestStringFilter;
import com.zimbra.cs.servlet.SetHeaderFilter;
import com.zimbra.cs.servlet.SpnegoFilter;
import com.zimbra.cs.servlet.ZimbraInvalidLoginFilter;
import com.zimbra.cs.servlet.ZimbraQoSFilter;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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

		final FilterHolder contextPathBasedThreadPoolBalancerFilter = new FilterHolder(ContextPathBasedThreadPoolBalancerFilter.class);
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("suspendMs", "1000");
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("Rules", String.join(",", configuration.getHttpContextPathBasedThreadPoolBalancingFilterRules()));
		servletContextHandler.addFilter(contextPathBasedThreadPoolBalancerFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(ETagHeaderFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder spnegoFilter = new FilterHolder(SpnegoFilter.class);
		spnegoFilter.setInitParameter("passThruOnFailureUri", "/service/spnego");
		spnegoFilter.setInitParameter("error401Page", "/spnego/error401.jsp");
		servletContextHandler.addFilter(spnegoFilter,"/spnego/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(SetHeaderFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder base64Filter = new FilterHolder(Base64Filter.class);
		servletContextHandler.addFilter(base64Filter,"/user/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(base64Filter,"/home/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(RequestStringFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));
		final FilterHolder csrfFilter = new FilterHolder(CsrfFilter.class);
		csrfFilter.setInitParameter("csrf.req.check", "true");
		csrfFilter.setInitParameter("allowed.referrer.host", "");
		servletContextHandler.addFilter(csrfFilter,"/admin/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/user/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/home/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/upload/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/extension/*", EnumSet.of(DispatcherType.REQUEST));

	}
	private void addListeners(ServletContextHandler servletContextHandler) {
		servletContextHandler.addEventListener(new GuiceMailboxServletConfig());
	}

	private void addServlets(ServletContextHandler servletContextHandler) {
		final var firstServlet = new ServletHolder(FirstServlet.class);
		firstServlet.setInitOrder(1);
		servletContextHandler.addServlet(firstServlet, "/*");

		final var extensionDispatcherServlet = new ServletHolder(ExtensionDispatcherServlet.class);
		extensionDispatcherServlet.setInitOrder(2);
		extensionDispatcherServlet.setInitParameter("allowed.ports", configuration.getMailPort() + ", " + configuration.getMailSSLPort());
		extensionDispatcherServlet.setInitParameter("allowed.ports", configuration.getMailPort() + ", " + configuration.getMailSSLPort());
		// Be careful about long to int conversion, however I just reported the old behavior
		MultipartConfigElement multipartConfig = new MultipartConfigElement("/opt/zextras/data/tmp", configuration.getFileUploadMaxSize(), configuration.getMailContentMaxSize(), (int) configuration.getFileUploadMaxSize());
		extensionDispatcherServlet.getRegistration().setMultipartConfig(multipartConfig);
		servletContextHandler.addServlet(extensionDispatcherServlet, "/*");
	}

	public ServletContextHandler createServletContextHandler() {
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		addListeners(servletContextHandler);
		addFilters(servletContextHandler);
		return servletContextHandler;
	}


}
