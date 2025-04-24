/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.servlet.GuiceMailboxServletConfig;
import com.zimbra.common.account.ZAttrProvisioning.MailMode;
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
	private static final String USER_PATH = "/user/*";
	private static final String HOME_PATH = "/home/*";

	public MailboxAPIs(ZAttrServer server) {
		this.server = server;
	}

	private void addFilters(ServletContextHandler servletContextHandler) {
		final FilterHolder guiceFilter = new FilterHolder(GuiceFilter.class);
		guiceFilter.setName("guiceFilter");
		guiceFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(guiceFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder dosFilter = new FilterHolder(DoSFilter.class);
		dosFilter.setName("DosFilter");
		dosFilter.setAsyncSupported(true);
		dosFilter.setInitParameter("delayMs", Integer.toString(server.getHttpDosFilterDelayMillis()));
		dosFilter.setInitParameter("maxRequestsPerSec", Integer.toString(server.getHttpDosFilterMaxRequestsPerSec()));
		dosFilter.setInitParameter("remotePort", "true");
		dosFilter.setInitParameter("maxRequestMs", "9223372036854775807");
		servletContextHandler.addFilter(dosFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder invalidLoginFilter = new FilterHolder(ZimbraInvalidLoginFilter.class);
		invalidLoginFilter.setName("ZimbraInvalidLoginFilter");
		invalidLoginFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(invalidLoginFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder qosFilter = new FilterHolder(ZimbraQoSFilter.class);
		qosFilter.setName("ZimbraQosFilter");
		qosFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(qosFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder contextPathBasedThreadPoolBalancerFilter = new FilterHolder(ContextPathBasedThreadPoolBalancerFilter.class);
		contextPathBasedThreadPoolBalancerFilter.setName("ContextPathBasedThreadPoolBalancerFilter");
		contextPathBasedThreadPoolBalancerFilter.setAsyncSupported(true);
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("suspendMs", "1000");
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("Rules", String.join(",", server.getHttpContextPathBasedThreadPoolBalancingFilterRules()));
		servletContextHandler.addFilter(contextPathBasedThreadPoolBalancerFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder eTageFilter = new FilterHolder(ETagHeaderFilter.class);
		eTageFilter.setName("ETagHeaderFilter");
		eTageFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(eTageFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder spnegoFilter = new FilterHolder(SpnegoFilter.class);
		spnegoFilter.setName("SpnegoFilter");
		spnegoFilter.setAsyncSupported(true);
		spnegoFilter.setInitParameter("passThruOnFailureUri", "/service/spnego");
		spnegoFilter.setInitParameter("error401Page", "/spnego/error401.jsp");
		servletContextHandler.addFilter(spnegoFilter,"/spnego/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder setHeaderFilter = new FilterHolder(SetHeaderFilter.class);
		setHeaderFilter.setName("SetHeaderFilter");
		setHeaderFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(setHeaderFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder base64Filter = new FilterHolder(Base64Filter.class);
		base64Filter.setName("Base64Filter");
		base64Filter.setAsyncSupported(true);
		servletContextHandler.addFilter(base64Filter, USER_PATH, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(base64Filter, HOME_PATH, EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder requestStringFilter = new FilterHolder(RequestStringFilter.class);
		requestStringFilter.setName("RequestStringFilter");
		requestStringFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(requestStringFilter,"/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder csrfFilter = new FilterHolder(CsrfFilter.class);
		csrfFilter.setName("CsrfFilter");
		csrfFilter.setAsyncSupported(true);
		csrfFilter.setInitParameter("csrf.req.check", "true");
		csrfFilter.setInitParameter("allowed.referrer.host", "");
		servletContextHandler.addFilter(csrfFilter,"/admin/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter, USER_PATH, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter, HOME_PATH, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/upload/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/extension/*", EnumSet.of(DispatcherType.REQUEST));

	}
	private void addListeners(ServletContextHandler servletContextHandler) {
		servletContextHandler.addEventListener(new GuiceMailboxServletConfig());
	}

	private void addServlets(ServletContextHandler servletContextHandler) {
		final String adminPortOnly = String.valueOf(server.getAdminPort());
		final String adminAndMTAPort = adminPortOnly + ", " + server.getMtaAuthPort();
		final String userOnlyPorts = server.getMailPort() + ", " + server.getMailSSLPort();
		final String userAndAdminPorts = server.getMailPort() + ", " + server.getMailSSLPort() + ", " + adminPortOnly;

		final var firstServlet = new ServletHolder(FirstServlet.class);
		firstServlet.setInitOrder(1);
		firstServlet.setAsyncSupported(true);
		servletContextHandler.addServlet(firstServlet, "/*");

		final var extensionDispatcherServlet = new ServletHolder(ExtensionDispatcherServlet.class);
		extensionDispatcherServlet.setName("ExtensionDispatcherServlet");
		extensionDispatcherServlet.setAsyncSupported(true);
		extensionDispatcherServlet.setInitOrder(2);
		final String allowedPortsParameter = "allowed.ports";
		extensionDispatcherServlet.setInitParameter(allowedPortsParameter, userOnlyPorts);
		extensionDispatcherServlet.setInitParameter(allowedPortsParameter, userOnlyPorts);
		// Be careful about long to int conversion, however I just reported the old behavior
		MultipartConfigElement multipartConfig = new MultipartConfigElement("/opt/zextras/data/tmp", server.getFileUploadMaxSize(), server.getMailContentMaxSize(), (int) server.getFileUploadMaxSize());
		extensionDispatcherServlet.getRegistration().setMultipartConfig(multipartConfig);
		servletContextHandler.addServlet(extensionDispatcherServlet, "/extension/*");

		final var soapServlet = new ServletHolder(SoapServlet.class);
		soapServlet.setName("SoapServlet");
		soapServlet.setAsyncSupported(true);
		soapServlet.setInitOrder(2);
		soapServlet.setInitParameter(
				allowedPortsParameter, userOnlyPorts);
		soapServlet.setInitParameter("engine.handler.0", AccountService.class.getName());
		soapServlet.setInitParameter("engine.handler.1", MailService.class.getName());
		servletContextHandler.addServlet(soapServlet, "/soap/*");

		final var adminServlet = new ServletHolder(SoapServlet.class);
		adminServlet.setAsyncSupported(true);
		adminServlet.setName("AdminServlet");
		adminServlet.setInitOrder(3);
		adminServlet.setInitParameter(
				allowedPortsParameter, adminAndMTAPort);
		adminServlet.setInitParameter("engine.handler.0", AdminService.class.getName());
		adminServlet.setInitParameter("engine.handler.1", AccountService.class.getName());
		adminServlet.setInitParameter("engine.handler.2", MailService.class.getName());
		servletContextHandler.addServlet(adminServlet, "/admin/soap/*");

		final var wsdlServlet = new ServletHolder(WsdlServlet.class);
		wsdlServlet.setName("WsdlServlet");
		wsdlServlet.setAsyncSupported(true);
		wsdlServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(wsdlServlet, "/wsdl/*");

		final var contentServlet = new ServletHolder(ContentServlet.class);
		contentServlet.setName("ContentServlet");
		contentServlet.setAsyncSupported(true);
		contentServlet.setInitOrder(5);
		contentServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		contentServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(contentServlet, "/content/*");

		final var previewServlet = new ServletHolder(PreviewServlet.class);
		previewServlet.setName("PreviewServlet");
		previewServlet.setAsyncSupported(true);
		previewServlet.setInitOrder(13);
		previewServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(previewServlet, "/preview/*");

		final var userServlet = new ServletHolder(UserServlet.class);
		userServlet.setName("UserServlet");
		userServlet.setAsyncSupported(true);
		userServlet.setInitOrder(5);
		userServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		userServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(userServlet, USER_PATH);
		servletContextHandler.addServlet(userServlet, HOME_PATH);

		final var preAuthServlet = new ServletHolder(PreAuthServlet.class);
		preAuthServlet.setName("PreAuthServlet");
		preAuthServlet.setAsyncSupported(true);
		preAuthServlet.setInitOrder(5);
		preAuthServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(preAuthServlet, "/preauth/*");
		servletContextHandler.addServlet(preAuthServlet, "/preauth");

		final var externalUserProvServlet = new ServletHolder(ExternalUserProvServlet.class);
		externalUserProvServlet.setName("ExternalUserProvServlet");
		externalUserProvServlet.setAsyncSupported(true);
		externalUserProvServlet.setInitOrder(5);
		externalUserProvServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(externalUserProvServlet, "/extuserprov/*");

		if (server.getMailSSLClientCertPort() > 0) {
			final var certAuthServlet = new ServletHolder(CertAuthServlet.class);
			certAuthServlet.setName("CertAuthServlet");
			certAuthServlet.setAsyncSupported(true);
			certAuthServlet.setInitOrder(5);
			certAuthServlet.setInitParameter(allowedPortsParameter,  server.getMailSSLClientCertPortAsString() + ", 9443");
			certAuthServlet.setInitParameter("errorpage.forbidden",  "/error/403.jsp");
			servletContextHandler.addServlet(certAuthServlet, "/certauth/*");
			servletContextHandler.addServlet(certAuthServlet, "/certauth");
		}

		final var spnegoAuthServlet = new ServletHolder(SpnegoAuthServlet.class);
		spnegoAuthServlet.setName("SpnegoAuthServlet");
		spnegoAuthServlet.setAsyncSupported(true);
		spnegoAuthServlet.setInitOrder(5);
		spnegoAuthServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(spnegoAuthServlet, "/spnego/");
		servletContextHandler.addServlet(spnegoAuthServlet, "/spnego");

		final var pubCalServlet = new ServletHolder(PublicICalServlet.class);
		pubCalServlet.setName("PubCalServlet");
		pubCalServlet.setAsyncSupported(true);
		pubCalServlet.setInitOrder(5);
		pubCalServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(pubCalServlet, "/pubcal/*");

		final var fileUploadServlet = new ServletHolder(FileUploadServlet.class);
		fileUploadServlet.setName("FileUploadServlet");
		fileUploadServlet.setAsyncSupported(true);
		fileUploadServlet.setInitOrder(6);
		fileUploadServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
		servletContextHandler.addServlet(fileUploadServlet, "/upload");

		final var statsImageServlet = new ServletHolder(StatsImageServlet.class);
		statsImageServlet.setName("StatsImageServlet");
		statsImageServlet.setAsyncSupported(true);
		statsImageServlet.setInitOrder(7);
		statsImageServlet.setInitParameter(allowedPortsParameter, adminPortOnly);
		servletContextHandler.addServlet(statsImageServlet, "/statsimg/*");

		final var proxyServlet = new ServletHolder(ProxyServlet.class);
		proxyServlet.setName("ProxyServlet");
		proxyServlet.setAsyncSupported(true);
		proxyServlet.setInitOrder(8);
		proxyServlet.setInitParameter(
				allowedPortsParameter, userOnlyPorts);
		servletContextHandler.addServlet(proxyServlet, "/proxy/*");

		final var davServlet = new ServletHolder(DavServlet.class);
		davServlet.setName("DavServlet");
		davServlet.setAsyncSupported(true);
		davServlet.setInitOrder(9);
		davServlet.setInitParameter(
				allowedPortsParameter, userOnlyPorts);
		servletContextHandler.addServlet(davServlet, "/dav/*");

		final var davWellKnownServlet = new ServletHolder(DavWellKnownServlet.class);
		davWellKnownServlet.setName("DavWellKnownServlet");
		davWellKnownServlet.setAsyncSupported(true);
		davWellKnownServlet.setInitOrder(9);
		davWellKnownServlet.setInitParameter(
				allowedPortsParameter, userOnlyPorts);
		servletContextHandler.addServlet(davWellKnownServlet, "/.well-known/*");

		final var collectLDAPConfigServlet = new ServletHolder(CollectLDAPConfigZimbra.class);
		collectLDAPConfigServlet.setName("CollectLDAPConfigZimbra");
		collectLDAPConfigServlet.setAsyncSupported(true);
		collectLDAPConfigServlet.setInitParameter(allowedPortsParameter, adminPortOnly);
		servletContextHandler.addServlet(collectLDAPConfigServlet, "/collectldapconfig/*");

		final var collectConfigFilesServlet = new ServletHolder(CollectConfigFiles.class);
		collectConfigFilesServlet.setName("CollectConfigFiles");
		collectConfigFilesServlet.setAsyncSupported(true);
		collectConfigFilesServlet.setInitParameter(allowedPortsParameter, adminPortOnly);
		servletContextHandler.addServlet(collectConfigFilesServlet, "/collectconfig/*");

		final var autoDiscoverServlet = new ServletHolder(AutoDiscoverServlet.class);
		autoDiscoverServlet.setName("AutoDiscoverServlet");
		autoDiscoverServlet.setAsyncSupported(true);
		autoDiscoverServlet.setInitOrder(12);
		autoDiscoverServlet.setInitParameter(
				allowedPortsParameter, userAndAdminPorts);
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
				buildSecurityMapping(USER_PATH, constraint),
				buildSecurityMapping("/service/home/*", constraint),
				buildSecurityMapping(HOME_PATH, constraint),
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

		final String mailModeAsString = server.getMailModeAsString();
		if(mailModeAsString.equals(MailMode.https.name()) ||  mailModeAsString.equals(MailMode.redirect.name())) {
			addSecurityConstraints(servletContextHandler);
		}
		return servletContextHandler;
	}


}
