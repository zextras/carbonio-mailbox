/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.audit;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.tar.TarEntry;
import com.zimbra.common.util.tar.TarOutputStream;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTest;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.UserServlet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.GZIPOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportExportAuditLogIT extends MailboxTestSuite {

  private static final String SERVER_HOST = "127.0.0.1";

  private Server server;
  private Account testAccount;
  private InMemoryAppender auditAppender;

  @BeforeEach
  void setUp() throws Exception {
    server = new Server();
    final ServletHolder servletHolder = new ServletHolder(UserServlet.class);
    final ServletContextHandler servletContextHandler = new ServletContextHandler();
    servletContextHandler.addServlet(servletHolder, "/*");
    server.setHandler(servletContextHandler.getCoreContextHandler());
    final ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.setHost(SERVER_HOST);
    server.setConnectors(new ServerConnector[] {serverConnector});
    server.start();
    testAccount = createAccount().create();
    auditAppender = attachAppenderToMailboxLog();
  }

  @AfterEach
  void tearDown() throws Exception {
    detachAppenderFromMailboxLog();
    server.stop();
  }

  @Test
  void shouldLogSuccessfulFolderExport() throws Exception {
    addMessageToInbox("audit export test");

    final HttpResponse response = get("~/Inbox?fmt=tgz&auth=co");

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String line = auditAppender.findLineContaining("cmd=FolderExport;");
    assertTrue(line.contains("account=" + testAccount.getName() + ";"));
    assertTrue(line.contains("authAccount=" + testAccount.getName() + ";"));
    assertTrue(line.contains("folder=/Inbox;"));
    assertTrue(line.contains("fmt=tgz;"));
    assertTrue(line.contains("outcome=success;"));
    assertTrue(exportedSize(line) > 0);
  }

  @Test
  void shouldLogSuccessfulFolderImport() throws Exception {
    final HttpResponse response =
        post("~/Calendar?fmt=ics&auth=co", new FileEntity(icsTestFile()));

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String line = auditAppender.findLineContaining("cmd=FolderImport;");
    assertTrue(line.contains("account=" + testAccount.getName() + ";"));
    assertTrue(line.contains("folder=/Calendar;"));
    assertTrue(line.contains("fmt=ics;"));
    assertTrue(line.contains("outcome=success;"));
    assertTrue(exportedSize(line) > 0);
  }

  @Test
  void shouldLogSuccessfulIcsExportWrittenThroughWriter() throws Exception {
    final HttpResponse response = get("~/Calendar?fmt=ics&auth=co");

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String line = auditAppender.findLineContaining("cmd=FolderExport;");
    assertTrue(line.contains("folder=/Calendar;"));
    assertTrue(line.contains("fmt=ics;"));
    assertTrue(line.contains("outcome=success;"));
    assertTrue(exportedSize(line) > 0);
  }

  @Test
  void shouldLogPartialOutcomeWhenSomeImportEntriesFail() throws Exception {
    final HttpResponse response =
        post("~/Inbox?fmt=tgz&auth=co", new ByteArrayEntity(tgzWithBrokenEntry()));

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String line = auditAppender.findLineContaining("cmd=FolderImport;");
    assertTrue(line.contains("folder=/Inbox;"));
    assertTrue(line.contains("fmt=tgz;"));
    assertTrue(line.contains("outcome=partial;"));
  }

  @Test
  void shouldLogErrorOutcomeWhenImportFails() throws Exception {
    post("~/Calendar?fmt=ics&auth=co", new ByteArrayEntity("not an ics file".getBytes()));

    final String line = auditAppender.findLineContaining("cmd=FolderImport;");
    assertTrue(line.contains("folder=/Calendar;"));
    assertTrue(line.contains("outcome=error;"));
  }

  @Test
  void shouldNotAuditSingleItemDownload() throws Exception {
    final int messageId = addMessageToInbox("single message download");

    final HttpResponse response = get("~/?id=" + messageId + "&auth=co");

    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    assertTrue(auditAppender.linesContaining("cmd=FolderExport;").isEmpty());
  }

  private int addMessageToInbox(String subject) throws Exception {
    final var mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    return mailbox
        .addMessage(
            null,
            MailboxTestUtil.generateMessage(subject),
            MailboxTest.STANDARD_DELIVERY_OPTIONS,
            null)
        .getId();
  }

  private byte[] tgzWithBrokenEntry() throws Exception {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final GZIPOutputStream gzip = new GZIPOutputStream(bytes);
    try (TarOutputStream tar = new TarOutputStream(gzip, "UTF-8")) {
      final byte[] content = "broken".getBytes();
      final TarEntry entry = new TarEntry("broken.err");
      entry.setSize(content.length);
      tar.putNextEntry(entry);
      tar.write(content);
      tar.closeEntry();
    }
    return bytes.toByteArray();
  }

  private long exportedSize(String line) {
    final String sizeAttribute = line.substring(line.indexOf("size=") + "size=".length());
    return Long.parseLong(sizeAttribute.replace(";", "").trim());
  }

  private File icsTestFile() {
    return new File(
        Objects.requireNonNull(
                this.getClass().getResource("/com/zimbra/cs/service/UploadCalendar.ics"))
            .getFile());
  }

  private HttpResponse get(String path) throws Exception {
    try (CloseableHttpClient client = authenticatedClient()) {
      final HttpGet httpGet = new HttpGet();
      httpGet.setURI(URI.create(server.getURI().toString() + path));
      return client.execute(httpGet);
    }
  }

  private HttpResponse post(String path, org.apache.http.HttpEntity entity) throws Exception {
    try (CloseableHttpClient client = authenticatedClient()) {
      final HttpPost httpPost = new HttpPost();
      httpPost.setEntity(entity);
      httpPost.setURI(URI.create(server.getURI().toString() + path));
      return client.execute(httpPost);
    }
  }

  private CloseableHttpClient authenticatedClient() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    final CookieStore cookieStore = new BasicCookieStore();
    final BasicClientCookie cookie =
        new BasicClientCookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded());
    cookie.setDomain(SERVER_HOST);
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
  }

  private InMemoryAppender attachAppenderToMailboxLog() {
    final InMemoryAppender appender = new InMemoryAppender();
    appender.start();
    ZimbraLog.mailbox.setLevel(com.zimbra.common.util.Log.Level.info);
    ((Logger) LogManager.getLogger("zimbra.mailbox")).addAppender(appender);
    return appender;
  }

  private void detachAppenderFromMailboxLog() {
    ((Logger) LogManager.getLogger("zimbra.mailbox")).removeAppender(auditAppender);
  }

  private static class InMemoryAppender extends AbstractAppender {

    private final List<String> lines = new CopyOnWriteArrayList<>();

    InMemoryAppender() {
      super("import-export-audit-test", null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      lines.add(event.getMessage().getFormattedMessage());
    }

    String findLineContaining(String text) {
      final List<String> matches = linesContaining(text);
      assertEquals(1, matches.size(), "expected exactly one audit line containing: " + text);
      return matches.get(0);
    }

    List<String> linesContaining(String text) {
      return lines.stream().filter(line -> line.contains(text)).toList();
    }
  }
}
