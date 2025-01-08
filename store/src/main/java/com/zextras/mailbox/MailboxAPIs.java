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
import com.zimbra.cs.dav.service.DavServlet;
import com.zimbra.cs.dav.service.DavWellKnownServlet;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.service.CertAuthServlet;
import com.zimbra.cs.service.ContentServlet;
import com.zimbra.cs.service.ExternalUserProvServlet;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.SpnegoAuthServlet;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.cs.service.admin.StatsImageServlet;
import com.zimbra.cs.service.mail.MailService;
import com.zimbra.cs.service.servlet.preauth.PreAuthServlet;
import com.zimbra.cs.service.servlet.preview.PreviewServlet;
import com.zimbra.cs.service.servlet.proxy.ProxyServlet;
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
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.WsdlServlet;
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

		final var soapServlet = new ServletHolder(SoapServlet.class);
		soapServlet.setInitOrder(2);
		soapServlet.setInitParameter("allowed.ports", configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", 7070, 7443");
		soapServlet.setInitParameter("engine.handler.0", AccountService.class.getName());
		soapServlet.setInitParameter("engine.handler.1", MailService.class.getName());
		servletContextHandler.addServlet(soapServlet, "/*");

		final var adminServlet = new ServletHolder(SoapServlet.class);
		adminServlet.setInitOrder(3);
		adminServlet.setInitParameter("allowed.ports", configuration.getAdminPort() + ", " + configuration.getMtaAuthPort() + ", 7071, 7073");
		adminServlet.setInitParameter("engine.handler.0", AdminService.class.getName());
		adminServlet.setInitParameter("engine.handler.1", AccountService.class.getName());
		adminServlet.setInitParameter("engine.handler.2", MailService.class.getName());
		servletContextHandler.addServlet(adminServlet, "/*");

		final var wsdlServlet = new ServletHolder(WsdlServlet.class);
		wsdlServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		servletContextHandler.addServlet(wsdlServlet, "/*");

		final var contentServlet = new ServletHolder(ContentServlet.class);
		contentServlet.setInitOrder(5);
		contentServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		contentServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(contentServlet, "/*");

		final var previewServlet = new ServletHolder(PreviewServlet.class);
		previewServlet.setInitOrder(13);
		previewServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		servletContextHandler.addServlet(previewServlet, "/*");

		final var userServlet = new ServletHolder(UserServlet.class);
		userServlet.setInitOrder(5);
		userServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		userServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(userServlet, "/*");

		final var preAuthServlet = new ServletHolder(PreAuthServlet.class);
		preAuthServlet.setInitOrder(5);
		preAuthServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		servletContextHandler.addServlet(preAuthServlet, "/*");

		final var externalUserProvServlet = new ServletHolder(ExternalUserProvServlet.class);
		externalUserProvServlet.setInitOrder(5);
		externalUserProvServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		servletContextHandler.addServlet(externalUserProvServlet, "/*");

		if (configuration.getMailSSLClientCertPort() > 0) {
			final var certAuthServlet = new ServletHolder(CertAuthServlet.class);
			certAuthServlet.setInitOrder(5);
			certAuthServlet.setInitParameter("allowed.ports",  configuration.getMailSSLClientCertPortAsString() + ", 9443");
			certAuthServlet.setInitParameter("errorpage.forbidden",  "/error/403.jsp");
			servletContextHandler.addServlet(externalUserProvServlet, "/*");
		}

		final var spnegoAuthServlet = new ServletHolder(SpnegoAuthServlet.class);
		spnegoAuthServlet.setInitOrder(5);
		spnegoAuthServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		servletContextHandler.addServlet(spnegoAuthServlet, "/*");

		final var fileUploadServlet = new ServletHolder(FileUploadServlet.class);
		fileUploadServlet.setInitOrder(6);
		fileUploadServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", " +  configuration.getAdminPort() + ", 7070, 7443, 7071");
		servletContextHandler.addServlet(fileUploadServlet, "/*");

		final var statsImageServlet = new ServletHolder(StatsImageServlet.class);
		statsImageServlet.setInitOrder(7);
		statsImageServlet.setInitParameter("allowed.ports",   configuration.getAdminPort() + ", 7071");
		servletContextHandler.addServlet(statsImageServlet, "/*");

		final var proxyServlet = new ServletHolder(ProxyServlet.class);
		proxyServlet.setInitOrder(8);
		proxyServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", 7070");
		servletContextHandler.addServlet(proxyServlet, "/*");

		final var davServlet = new ServletHolder(DavServlet.class);
		davServlet.setInitOrder(9);
		davServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", 7070");
		servletContextHandler.addServlet(davServlet, "/*");

		final var davWellKnownServlet = new ServletHolder(DavWellKnownServlet.class);
		davWellKnownServlet.setInitOrder(9);
		davWellKnownServlet.setInitParameter("allowed.ports",  configuration.getMailPort() + ", " + configuration.getMailSSLPort() + ", 7070");
		servletContextHandler.addServlet(davWellKnownServlet, "/*");
	}

	public ServletContextHandler createServletContextHandler() {
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		addListeners(servletContextHandler);
		addFilters(servletContextHandler);
		return servletContextHandler;
	}


}
