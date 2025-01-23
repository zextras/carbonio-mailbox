/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.servlet.GuiceMailboxServletConfig;
import com.zimbra.common.filters.Base64Filter;
import com.zimbra.cs.account.ZAttrServer;
import com.zimbra.cs.dav.service.DavServlet;
import com.zimbra.cs.dav.service.DavWellKnownServlet;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.service.AutoDiscoverServlet;
import com.zimbra.cs.service.CertAuthServlet;
import com.zimbra.cs.service.ContentServlet;
import com.zimbra.cs.service.ExternalUserProvServlet;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.PublicICalServlet;
import com.zimbra.cs.service.SpnegoAuthServlet;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.cs.service.admin.CollectConfigFiles;
import com.zimbra.cs.service.admin.CollectLDAPConfigZimbra;
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
import com.zimbra.cs.servlet.RobotsServlet;
import com.zimbra.cs.servlet.SetHeaderFilter;
import com.zimbra.cs.servlet.SpnegoFilter;
import com.zimbra.cs.servlet.ZimbraInvalidLoginFilter;
import com.zimbra.cs.servlet.ZimbraQoSFilter;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.WsdlServlet;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;

public class MailboxAPIs {

	private final ZAttrServer server;

	public MailboxAPIs(ZAttrServer server) {
		this.server = server;
	}

	private void addFilters(ServletContextHandler servletContextHandler) {
		servletContextHandler.addFilter(new FilterHolder(GuiceFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder dosFilter = new FilterHolder(DoSFilter.class);
		dosFilter.setInitParameter("delayMs", Integer.toString(server.getHttpDosFilterDelayMillis()));
		dosFilter.setInitParameter("maxRequestsPerSec", Integer.toString(server.getHttpDosFilterMaxRequestsPerSec()));
		dosFilter.setInitParameter("remotePort", "true");
		dosFilter.setInitParameter("maxRequestMs", "9223372036854775807");
		servletContextHandler.addFilter(dosFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(ZimbraInvalidLoginFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(new FilterHolder(ZimbraQoSFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder contextPathBasedThreadPoolBalancerFilter = new FilterHolder(ContextPathBasedThreadPoolBalancerFilter.class);
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("suspendMs", "1000");
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("Rules", String.join(",", server.getHttpContextPathBasedThreadPoolBalancingFilterRules()));
		servletContextHandler.addFilter(contextPathBasedThreadPoolBalancerFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(ETagHeaderFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder spnegoFilter = new FilterHolder(SpnegoFilter.class);
		spnegoFilter.setInitParameter("passThruOnFailureUri", "/service/spnego");
		spnegoFilter.setInitParameter("error401Page", "/spnego/error401.jsp");
		servletContextHandler.addFilter(spnegoFilter,"/spnego/*", EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(SetHeaderFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder base64Filter = new FilterHolder(Base64Filter.class);
		final String userPath = "/user/*";
		servletContextHandler.addFilter(base64Filter, userPath, EnumSet.of(DispatcherType.REQUEST));
		final String homePath = "/home/*";
		servletContextHandler.addFilter(base64Filter, homePath, EnumSet.of(DispatcherType.REQUEST));

		servletContextHandler.addFilter(new FilterHolder(RequestStringFilter.class),"/*", EnumSet.of(DispatcherType.REQUEST));
		final FilterHolder csrfFilter = new FilterHolder(CsrfFilter.class);
		csrfFilter.setInitParameter("csrf.req.check", "true");
		csrfFilter.setInitParameter("allowed.referrer.host", "");
		servletContextHandler.addFilter(csrfFilter,"/admin/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter, userPath, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter, homePath, EnumSet.of(DispatcherType.REQUEST));
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
		final String allowedPortsParameter = "allowed.ports";
		extensionDispatcherServlet.setInitParameter(allowedPortsParameter, server.getMailPort() + ", " + server.getMailSSLPort());
		extensionDispatcherServlet.setInitParameter(allowedPortsParameter, server.getMailPort() + ", " + server.getMailSSLPort());
		// Be careful about long to int conversion, however I just reported the old behavior
		MultipartConfigElement multipartConfig = new MultipartConfigElement("/opt/zextras/data/tmp", server.getFileUploadMaxSize(), server.getMailContentMaxSize(), (int) server.getFileUploadMaxSize());
		extensionDispatcherServlet.getRegistration().setMultipartConfig(multipartConfig);
		servletContextHandler.addServlet(extensionDispatcherServlet, "/extension/*");

		final var soapServlet = new ServletHolder(SoapServlet.class);
		soapServlet.setInitOrder(2);
		soapServlet.setInitParameter(
				allowedPortsParameter, server.getMailPort() + ", " + server.getMailSSLPort() + ", 7070, 7443");
		soapServlet.setInitParameter("engine.handler.0", AccountService.class.getName());
		soapServlet.setInitParameter("engine.handler.1", MailService.class.getName());
		servletContextHandler.addServlet(soapServlet, "/soap/*");

		final var adminServlet = new ServletHolder(SoapServlet.class);
		adminServlet.setInitOrder(3);
		adminServlet.setInitParameter(
				allowedPortsParameter, server.getAdminPort() + ", " + server.getMtaAuthPort() + ", 7071, 7073");
		adminServlet.setInitParameter("engine.handler.0", AdminService.class.getName());
		adminServlet.setInitParameter("engine.handler.1", AccountService.class.getName());
		adminServlet.setInitParameter("engine.handler.2", MailService.class.getName());
		servletContextHandler.addServlet(adminServlet, "/admin/soap/*");

		final var wsdlServlet = new ServletHolder(WsdlServlet.class);
		final String defaultPorts = ", 7070, 7443, 7071";
		wsdlServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		servletContextHandler.addServlet(wsdlServlet, "/wsdl/*");

		final var contentServlet = new ServletHolder(ContentServlet.class);
		contentServlet.setInitOrder(5);
		contentServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		contentServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(contentServlet, "/content/*");

		final var previewServlet = new ServletHolder(PreviewServlet.class);
		previewServlet.setInitOrder(13);
		previewServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		servletContextHandler.addServlet(previewServlet, "/preview/*");

		final var pubCalServlet = new ServletHolder(PublicICalServlet.class);
		servletContextHandler.addServlet(pubCalServlet, "/pubcal/*");

		final var userServlet = new ServletHolder(UserServlet.class);
		userServlet.setInitOrder(5);
		userServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		userServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(userServlet, "/user/*");
		servletContextHandler.addServlet(userServlet, "/home/*");

		final var preAuthServlet = new ServletHolder(PreAuthServlet.class);
		preAuthServlet.setInitOrder(5);
		preAuthServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		servletContextHandler.addServlet(preAuthServlet, "/preauth/*");
		servletContextHandler.addServlet(preAuthServlet, "/preauth");

		final var externalUserProvServlet = new ServletHolder(ExternalUserProvServlet.class);
		externalUserProvServlet.setInitOrder(5);
		externalUserProvServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		servletContextHandler.addServlet(externalUserProvServlet, "/extuserprov/*");

		if (server.getMailSSLClientCertPort() > 0) {
			final var certAuthServlet = new ServletHolder(CertAuthServlet.class);
			certAuthServlet.setInitOrder(5);
			certAuthServlet.setInitParameter(allowedPortsParameter,  server.getMailSSLClientCertPortAsString() + ", 9443");
			certAuthServlet.setInitParameter("errorpage.forbidden",  "/error/403.jsp");
			servletContextHandler.addServlet(externalUserProvServlet, "/certauth/*");
			servletContextHandler.addServlet(externalUserProvServlet, "/certauth");
		}

		final var spnegoAuthServlet = new ServletHolder(SpnegoAuthServlet.class);
		spnegoAuthServlet.setInitOrder(5);
		spnegoAuthServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		servletContextHandler.addServlet(spnegoAuthServlet, "/spnego/");
		servletContextHandler.addServlet(spnegoAuthServlet, "/spnego");

		final var fileUploadServlet = new ServletHolder(FileUploadServlet.class);
		fileUploadServlet.setInitOrder(6);
		fileUploadServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " +  server.getAdminPort() + defaultPorts);
		servletContextHandler.addServlet(fileUploadServlet, "/upload");

		final var statsImageServlet = new ServletHolder(StatsImageServlet.class);
		statsImageServlet.setInitOrder(7);
		final String defaultAdminPort = ", 7071";
		statsImageServlet.setInitParameter(allowedPortsParameter,   server.getAdminPort() + defaultAdminPort);
		servletContextHandler.addServlet(statsImageServlet, "/statsimg/*");

		final var proxyServlet = new ServletHolder(ProxyServlet.class);
		proxyServlet.setInitOrder(8);
		final String defaultMailPort = ", 7070";
		proxyServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + defaultMailPort);
		servletContextHandler.addServlet(proxyServlet, "/proxy/*");

		final var davServlet = new ServletHolder(DavServlet.class);
		davServlet.setInitOrder(9);
		davServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + defaultMailPort);
		servletContextHandler.addServlet(davServlet, "/dav/*");

		final var davWellKnownServlet = new ServletHolder(DavWellKnownServlet.class);
		davWellKnownServlet.setInitOrder(9);
		davWellKnownServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + defaultMailPort);
		servletContextHandler.addServlet(davWellKnownServlet, "/.well-known/*");

		final var collectLDAPConfigServlet = new ServletHolder(CollectLDAPConfigZimbra.class);
		collectLDAPConfigServlet.setInitParameter(allowedPortsParameter,  server.getAdminPort() + defaultAdminPort);
		servletContextHandler.addServlet(collectLDAPConfigServlet, "/collectldapconfig/*");

		final var collectConfigFilesServlet = new ServletHolder(CollectConfigFiles.class);
		collectConfigFilesServlet.setInitParameter(allowedPortsParameter,  server.getAdminPort() + defaultAdminPort);
		servletContextHandler.addServlet(collectConfigFilesServlet, "/collectconfig/*");

		final var robotsServlet = new ServletHolder(RobotsServlet.class);
		robotsServlet.setInitOrder(11);
		servletContextHandler.addServlet(robotsServlet, "/robots.txt");

		final var autoDiscoverServlet = new ServletHolder(AutoDiscoverServlet.class);
		autoDiscoverServlet.setInitOrder(12);
		autoDiscoverServlet.setInitParameter(
				allowedPortsParameter,  server.getMailPort() + ", " + server.getMailSSLPort() + ", " + server.getAdminPort() + ", 7070, 7443");
		servletContextHandler.addServlet(autoDiscoverServlet, "/autodiscover/*");
		servletContextHandler.addServlet(autoDiscoverServlet, "/Autodiscover/*");
		servletContextHandler.addServlet(autoDiscoverServlet, "/AutoDiscover/*");
	}

	private static ConstraintMapping buildSecurityMapping(String path, Constraint constraint) {
		// this configures jetty to require HTTPS for all requests
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.setPathSpec(path);
		mapping.setConstraint(constraint);
		return mapping;
	}


	private void addSecurityConstraints(ServletContextHandler servletContextHandler) {
		Constraint constraint = new Constraint();
		constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);
		ConstraintSecurityHandler security = new ConstraintSecurityHandler();
		security.setConstraintMappings(List.of(
				buildSecurityMapping("/service/user/*", constraint),
				buildSecurityMapping("/user/*", constraint),
				buildSecurityMapping("/service/home/*", constraint),
				buildSecurityMapping("/home/*", constraint),
				buildSecurityMapping("/dav/*", constraint)
		));
		servletContextHandler.setSecurityHandler(security);
	}
	/**
	NOTE: JSP is not available anymore, plus
	 the old env-entry is used to setup zimbraServicesEnabled
	 and this is used only in {@link com.zimbra.common.util.WebSplitUtil}
	 */
	public ServletContextHandler createServletContextHandler() {
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		servletContextHandler.setContextPath("/service");
		addListeners(servletContextHandler);
		addFilters(servletContextHandler);
		addServlets(servletContextHandler);

		if("https, redirect".equals(server.getMailModeAsString())) {
			addSecurityConstraints(servletContextHandler);
		}
		return servletContextHandler;
	}


}
