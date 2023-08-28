// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.util;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.soap.DocumentService;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class to encapsulate logic for testing Mailbox SOAP Engine.
 * Usage: extend and use {@link
 * #startServer(String, List)} {@link #stopServer()} to start and stop Mailbox Soap Servlet. Also
 * uses {@link MailboxTestUtil#initServer()} to start the environment. Unfortunately this last step
 * is needed, but can be removed if {@link com.zimbra.cs.servlet.ZimbraServlet} will use a provided
 * {@link Provisioning} instead of static usage.
 */
public abstract class MailboxSoapTest {

  private MailboxSOAPServer mailboxSOAPServer;
  private String basePath = "";

  /**
   * Starts Mailbox SOAP engine
   *
   * @param basePath base endpoint of SOAP server
   * @param handlers handlers to add in server. These will process requests.
   * @throws Exception
   */
  protected void startServer(String basePath, List<DocumentService> handlers) throws Exception {
    this.basePath = basePath;
    MailboxTestUtil.initServer();
    mailboxSOAPServer = new MailboxSOAPServer(basePath, 9090, handlers);
    mailboxSOAPServer.start();
  }

  /**
   * Stops the server
   *
   * @throws Exception
   */
  protected void stopServer() throws Exception {
    mailboxSOAPServer.stop();
  }

  /**
   * Wrapper around {@link SoapServlet} that register handlers without reflection and spinning up
   * the whole system, managing only the SOAP engine of Mailbox.
   */
  public class TestSoapServlet extends SoapServlet {

    private final List<DocumentService> handlers;

    public TestSoapServlet(List<DocumentService> handlers) {
      super();
      this.handlers = handlers;
    }

    @Override
    public void init() throws ServletException {
      this.mEngine = new SoapEngine(null);
      handlers.forEach(handler -> Try.run(() -> addService(handler)));
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
        throws ServletException, IOException {
      super.service(req, res);
    }
  }

  /** Mailbox SOAP Server that uses embedded Jetty to serve a SOAP endpoint. */
  public class MailboxSOAPServer {

    private Server server;
    private final String basePath;
    private final int port;
    private final List<DocumentService> handlers;

    public MailboxSOAPServer(String basePath, int port, List<DocumentService> handlers) {
      this.basePath = basePath;
      this.port = port;
      this.handlers = handlers;
    }

    /**
     * Starts Jetty Server with SOAP Engine and handlers
     * @throws Exception
     */
    public void start() throws Exception {
      server = new Server();
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath(basePath);

      final ServletHolder servletHolder =
          new ServletHolder(new TestSoapServlet(handlers));

      context.addServlet(servletHolder, basePath + "/*");

      server.setHandler(context);
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(port);
      server.setConnectors(new Connector[] {connector});
      server.start();
    }

    public void stop() throws Exception {
      server.stop();
    }
  }

  private String getTargetEndpoint() {
    return "http://localhost:" + mailboxSOAPServer.port + basePath;
  }

  /**
   * Execute SOAP call against target SOAP endpoint.
   *
   * @param authToken token to authenticate
   * @param element body of request
   * @return
   * @throws Exception
   */
  protected HttpResponse executeSoapPost(AuthToken authToken, Element element) throws Exception {
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    try (CloseableHttpClient httpClient =
        HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {
      HttpPost request = new HttpPost(getTargetEndpoint());
      request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_XML.getMimeType());
      request.setEntity(new StringEntity(SoapProtocol.Soap12.soapEnvelope(element).toString()));
      return httpClient.execute(request);
    }
  }
}
