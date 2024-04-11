// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zimbra.common.jetty.JettyMonitor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HostHeaderCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class Mailbox {

  private static final int APP_USER_SERVER_PORT = 8080;
  private static final int APP_ADMIN_SERVER_PORT = 7071;
  private static final int MTA_APP_ADMIN_SERVER_PORT = 7073;
  private static final int EXTENSIONS_SERVER_PORT = 7072;

  private static final String WEB_DESCRIPTOR = "webDescriptor";
  private static final String LOCALCONFIG = "localconfig";

  public static void main(String[] args) throws Exception {
    System.setProperty("zimbra.native.required", "false");
    Options options = getOptions();
    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = parser.parse(options, args);

    if (commandLine.hasOption(LOCALCONFIG)) {
      System.setProperty("zimbra.config", commandLine.getOptionValue(LOCALCONFIG));
    }

    String webDescriptor = "store/conf/web-dev.xml";
    if (commandLine.hasOption(WEB_DESCRIPTOR)) {
      webDescriptor = commandLine.getOptionValue(WEB_DESCRIPTOR);
    }

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setDescriptor(webDescriptor);
    webAppContext.setResourceBase("/");
    webAppContext.setContextPath("/service");

    Server server = likeJettyXmlServer();

    server.setHandler(webAppContext);
    server.setStopAtShutdown(true);
    server.setDumpAfterStart(true);
    server.setDumpBeforeStop(true);
    server.start();
    server.join();
  }

  private static Options getOptions() {
    Option webDescriptor =  new Option(Mailbox.WEB_DESCRIPTOR,true, "Location to web descriptor");
    webDescriptor.setRequired(false);
    Options options = new Options();
    options.addOption(webDescriptor);

    Option localconfig =  new Option(LOCALCONFIG,true, "Location to localconfig");
    localconfig.setRequired(false);
    options.addOption(localconfig);

    return options;
  }

  // See: https://github.com/jetty/jetty.project/blob/jetty-9.4.x/examples/embedded/src/main/java/org/eclipse/jetty/embedded/LikeJettyXml.java
  private static Server likeJettyXmlServer() {
    HttpConfiguration httpConfig = createHttpConfig();
    ThreadPool threadPool = createThreadPool();

    Server server = new Server(threadPool);
    addUserHttpConnector(server);
    addAdminHttpsConnector(server);
    addMtaAdminHttpsConnector(server);
    addMtaAdminHttpsConnector(server);
    addExtensionsHttpsConnector(server);

    return server;
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
    threadPool.setMaxThreads(250);
    threadPool.setMinThreads(10);
    threadPool.setDetailedDump(false);
    threadPool.setDetailedDump(false);
    JettyMonitor.setThreadPool(threadPool);
    return threadPool;
  }

  private static HttpConfiguration createHttpConfig() {
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
    httpConfig.setSecurePort(8443);

    final ForwardedRequestCustomizer forwardedRequestCustomizer = new ForwardedRequestCustomizer();
    forwardedRequestCustomizer.setForwardedForHeader("bogus");
    httpConfig.addCustomizer(forwardedRequestCustomizer);

    //TODO: add server name (hostname of VM)
    final HostHeaderCustomizer hostHeaderCustomizer = new HostHeaderCustomizer("");
    httpConfig.addCustomizer(hostHeaderCustomizer);

    return httpConfig;
  }

  private static HttpConfiguration createSSLHttpConfig() {
    HttpConfiguration sslHttpConfig = createHttpConfig();
    sslHttpConfig.addCustomizer(new SecureRequestCustomizer());
    return sslHttpConfig;
  }

  private static ServerConnector addUserHttpConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createHttpConfig()));
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
    sslContextFactory.setKeyStorePath(System.getProperty("jetty.base", ".") + "/etc/keystore");
    sslContextFactory.setKeyStorePassword("password");
    sslContextFactory.setKeyManagerPassword("password");
    sslContextFactory.setEndpointIdentificationAlgorithm("password");
    sslContextFactory.setRenegotiationAllowed(true);
    sslContextFactory.setIncludeProtocols("TLSv1.2");
    sslContextFactory.setExcludeCipherSuites(".*_RC4_.*", "^.*_(MD5|SHA|SHA1)$", "^TLS_RSA_.*");
    return sslContextFactory;
  }

  private static ServerConnector addAdminHttpsConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createSSLHttpConfig()));
    final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSSLContextFactory(),  "http/1.1");
    serverConnector.addConnectionFactory(sslConnectionFactory);
    serverConnector.setPort(APP_ADMIN_SERVER_PORT);
    serverConnector.setIdleTimeout(0);
    return serverConnector;
  }

  private static ServerConnector addMtaAdminHttpsConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createSSLHttpConfig()));
    final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSSLContextFactory(),  "http/1.1");
    serverConnector.addConnectionFactory(sslConnectionFactory);
    serverConnector.setPort(MTA_APP_ADMIN_SERVER_PORT);
    serverConnector.setIdleTimeout(0);
    return serverConnector;
  }

  private static ServerConnector addExtensionsHttpsConnector(Server server) {
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.addConnectionFactory(new HttpConnectionFactory(createSSLHttpConfig()));
    final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSSLContextFactory(),  "http/1.1");
    serverConnector.addConnectionFactory(sslConnectionFactory);
    serverConnector.setPort(EXTENSIONS_SERVER_PORT);
    serverConnector.setIdleTimeout(60000);
    return serverConnector;
  }



}
