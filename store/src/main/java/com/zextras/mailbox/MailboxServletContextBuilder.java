/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.api.InternalApiApplication;
import com.zextras.mailbox.metric.CarbonioMetricRegisterer;
import com.zextras.mailbox.metric.Metrics;
import com.zextras.mailbox.servlet.HealthApplication;
import com.zextras.mailbox.servlet.PortRestrictionFilter;
import com.zimbra.common.account.ZAttrProvisioning.MailMode;
import com.zimbra.common.filters.Base64Filter;
import com.zimbra.common.localconfig.LC;
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
import com.zimbra.cs.servlet.RequestStringFilter;
import com.zimbra.cs.servlet.SetHeaderFilter;
import com.zimbra.cs.servlet.TracingSpanFilter;
import com.zimbra.cs.servlet.ZimbraInvalidLoginFilter;
import com.zimbra.cs.servlet.ZimbraQoSFilter;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.WsdlServlet;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import org.eclipse.jetty.ee8.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee8.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee8.security.ConstraintMapping;
import org.eclipse.jetty.ee8.security.ConstraintSecurityHandler;
import org.eclipse.jetty.ee8.nested.ServletConstraint;
import org.eclipse.jetty.ee8.servlet.FilterHolder;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.weld.environment.servlet.EnhancedListener;

public class MailboxServletContextBuilder {

	private static final String USER_PATH = "/service/user/*";
	private static final String HOME_PATH = "/service/home/*";
	private static final String ALLOWED_PORTS = "allowed.ports";

	private final ZAttrServer server;

	public MailboxServletContextBuilder(ZAttrServer server) {
		this.server = server;
	}

	public ServletContextHandler createServletContextHandler() {
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		servletContextHandler.setContextPath("/");
		addListeners(servletContextHandler);
		addFilters(servletContextHandler);
		addServlets(servletContextHandler);

		final String mailModeAsString = server.getMailModeAsString();
		if (mailModeAsString.equals(MailMode.https.name()) || mailModeAsString.equals(MailMode.redirect.name())) {
			addSecurityConstraints(servletContextHandler);
		}
		return servletContextHandler;
	}

	private void addListeners(ServletContextHandler servletContextHandler) {
		servletContextHandler.setInitParameter(
				CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
		servletContextHandler.addServletContainerInitializer(new CdiServletContainerInitializer());
		servletContextHandler.addServletContainerInitializer(new EnhancedListener());
	}

	private void addFilters(ServletContextHandler servletContextHandler) {
		final FilterHolder tracingSpanFilter = new FilterHolder(TracingSpanFilter.class);
		tracingSpanFilter.setName("TracingSpanFilter");
		tracingSpanFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(tracingSpanFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder dosFilter = new FilterHolder(DoSFilter.class);
		dosFilter.setName("DosFilter");
		dosFilter.setAsyncSupported(true);
		dosFilter.setInitParameter("delayMs", Integer.toString(server.getHttpDosFilterDelayMillis()));
		dosFilter.setInitParameter("maxRequestsPerSec", Integer.toString(server.getHttpDosFilterMaxRequestsPerSec()));
		dosFilter.setInitParameter("remotePort", "false");
		dosFilter.setInitParameter("maxRequestMs", "9223372036854775807");
		servletContextHandler.addFilter(dosFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder invalidLoginFilter = new FilterHolder(ZimbraInvalidLoginFilter.class);
		invalidLoginFilter.setName("ZimbraInvalidLoginFilter");
		invalidLoginFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(invalidLoginFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder qosFilter = new FilterHolder(ZimbraQoSFilter.class);
		qosFilter.setName("ZimbraQosFilter");
		qosFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(qosFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder contextPathBasedThreadPoolBalancerFilter = new FilterHolder(ContextPathBasedThreadPoolBalancerFilter.class);
		contextPathBasedThreadPoolBalancerFilter.setName("ContextPathBasedThreadPoolBalancerFilter");
		contextPathBasedThreadPoolBalancerFilter.setAsyncSupported(true);
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("suspendMs", "1000");
		contextPathBasedThreadPoolBalancerFilter.setInitParameter("Rules", String.join(",", server.getHttpContextPathBasedThreadPoolBalancingFilterRules()));
		servletContextHandler.addFilter(contextPathBasedThreadPoolBalancerFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder eTageFilter = new FilterHolder(ETagHeaderFilter.class);
		eTageFilter.setName("ETagHeaderFilter");
		eTageFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(eTageFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder setHeaderFilter = new FilterHolder(SetHeaderFilter.class);
		setHeaderFilter.setName("SetHeaderFilter");
		setHeaderFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(setHeaderFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder base64Filter = new FilterHolder(Base64Filter.class);
		base64Filter.setName("Base64Filter");
		base64Filter.setAsyncSupported(true);
		servletContextHandler.addFilter(base64Filter, USER_PATH, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(base64Filter, HOME_PATH, EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder requestStringFilter = new FilterHolder(RequestStringFilter.class);
		requestStringFilter.setName("RequestStringFilter");
		requestStringFilter.setAsyncSupported(true);
		servletContextHandler.addFilter(requestStringFilter,"/service/*", EnumSet.of(DispatcherType.REQUEST));

		final FilterHolder csrfFilter = new FilterHolder(CsrfFilter.class);
		csrfFilter.setName("CsrfFilter");
		csrfFilter.setAsyncSupported(true);
		csrfFilter.setInitParameter("csrf.req.check", "true");
		csrfFilter.setInitParameter("allowed.referrer.host", "");
		servletContextHandler.addFilter(csrfFilter,"/service/admin/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/service/soap/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter, USER_PATH, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter, HOME_PATH, EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/service/upload/*", EnumSet.of(DispatcherType.REQUEST));
		servletContextHandler.addFilter(csrfFilter,"/service/extension/*", EnumSet.of(DispatcherType.REQUEST));

		// Public filters above are scoped to /service/* so /internal stays off them; it is restricted
		// to its loopback port instead.
		servletContextHandler.addFilter(
				new FilterHolder(new PortRestrictionFilter(LC.mailbox_internal_api_port.intValue())),
				"/internal/*", EnumSet.of(DispatcherType.REQUEST));
	}

	private void addServlets(ServletContextHandler servletContextHandler) {
		addHealthServlet(servletContextHandler);
		addInternalApiServlet(servletContextHandler);
		addMetricsServlet(servletContextHandler);
		addExtensionServlet(servletContextHandler);
		addSoapServlet(servletContextHandler);
		addAdminServlet(servletContextHandler);
		addWsdlServlet(servletContextHandler);
		addContentServlet(servletContextHandler);
		addPreviewServlet(servletContextHandler);
		addUserServlet(servletContextHandler);
		addPreAuthServlet(servletContextHandler);
		addExternalUserProvServlet(servletContextHandler);
		addCertAuthServlet(servletContextHandler);
		addPublicCalendarServlet(servletContextHandler);
		addFileUploadServlet(servletContextHandler);
		addStatsImageServlet(servletContextHandler);
		addProxyServlet(servletContextHandler);
		addDavServlet(servletContextHandler);
		addDavWellKnownServlet(servletContextHandler);
		addCollectLdapConfigServlet(servletContextHandler);
		addCollectConfigFilesServlet(servletContextHandler);
		addAutoDiscoverServlet(servletContextHandler);
	}

	private void addHealthServlet(ServletContextHandler servletContextHandler) {
		servletContextHandler.addServlet(
				cdiJaxrsDispatcher(HealthApplication.class, "/service/health"), "/service/health/*");
	}

	private void addInternalApiServlet(ServletContextHandler servletContextHandler) {
		servletContextHandler.addServlet(
				cdiJaxrsDispatcher(InternalApiApplication.class, "/internal"), "/internal/*");
	}

	private void addMetricsServlet(ServletContextHandler servletContextHandler) {
		CarbonioMetricRegisterer.register(Metrics.COLLECTOR_REGISTRY);
		final var metricsServlet =
				new ServletHolder(new io.prometheus.client.exporter.MetricsServlet(Metrics.COLLECTOR_REGISTRY));
		metricsServlet.setName("MetricsServlet");
		servletContextHandler.addServlet(metricsServlet, "/service/metrics");
	}

	private void addExtensionServlet(ServletContextHandler servletContextHandler) {
		final var extensionDispatcherServlet = new ServletHolder(ExtensionDispatcherServlet.class);
		extensionDispatcherServlet.setName("ExtensionDispatcherServlet");
		extensionDispatcherServlet.setAsyncSupported(true);
		extensionDispatcherServlet.setInitOrder(2);
		extensionDispatcherServlet.setInitParameter(ALLOWED_PORTS, userOnlyPorts());
		// Be careful about long to int conversion, however I just reported the old behavior
		MultipartConfigElement multipartConfig = new MultipartConfigElement("/opt/zextras/data/tmp", server.getFileUploadMaxSize(), server.getMailContentMaxSize(), (int) server.getFileUploadMaxSize());
		extensionDispatcherServlet.getRegistration().setMultipartConfig(multipartConfig);
		servletContextHandler.addServlet(extensionDispatcherServlet, "/service/extension/*");
	}

	private void addSoapServlet(ServletContextHandler servletContextHandler) {
		final var soapServlet = new ServletHolder(SoapServlet.class);
		soapServlet.setName("SoapServlet");
		soapServlet.setAsyncSupported(true);
		soapServlet.setInitOrder(2);
		soapServlet.setInitParameter(ALLOWED_PORTS, userOnlyPorts());
		soapServlet.setInitParameter("engine.handler.0", AccountService.class.getName());
		soapServlet.setInitParameter("engine.handler.1", MailService.class.getName());
		servletContextHandler.addServlet(soapServlet, "/service/soap/*");
	}

	private void addAdminServlet(ServletContextHandler servletContextHandler) {
		final var adminServlet = new ServletHolder(SoapServlet.class);
		adminServlet.setAsyncSupported(true);
		adminServlet.setName("AdminServlet");
		adminServlet.setInitOrder(3);
		adminServlet.setInitParameter(ALLOWED_PORTS, adminAndMtaPorts());
		adminServlet.setInitParameter("engine.handler.0", AdminService.class.getName());
		adminServlet.setInitParameter("engine.handler.1", AccountService.class.getName());
		adminServlet.setInitParameter("engine.handler.2", MailService.class.getName());
		servletContextHandler.addServlet(adminServlet, "/service/admin/soap/*");
	}

	private void addWsdlServlet(ServletContextHandler servletContextHandler) {
		final var wsdlServlet = new ServletHolder(WsdlServlet.class);
		wsdlServlet.setName("WsdlServlet");
		wsdlServlet.setAsyncSupported(true);
		wsdlServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(wsdlServlet, "/service/wsdl/*");
	}

	private void addContentServlet(ServletContextHandler servletContextHandler) {
		final var contentServlet = new ServletHolder(ContentServlet.class);
		contentServlet.setName("ContentServlet");
		contentServlet.setAsyncSupported(true);
		contentServlet.setInitOrder(5);
		contentServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		contentServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(contentServlet, "/service/content/*");
	}

	private void addPreviewServlet(ServletContextHandler servletContextHandler) {
		final var previewServlet = new ServletHolder(PreviewServlet.class);
		previewServlet.setName("PreviewServlet");
		previewServlet.setAsyncSupported(true);
		previewServlet.setInitOrder(13);
		previewServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(previewServlet, "/service/preview/*");
	}

	private void addUserServlet(ServletContextHandler servletContextHandler) {
		final var userServlet = new ServletHolder(UserServlet.class);
		userServlet.setName("UserServlet");
		userServlet.setAsyncSupported(true);
		userServlet.setInitOrder(5);
		userServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		userServlet.setInitParameter("errorpage.attachment.blocked",  "/error/attachment_blocked.jsp");
		servletContextHandler.addServlet(userServlet, USER_PATH);
		servletContextHandler.addServlet(userServlet, HOME_PATH);
	}

	private void addPreAuthServlet(ServletContextHandler servletContextHandler) {
		final var preAuthServlet = new ServletHolder(PreAuthServlet.class);
		preAuthServlet.setName("PreAuthServlet");
		preAuthServlet.setAsyncSupported(true);
		preAuthServlet.setInitOrder(5);
		preAuthServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(preAuthServlet, "/service/preauth/*");
		servletContextHandler.addServlet(preAuthServlet, "/service/preauth");
	}

	private void addExternalUserProvServlet(ServletContextHandler servletContextHandler) {
		final var externalUserProvServlet = new ServletHolder(ExternalUserProvServlet.class);
		externalUserProvServlet.setName("ExternalUserProvServlet");
		externalUserProvServlet.setAsyncSupported(true);
		externalUserProvServlet.setInitOrder(5);
		externalUserProvServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(externalUserProvServlet, "/service/extuserprov/*");
	}

	private void addCertAuthServlet(ServletContextHandler servletContextHandler) {
		if (server.getMailSSLClientCertPort() <= 0) {
			return;
		}
		final var certAuthServlet = new ServletHolder(CertAuthServlet.class);
		certAuthServlet.setName("CertAuthServlet");
		certAuthServlet.setAsyncSupported(true);
		certAuthServlet.setInitOrder(5);
		certAuthServlet.setInitParameter(ALLOWED_PORTS, server.getMailSSLClientCertPortAsString() + ", 9443");
		certAuthServlet.setInitParameter("errorpage.forbidden",  "/error/403.jsp");
		servletContextHandler.addServlet(certAuthServlet, "/service/certauth/*");
		servletContextHandler.addServlet(certAuthServlet, "/service/certauth");
	}

	private void addPublicCalendarServlet(ServletContextHandler servletContextHandler) {
		final var pubCalServlet = new ServletHolder(PublicICalServlet.class);
		pubCalServlet.setName("PubCalServlet");
		pubCalServlet.setAsyncSupported(true);
		pubCalServlet.setInitOrder(5);
		pubCalServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(pubCalServlet, "/service/pubcal/*");
	}

	private void addFileUploadServlet(ServletContextHandler servletContextHandler) {
		final var fileUploadServlet = new ServletHolder(FileUploadServlet.class);
		fileUploadServlet.setName("FileUploadServlet");
		fileUploadServlet.setAsyncSupported(true);
		fileUploadServlet.setInitOrder(6);
		fileUploadServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(fileUploadServlet, "/service/upload");
	}

	private void addStatsImageServlet(ServletContextHandler servletContextHandler) {
		final var statsImageServlet = new ServletHolder(StatsImageServlet.class);
		statsImageServlet.setName("StatsImageServlet");
		statsImageServlet.setAsyncSupported(true);
		statsImageServlet.setInitOrder(7);
		statsImageServlet.setInitParameter(ALLOWED_PORTS, adminPortOnly());
		servletContextHandler.addServlet(statsImageServlet, "/service/statsimg/*");
	}

	private void addProxyServlet(ServletContextHandler servletContextHandler) {
		final var proxyServlet = new ServletHolder(ProxyServlet.class);
		proxyServlet.setName("ProxyServlet");
		proxyServlet.setAsyncSupported(true);
		proxyServlet.setInitOrder(8);
		proxyServlet.setInitParameter(ALLOWED_PORTS, userOnlyPorts());
		servletContextHandler.addServlet(proxyServlet, "/service/proxy/*");
	}

	private void addDavServlet(ServletContextHandler servletContextHandler) {
		final var davServlet = new ServletHolder(DavServlet.class);
		davServlet.setName("DavServlet");
		davServlet.setAsyncSupported(true);
		davServlet.setInitOrder(9);
		davServlet.setInitParameter(ALLOWED_PORTS, userOnlyPorts());
		servletContextHandler.addServlet(davServlet, "/service/dav/*");
	}

	private void addDavWellKnownServlet(ServletContextHandler servletContextHandler) {
		final var davWellKnownServlet = new ServletHolder(DavWellKnownServlet.class);
		davWellKnownServlet.setName("DavWellKnownServlet");
		davWellKnownServlet.setAsyncSupported(true);
		davWellKnownServlet.setInitOrder(9);
		davWellKnownServlet.setInitParameter(ALLOWED_PORTS, userOnlyPorts());
		servletContextHandler.addServlet(davWellKnownServlet, "/service/.well-known/*");
	}

	private void addCollectLdapConfigServlet(ServletContextHandler servletContextHandler) {
		final var collectLDAPConfigServlet = new ServletHolder(CollectLDAPConfigZimbra.class);
		collectLDAPConfigServlet.setName("CollectLDAPConfigZimbra");
		collectLDAPConfigServlet.setAsyncSupported(true);
		collectLDAPConfigServlet.setInitParameter(ALLOWED_PORTS, adminPortOnly());
		servletContextHandler.addServlet(collectLDAPConfigServlet, "/service/collectldapconfig/*");
	}

	private void addCollectConfigFilesServlet(ServletContextHandler servletContextHandler) {
		final var collectConfigFilesServlet = new ServletHolder(CollectConfigFiles.class);
		collectConfigFilesServlet.setName("CollectConfigFiles");
		collectConfigFilesServlet.setAsyncSupported(true);
		collectConfigFilesServlet.setInitParameter(ALLOWED_PORTS, adminPortOnly());
		servletContextHandler.addServlet(collectConfigFilesServlet, "/service/collectconfig/*");
	}

	private void addAutoDiscoverServlet(ServletContextHandler servletContextHandler) {
		final var autoDiscoverServlet = new ServletHolder(AutoDiscoverServlet.class);
		autoDiscoverServlet.setName("AutoDiscoverServlet");
		autoDiscoverServlet.setAsyncSupported(true);
		autoDiscoverServlet.setInitOrder(12);
		autoDiscoverServlet.setInitParameter(ALLOWED_PORTS, userAndAdminPorts());
		servletContextHandler.addServlet(autoDiscoverServlet, "/service/autodiscover/*");
		servletContextHandler.addServlet(autoDiscoverServlet, "/service/Autodiscover/*");
		servletContextHandler.addServlet(autoDiscoverServlet, "/service/AutoDiscover/*");
	}

	private void addSecurityConstraints(ServletContextHandler servletContextHandler) {
		ServletConstraint constraint = new ServletConstraint();
		constraint.setDataConstraint(ServletConstraint.DC_CONFIDENTIAL);
		ConstraintSecurityHandler security = new ConstraintSecurityHandler();
		security.setConstraintMappings(List.of(
				buildSecurityMapping(USER_PATH, constraint),
				buildSecurityMapping(HOME_PATH, constraint),
				buildSecurityMapping("/service/dav/*", constraint)
		));
		servletContextHandler.setSecurityHandler(security);
	}

	private static ConstraintMapping buildSecurityMapping(String path, ServletConstraint constraint) {
		// this configures jetty to require HTTPS for all requests
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.setPathSpec(path);
		mapping.setConstraint(constraint);
		return mapping;
	}

	private static ServletHolder cdiJaxrsDispatcher(Class<?> application, String mappingPrefix) {
		final var holder = new ServletHolder(new HttpServletDispatcher());
		holder.setInitParameter("javax.ws.rs.Application", application.getName());
		holder.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");
		holder.setInitParameter("resteasy.servlet.mapping.prefix", mappingPrefix);
		return holder;
	}

	private String adminPortOnly() {
		return String.valueOf(server.getAdminPort());
	}

	private String adminAndMtaPorts() {
		return adminPortOnly() + ", " + server.getMtaAuthPort();
	}

	private String userOnlyPorts() {
		return server.getMailPort() + ", " + server.getMailSSLPort();
	}

	private String userAndAdminPorts() {
		return userOnlyPorts() + ", " + adminPortOnly();
	}
}
