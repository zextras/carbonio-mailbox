// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

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
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

// See: https://github.com/jetty/jetty.project/blob/jetty-9.4.x/examples/embedded/src/main/java/org/eclipse/jetty/embedded/LikeJettyXml.java
// Plus we adopted the same variables used in https://github.com/zextras/carbonio-appserver/blob/main/appserver/conf/jetty/jetty.xml.production
public class LikeXmlJettyServer {

  public static class InstantiationException extends Exception {

    private static final long serialVersionUID = 3614086690444919273L;

    public InstantiationException(Throwable cause) {
      super("Failed to create Jetty server", cause);
    }
  }

  private static final int USER_SERVER_PORT = 8080;
  private static final int ADMIN_SERVER_PORT = 7071;
  private static final int ADMIN_MTA_SERVER_PORT = 7073;
  private static final int EXTENSIONS_SERVER_PORT = 7072;
  private static final int SECURE_PORT = 8443;

  private LikeXmlJettyServer() {
  }

  public static class Builder {

    private final Config globalConfig;
    private HttpConfiguration httpsConfig;
    private SslContextFactory sslContextFactory;
    private String webDescriptor = "/opt/zextras/conf/web.xml";
    private String webApp = "/opt/zextras/conf";

    public Builder(Config globalConfig) {
      this.globalConfig = globalConfig;
    }

    public Server build() throws InstantiationException {
      try {
        final ThreadPool threadPool = createThreadPool();
      Server server = new Server(threadPool);

      final HttpConfiguration httpConfig = createHttpConfig();
      this.httpsConfig = createHttpsConfig(httpConfig);
      this.sslContextFactory = createSSLContextFactory();

      final ServerConnector userHttpConnector = createUserHttpConnector(server, httpConfig);
      server.addConnector(userHttpConnector);

      final ServerConnector adminHttpsConnector = createAdminHttpsConnector(server);
      server.addConnector(adminHttpsConnector);

      server.addConnector(createMtaAdminHttpsConnector(server));
      server.addConnector(createExtensionsHttpsConnector(server));

      final ContextHandlerCollection contexts = new ContextHandlerCollection();
        WebAppContext webAppContext = new WebAppContext(contexts, webApp, "/service");
      webAppContext.setDescriptor(webDescriptor);
       webAppContext.setThrowUnavailableOnStartupException(true);


      final GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.setHandler(createRewriteHandler());
      gzipHandler.setMinGzipSize(2048);
      gzipHandler.setCompressionLevel(-1);
      gzipHandler.setExcludedAgentPatterns(".*MSIE.6\\.0.*");
      gzipHandler.setIncludedMethods("GET", "POST");
      server.setHandler(gzipHandler);

      server.setHandler(webAppContext);

      userHttpConnector.open();
      adminHttpsConnector.open();

      server.setStopAtShutdown(true);
      server.setDumpAfterStart(true);
      server.setDumpBeforeStop(true);

      return server;
      } catch (IOException e) {
        throw new InstantiationException(e.getCause());
      }
    }

    public Builder withWebApp(String webApp) {
      this.webApp = webApp;
      return this;
    }

    public Builder withWebDescriptor(String webDescriptor) {
      this.webDescriptor = webDescriptor;
      return this;
    }


    private Handler createRewriteHandler() {
      final RewriteHandler rewriteHandler = new RewriteHandler();
      rewriteHandler.setRewriteRequestURI(true);
      rewriteHandler.setRewritePathInfo(false);
      rewriteHandler.setOriginalPathAttribute("requestedPath");

      rewriteHandler.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD);
      rewriteHandler.addRule(new MsieSslRule());

      final String mailURL = globalConfig.getMailURL();

      rewriteHandler.addRule(new RewritePatternRule("/Microsoft-Server-ActiveSync/*", "/service/extension/zimbrasync"));
      rewriteHandler.addRule(new RewriteRegexRule("(?i)/ews/Exchange.asmx/*", "/service/extension/zimbraews"));
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
      rewriteHandler.addRule(new RewritePatternRule("/spnegoauth/*", "/service/spnego"));
      rewriteHandler.addRule(new RewritePatternRule( "/spnego/*", "/spnego"));
      rewriteHandler.addRule(new RewritePatternRule(mailURL + "/service/spnego/*", "/service/spnego"));
      rewriteHandler.addRule(new RewritePatternRule("/autodiscover/*", "/service/extension/autodiscover"));
      rewriteHandler.addRule(new RewritePatternRule("/Autodiscover/*", "/service/extension/autodiscover"));

      final RewritePatternRule serviceRule = new RewritePatternRule("/service/*", "/service");
      serviceRule.setTerminating(true);
      rewriteHandler.addRule(serviceRule);

      final RewritePatternRule spnegoRule = new RewritePatternRule("/spnego/*", "/spnego");
      spnegoRule.setTerminating(true);
      rewriteHandler.addRule(spnegoRule);

      final RewritePatternRule carbonioAdminRule = new RewritePatternRule("/carbonioAdmin/*", "/carbonioAdmin");
      carbonioAdminRule.setTerminating(true);
      rewriteHandler.addRule(carbonioAdminRule);

      final RewritePatternRule rootRule = new RewritePatternRule( mailURL + "/*", "/");
      rootRule.setTerminating(true);
      rewriteHandler.addRule(rootRule);

      final RewritePatternRule rootRule2 = new RewritePatternRule("/*", mailURL);
      rootRule2.setTerminating(true);
      rewriteHandler.addRule(rootRule2);

      rewriteHandler.setHandler(new HandlerCollection(new ContextHandlerCollection(), new DefaultHandler(), new RequestLogHandler()));

      return rewriteHandler;
    }

    private ThreadPool createThreadPool() {
      QueuedThreadPool threadPool = new QueuedThreadPool();
      threadPool.setMinThreads(10);
      threadPool.setMaxThreads(globalConfig.getHttpNumThreads());
      threadPool.setIdleTimeout(globalConfig.getHttpThreadPoolMaxIdleTimeMillis());
      threadPool.setDetailedDump(false);

      JettyMonitor.setThreadPool(threadPool);
      return threadPool;
    }

    private HttpConfiguration createHttpConfig() {
      HttpConfiguration httpConfig = new HttpConfiguration();
      httpConfig.setOutputBufferSize(globalConfig.getHttpOutputBufferSize());
      httpConfig.setRequestHeaderSize(globalConfig.getHttpRequestHeaderSize());
      httpConfig.setResponseHeaderSize(globalConfig.getHttpResponseHeaderSize());
      httpConfig.setSendServerVersion(false);
      httpConfig.setSendDateHeader(true);
      httpConfig.setHeaderCacheSize(globalConfig.getHttpHeaderCacheSize());
      httpConfig.setSecurePort(SECURE_PORT);

      final ForwardedRequestCustomizer forwardedRequestCustomizer = new ForwardedRequestCustomizer();
      forwardedRequestCustomizer.setForwardedForHeader("bogus");
      httpConfig.addCustomizer(forwardedRequestCustomizer);

      final String publicServiceHostname = globalConfig.getPublicServiceHostname();
      if (publicServiceHostname != null) {
        final HostHeaderCustomizer hostHeaderCustomizer = new HostHeaderCustomizer(globalConfig.getPublicServiceHostname());
        httpConfig.addCustomizer(hostHeaderCustomizer);
      }

      return httpConfig;
    }

    private HttpConfiguration createHttpsConfig(HttpConfiguration baseConfig) {
      HttpConfiguration sslHttpConfig = new HttpConfiguration(baseConfig);
      sslHttpConfig.addCustomizer(new SecureRequestCustomizer());
      return sslHttpConfig;
    }

    private ServerConnector createUserHttpConnector(Server server, HttpConfiguration httpConfig) {
      ServerConnector serverConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
      // Use zimbraMailPort or set static?
      serverConnector.setPort(USER_SERVER_PORT);
      serverConnector.setIdleTimeout(globalConfig.getHttpConnectorMaxIdleTimeMillis());
      return serverConnector;
    }

    private SslContextFactory createSSLContextFactory() {
      SslContextFactory localSslContextFactory = new SslContextFactory.Server();
      localSslContextFactory.setKeyStorePath(LC.mailboxd_keystore.value());
      localSslContextFactory.setKeyStorePassword(LC.mailboxd_keystore_password.value());
      localSslContextFactory.setKeyManagerPassword(LC.mailboxd_keystore_password.value());
      localSslContextFactory.setRenegotiationAllowed(globalConfig.isMailboxdSSLRenegotiationAllowed());

      for (String protocol : globalConfig.getMailboxdSSLProtocols()) {
        localSslContextFactory.setIncludeProtocols(protocol);
      }

      localSslContextFactory.setExcludeCipherSuites(globalConfig.getSSLExcludeCipherSuites());

      final String[] sslIncludeCipherSuites = globalConfig.getSSLIncludeCipherSuites();
      if (sslIncludeCipherSuites.length > 0) {
        localSslContextFactory.setIncludeCipherSuites(sslIncludeCipherSuites);
      }

      return localSslContextFactory;
    }

    private ServerConnector createHttpsConnector(Server server, int port, int idleTimeMillis) {
      ServerConnector serverConnector = createHttpsConnector(server);
      serverConnector.setPort(port);
      serverConnector.setIdleTimeout(idleTimeMillis);
      return serverConnector;
    }

    private ServerConnector createAdminHttpsConnector(Server server) {
      return createHttpsConnector(server, ADMIN_SERVER_PORT, 0);
    }

    private ServerConnector createMtaAdminHttpsConnector(Server server) {
      return createHttpsConnector(server, ADMIN_MTA_SERVER_PORT, 0);
    }

    private ServerConnector createExtensionsHttpsConnector(Server server) {
      return createHttpsConnector(server, EXTENSIONS_SERVER_PORT, globalConfig.getHttpConnectorMaxIdleTimeMillis());
    }

    private ServerConnector createHttpsConnector(Server server) {
      return new ServerConnector(server,
          new SslConnectionFactory(this.sslContextFactory, HttpVersion.HTTP_1_1.asString()),
          new HttpConnectionFactory(this.httpsConfig));
    }
  }


}
