package com.zextras.mailbox.server;

import com.zextras.mailbox.MailboxAPIs;
import com.zextras.mailbox.server.MailboxServer.InstantiationException;
import com.zimbra.common.jetty.JettyMonitor;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Config;
import java.io.IOException;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.rewrite.handler.MsieSslRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewritePatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HostHeaderCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;


// See: https://github.com/jetty/jetty.project/blob/jetty-9.4.x/examples/embedded/src/main/java/org/eclipse/jetty/embedded/LikeJettyXml.java
// See previous jetty.xml config for reference: https://github.com/zextras/carbonio-appserver/blob/81bce01f4b97efd89ccf89e79c963b40f16ffc81/appserver/conf/jetty/jetty.xml.production
public class MailboxServerBuilder {

	private final Config config;
	private final com.zimbra.cs.account.Server localServer;
	private HttpConfiguration httpsConfig;
	private SslContextFactory sslContextFactory;
	private boolean dump = true;

	public MailboxServerBuilder(Config config, com.zimbra.cs.account.Server localServer) {
		this.config = config;
		this.localServer = localServer;
	}

	public MailboxServerBuilder withDump(boolean dump) {
		this.dump = dump;
		return this;
	}

	private static RequestLogHandler createRequestLogHandler() {
		final String accessLogFileName = LC.zimbra_log_directory.value() + "/access_log.yyyy_mm_dd";
		final NCSARequestLog ncsaRequestLog = new NCSARequestLog(accessLogFileName);
		ncsaRequestLog.setLogDateFormat("dd/MMM/yyyy:HH:mm:ss:ms Z");
		ncsaRequestLog.setRetainDays(30);
		ncsaRequestLog.setAppend(true);
		ncsaRequestLog.setExtended(true);
		ncsaRequestLog.setFilenameDateFormat("yyyy-MM-dd");
		ncsaRequestLog.setPreferProxiedForAddress(true);
		ncsaRequestLog.setLogLatency(true);

		final RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog(ncsaRequestLog);
		return requestLogHandler;
	}

	public MailboxServer create() throws InstantiationException {
		try {
			final ThreadPool threadPool = createThreadPool();
			Server server = new Server(threadPool);

			final HttpConfiguration httpConfig = createHttpConfig();
			this.httpsConfig = createHttpsConfig(httpConfig);
			this.sslContextFactory = createSSLContextFactory();

			final ServerConnector userHttpConnector = createUserHttpConnector(server, httpConfig);
			server.addConnector(userHttpConnector);

			final ServerConnector userHttpsConnector = createUserHttpsConnector(server);
			userHttpsConnector.setName("userHttpsConnector");
			server.addConnector(userHttpsConnector);

			final ServerConnector adminHttpsConnector = createAdminHttpsConnector(server);
			server.addConnector(adminHttpsConnector);

			server.addConnector(createMtaAdminHttpsConnector(server));
			server.addConnector(createExtensionsHttpsConnector(server));
			final ContextHandlerCollection contexts = new ContextHandlerCollection();
			Handler webAppHandler = new MailboxAPIs(localServer).createServletContextHandler();

			final RewriteHandler mainHandler = createRewriteHandler();
			mainHandler.setHandler(new HandlerCollection(contexts, webAppHandler,
					createRequestLogHandler()));

			if (localServer.isHttpCompressionEnabled()) {
				final GzipHandler gzipHandler = new GzipHandler();
				gzipHandler.setHandler(mainHandler);
				gzipHandler.setMinGzipSize(2048);
				gzipHandler.setCompressionLevel(-1);
				gzipHandler.setExcludedAgentPatterns(".*MSIE.6\\.0.*");
				gzipHandler.setIncludedMethods("GET", "POST");
				server.setHandler(gzipHandler);
			} else {
				server.setHandler(mainHandler);
			}
			userHttpConnector.open();
			adminHttpsConnector.open();

			server.setStopAtShutdown(dump);
			server.setDumpAfterStart(dump);
			server.setDumpBeforeStop(dump);

			return new MailboxServer(server);
		} catch (IOException e) {
			throw new InstantiationException(e.getCause());
		}
	}


	private ServerConnector createHttpsConnector(Server server, int port, int idleTimeMillis,
			String host) {
		ServerConnector serverConnector = new ServerConnector(server,
				new SslConnectionFactory(this.sslContextFactory, HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(this.httpsConfig));
		serverConnector.setPort(port);
		serverConnector.setHost(host);
		serverConnector.setIdleTimeout(idleTimeMillis);
		return serverConnector;
	}

	private ServerConnector createUserHttpConnector(Server server, HttpConfiguration httpConfig) {
		ServerConnector serverConnector = new ServerConnector(server,
				new HttpConnectionFactory(httpConfig));
		serverConnector.setPort(localServer.getMailPort());
		serverConnector.setHost(localServer.getMailBindAddress());
		serverConnector.setIdleTimeout(localServer.getHttpConnectorMaxIdleTimeMillis());
		return serverConnector;
	}

	private ServerConnector createUserHttpsConnector(Server server) {
		return createHttpsConnector(server, localServer.getMailSSLPort(),
				localServer.getHttpConnectorMaxIdleTimeMillis(),
				localServer.getMailBindAddress());
	}

	private ServerConnector createAdminHttpsConnector(Server server) {
		return createHttpsConnector(server, localServer.getAdminPort(), 0,
				localServer.getAdminBindAddress());
	}

	private ServerConnector createMtaAdminHttpsConnector(Server server) {
		return createHttpsConnector(server, localServer.getMtaAuthPort(), 0,
				localServer.getMtaAuthBindAddress());
	}

	private ServerConnector createExtensionsHttpsConnector(Server server) {
		return createHttpsConnector(server, localServer.getExtensionBindPort(),
				localServer.getHttpConnectorMaxIdleTimeMillis(),
				localServer.getExtensionBindAddress());
	}

	private RewriteHandler createRewriteHandler() {
		final RewriteHandler rewriteHandler = new RewriteHandler();
		rewriteHandler.setRewriteRequestURI(true);
		rewriteHandler.setRewritePathInfo(false);
		rewriteHandler.setOriginalPathAttribute("requestedPath");

		rewriteHandler.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC,
				DispatcherType.ERROR,
				DispatcherType.FORWARD);
		rewriteHandler.addRule(new MsieSslRule());

		final String mailURL = localServer.getMailURL();

		rewriteHandler.addRule(new RewritePatternRule("/Microsoft-Server-ActiveSync/*",
				"/service/extension/zimbrasync"));
		rewriteHandler.addRule(
				new RewriteRegexRule("(?i)/ews/Exchange.asmx/*", "/service/extension/zimbraews"));
		rewriteHandler.addRule(new RewritePatternRule("/principals/*", "/service/dav/principals"));
		rewriteHandler.addRule(new RewritePatternRule("/dav/*", "/service/dav/home"));
		rewriteHandler.addRule(new RewritePatternRule("/.well-known/*", "/service/.well-known"));
		rewriteHandler.addRule(new RewritePatternRule("/.well-known/*", "/service/.well-known"));
		rewriteHandler.addRule(new RewritePatternRule("/home/*", "/service/home/"));
		rewriteHandler.addRule(new RewritePatternRule(mailURL + "/home/*", "/service/home"));
		rewriteHandler.addRule(new RewritePatternRule("/user/*", "/service/user/"));
		rewriteHandler.addRule(new RewritePatternRule(mailURL + "/user/*", "/service/user"));
		rewriteHandler.addRule(new RewritePatternRule("/shf/*", "/service/shf/"));
		rewriteHandler.addRule(new RewritePatternRule("/certauth/*", "/service/certauth"));
		rewriteHandler.addRule(
				new RewritePatternRule("/autodiscover/*", "/service/extension/autodiscover"));
		rewriteHandler.addRule(
				new RewritePatternRule("/Autodiscover/*", "/service/extension/autodiscover"));
		rewriteHandler.addRule(
				new RewritePatternRule("/AutoDiscover/*", "/service/extension/autodiscover"));

		final RewritePatternRule serviceRule = new RewritePatternRule("/service/*", "/service");
		serviceRule.setTerminating(true);
		rewriteHandler.addRule(serviceRule);

		final RewritePatternRule carbonioAdminRule = new RewritePatternRule("/carbonioAdmin/*",
				"/carbonioAdmin");
		carbonioAdminRule.setTerminating(true);
		rewriteHandler.addRule(carbonioAdminRule);

		final RewritePatternRule rootRule = new RewritePatternRule(mailURL + "/*", "/");
		rootRule.setTerminating(true);
		rewriteHandler.addRule(rootRule);

		final RewritePatternRule rootRule2 = new RewritePatternRule("/*", mailURL);
		rootRule2.setTerminating(true);
		rewriteHandler.addRule(rootRule2);
		return rewriteHandler;
	}

	private ThreadPool createThreadPool() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(10);
		threadPool.setMaxThreads(localServer.getHttpNumThreads());
		threadPool.setIdleTimeout(localServer.getHttpThreadPoolMaxIdleTimeMillis());
		threadPool.setDetailedDump(false);

		JettyMonitor.setThreadPool(threadPool);
		return threadPool;
	}

	private HttpConfiguration createHttpConfig() {
		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setOutputBufferSize(localServer.getHttpOutputBufferSize());
		httpConfig.setRequestHeaderSize(localServer.getHttpRequestHeaderSize());
		httpConfig.setResponseHeaderSize(localServer.getHttpResponseHeaderSize());
		httpConfig.setSendServerVersion(false);
		httpConfig.setSendDateHeader(true);
		httpConfig.setHeaderCacheSize(localServer.getHttpHeaderCacheSize());
		httpConfig.setSecurePort(localServer.getMailSSLPort());

		final ForwardedRequestCustomizer forwardedRequestCustomizer = new ForwardedRequestCustomizer();
		forwardedRequestCustomizer.setForwardedForHeader("bogus");
		httpConfig.addCustomizer(forwardedRequestCustomizer);

		final String publicServiceHostname = config.getPublicServiceHostname();
		if (publicServiceHostname != null) {
			final HostHeaderCustomizer hostHeaderCustomizer = new HostHeaderCustomizer(
					config.getPublicServiceHostname());
			httpConfig.addCustomizer(hostHeaderCustomizer);
		}

		return httpConfig;
	}

	private HttpConfiguration createHttpsConfig(HttpConfiguration baseConfig) {
		HttpConfiguration sslHttpConfig = new HttpConfiguration(baseConfig);
		sslHttpConfig.addCustomizer(new SecureRequestCustomizer());
		return sslHttpConfig;
	}

	private SslContextFactory createSSLContextFactory() {
		SslContextFactory localSslContextFactory = new SslContextFactory.Server();
		localSslContextFactory.setKeyStorePath(LC.mailboxd_keystore.value());
		localSslContextFactory.setKeyStorePassword(LC.mailboxd_keystore_password.value());
		localSslContextFactory.setKeyManagerPassword(LC.mailboxd_keystore_password.value());
		localSslContextFactory.setRenegotiationAllowed(
				localServer.isMailboxdSSLRenegotiationAllowed());

		for (String protocol : localServer.getMailboxdSSLProtocols()) {
			localSslContextFactory.setIncludeProtocols(protocol);
		}

		localSslContextFactory.setExcludeCipherSuites(localServer.getSSLExcludeCipherSuites());

		final String[] sslIncludeCipherSuites = localServer.getSSLIncludeCipherSuites();
		if (sslIncludeCipherSuites.length > 0) {
			localSslContextFactory.setIncludeCipherSuites(sslIncludeCipherSuites);
		}

		return localSslContextFactory;
	}
}
