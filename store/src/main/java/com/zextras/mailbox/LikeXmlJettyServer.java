// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zimbra.common.jetty.JettyMonitor;
import javax.servlet.DispatcherType;
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

  private static final int APP_USER_SERVER_PORT = 8080;
  private static final int APP_ADMIN_SERVER_PORT = 7071;
  private static final int MTA_APP_ADMIN_SERVER_PORT = 7073;
  private static final int EXTENSIONS_SERVER_PORT = 7072;
  private static final int SECURE_PORT = 8443;

  public static class Builder {

    private String webDescriptor;

    public Server build() throws Exception {
      ThreadPool threadPool = createThreadPool();
      Server server = new Server(threadPool);

      final ServerConnector userHttpConnector = createUserHttpConnector(server);
      server.addConnector(userHttpConnector);

      final ServerConnector adminHttpsConnector = createAdminHttpsConnector(server);
      server.addConnector(adminHttpsConnector);

      server.addConnector(createMtaAdminHttpsConnector(server));
      server.addConnector(createExtensionsHttpsConnector(server));

      WebAppContext webAppContext = new WebAppContext(new ContextHandlerCollection(), "/opt/zextras/jetty_base/webapps/service", "/service");
      webAppContext.setDescriptor(webDescriptor);


      final GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.setHandler(createRewriteHandler());
      gzipHandler.setMinGzipSize(2048);
      gzipHandler.setCheckGzExists(false);
      gzipHandler.setCompressionLevel(-1);
      gzipHandler.setExcludedAgentPatterns(".*MSIE.6\\.0.*");
      gzipHandler.setIncludedMethods("GET", "POST");
      server.setHandler(gzipHandler);

      server.setHandler(webAppContext);

      userHttpConnector.open();
      adminHttpsConnector.open();

      return server;
    }

    public Builder withWebDescriptor(String webDescriptor) {
      this.webDescriptor = webDescriptor;
      return this;
    }
  }

  private static Handler createRewriteHandler() {
    final RewriteHandler rewriteHandler = new RewriteHandler();
    rewriteHandler.setRewriteRequestURI(true);
    rewriteHandler.setRewritePathInfo(false);
    rewriteHandler.setOriginalPathAttribute("requestedPath");

    rewriteHandler.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD);
    rewriteHandler.addRule(new MsieSslRule());

    rewriteHandler.addRule(new RewritePatternRule("/Microsoft-Server-ActiveSync/*", "/service/extension/zimbrasync"));
    rewriteHandler.addRule(new RewriteRegexRule("(?i)/ews/Exchange.asmx/*", "/service/extension/zimbraews"));
    rewriteHandler.addRule(new RewritePatternRule("/principals/*", "/service/dav/principals"));
    rewriteHandler.addRule(new RewritePatternRule("/dav/*", "/service/dav/home"));
    rewriteHandler.addRule(new RewritePatternRule("/.well-known/*", "/service/.well-known"));
    rewriteHandler.addRule(new RewritePatternRule("/.well-known/*", "/service/.well-known"));
    rewriteHandler.addRule(new RewritePatternRule("/home/*", "/service/home/"));
    rewriteHandler.addRule(new RewritePatternRule("//home/*", "/service/home"));
    rewriteHandler.addRule(new RewritePatternRule("/user/*", "/service/user/"));
    rewriteHandler.addRule(new RewritePatternRule("//user/*", "/service/user"));
    rewriteHandler.addRule(new RewritePatternRule("/shf/*", "/service/shf/"));
    rewriteHandler.addRule(new RewritePatternRule("/certauth/*", "/service/certauth"));
    rewriteHandler.addRule(new RewritePatternRule("/spnegoauth/*", "/service/spnego"));
    rewriteHandler.addRule(new RewritePatternRule("//service/spnego/*", "/service/spnego"));
    rewriteHandler.addRule(new RewritePatternRule("//service/spnego/*", "/service/spnego"));
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

    final RewritePatternRule rootRule = new RewritePatternRule("//*", "/");
    rootRule.setTerminating(true);
    rewriteHandler.addRule(rootRule);

    final RewritePatternRule rootRule2 = new RewritePatternRule("/*", "/");
    rootRule2.setTerminating(true);
    rewriteHandler.addRule(rootRule2);

    rewriteHandler.setHandler(new HandlerCollection(new ContextHandlerCollection(), new DefaultHandler(), new RequestLogHandler()));

    return rewriteHandler;
  }

  private static ThreadPool createThreadPool() {
//    <Get name="ThreadPool" id="pool">
//		<Set name="minThreads" type="int">10</Set>
//		<Set name="maxThreads" type="int">250</Set>
//		<Set name="idleTimeout" type="int">10000</Set>
//		<Set name="detailedDump">false</Set>
//	</Get>
//
//	<Call class="com.zimbra.common.jetty.JettyMonitor" name="setThreadPool">
//		<Arg><Ref id="pool"/></Arg>
//	</Call>
    QueuedThreadPool threadPool = new QueuedThreadPool();
    threadPool.setMinThreads(10);
    threadPool.setMaxThreads(250);
    threadPool.setIdleTimeout(10000);
    threadPool.setDetailedDump(false);

    JettyMonitor.setThreadPool(threadPool);
    return threadPool;
  }

  private static HttpConfiguration basicHttpConfig() {
//    <Set name="outputBufferSize">32768</Set>
//		<Set name="requestHeaderSize">8192</Set>
//		<Set name="responseHeaderSize">8192</Set>
//		<Set name="sendServerVersion">false</Set>
//		<Set name="sendDateHeader">true</Set>
//		<Set name="headerCacheSize">512</Set>
//		<Set name="securePort">8443</Set>
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setOutputBufferSize(32768);
    httpConfig.setRequestHeaderSize(8192);
    httpConfig.setResponseHeaderSize(8192);
    httpConfig.setSendServerVersion(false);
    httpConfig.setSendDateHeader(true);
    httpConfig.setHeaderCacheSize(512);
    httpConfig.setSecurePort(SECURE_PORT);

    final ForwardedRequestCustomizer forwardedRequestCustomizer = new ForwardedRequestCustomizer();
    forwardedRequestCustomizer.setForwardedForHeader("bogus");
    httpConfig.addCustomizer(forwardedRequestCustomizer);

    final HostHeaderCustomizer hostHeaderCustomizer = new HostHeaderCustomizer("carbonio-ce-proxy.carbonio-system.svc.cluster.local");
    httpConfig.addCustomizer(hostHeaderCustomizer);

    return httpConfig;
  }

  private static HttpConfiguration createSSLHttpConfig() {
    HttpConfiguration sslHttpConfig = basicHttpConfig();
    sslHttpConfig.addCustomizer(new SecureRequestCustomizer());
    return sslHttpConfig;
  }

  private static ServerConnector createUserHttpConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(basicHttpConfig()));
    serverConnector.setPort(APP_USER_SERVER_PORT);
    serverConnector.setIdleTimeout(60000);
    return serverConnector;
  }

  private static SslContextFactory createSSLContextFactory() {
//    <Set name="KeyStorePath"><SystemProperty name="jetty.base" default="." />/etc/keystore</Set>
//		<Set name="KeyStorePassword">password</Set>
//		<Set name="KeyManagerPassword">password</Set>
//		<Set name="EndpointIdentificationAlgorithm"></Set>
//		<Set name="renegotiationAllowed">TRUE</Set>
//		<!-- SSLPROTOCOLSBEGIN -->
//		<Set name="IncludeProtocols">
//			<Array type="java.lang.String">
//				<Item>TLSv1.2</Item>
//			</Array>
//		</Set>
//		<!-- SSLPROTOCOLSEND -->
//		<Set name="ExcludeCipherSuites">
//			<Array type="java.lang.String">
//				<Item>.*_RC4_.*</Item>
//<Item>^.*_(MD5|SHA|SHA1)$</Item>
//<Item>^TLS_RSA_.*</Item>
//			</Array>
//		</Set>
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath("/opt/zextras/mailboxd/etc/keystore");
    sslContextFactory.setKeyStorePassword("password");
    sslContextFactory.setKeyManagerPassword("password");
    sslContextFactory.setEndpointIdentificationAlgorithm("password");
    sslContextFactory.setRenegotiationAllowed(true);
    sslContextFactory.setIncludeProtocols("TLSv1.2");
    sslContextFactory.setExcludeCipherSuites(".*_RC4_.*", "^.*_(MD5|SHA|SHA1)$", "^TLS_RSA_.*");
    return sslContextFactory;
  }

  private static ServerConnector createAdminHttpsConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createSSLHttpConfig()));
    final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSSLContextFactory(),  "http/1.1");
    serverConnector.addConnectionFactory(sslConnectionFactory);
    serverConnector.setPort(APP_ADMIN_SERVER_PORT);
    serverConnector.setIdleTimeout(0);
    return serverConnector;
  }

  private static ServerConnector createMtaAdminHttpsConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createSSLHttpConfig()));
    final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSSLContextFactory(),  "http/1.1");
    serverConnector.addConnectionFactory(sslConnectionFactory);
    serverConnector.setPort(MTA_APP_ADMIN_SERVER_PORT);
    serverConnector.setIdleTimeout(0);
    return serverConnector;
  }

  private static ServerConnector createExtensionsHttpsConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createSSLHttpConfig()));
    final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSSLContextFactory(),  "http/1.1");
    serverConnector.addConnectionFactory(sslConnectionFactory);
    serverConnector.setPort(EXTENSIONS_SERVER_PORT);
    serverConnector.setIdleTimeout(60000);
    return serverConnector;
  }

}
