// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.UserServlet;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ArchiveFormatterImportIT extends MailboxTestSuite {

  private static final String SERVER_HOST = "127.0.0.1";

  private Server server;
  private Account account;
  private Mailbox mailbox;

  @BeforeEach
  void setUp() throws Exception {
    server = new Server();
    final ServletContextHandler handler = new ServletContextHandler();
    handler.addServlet(new ServletHolder(UserServlet.class), "/*");
    server.setHandler(handler.getCoreContextHandler());
    final ServerConnector connector = new ServerConnector(server);
    connector.setHost(SERVER_HOST);
    server.setConnectors(new ServerConnector[] {connector});
    server.start();
    account = createAccount().create();
    mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
  }

  @AfterEach
  void tearDown() throws Exception {
    server.stop();
  }

  @ParameterizedTest(name = "fmt={0}, meta={1}")
  @MethodSource("archiveFormats")
  void importIntoExportedFolderDoesNotCreateDuplicateSubfolder(String format, boolean meta) throws Exception {
    addMessages("/Inbox", 2);
    final Map<String, Integer> before = messagesPerFolder();
    final byte[] archive = export("/Inbox", format, meta);
    emptyFolder("/Inbox");

    importArchive("Inbox", format, archive);

    assertEquals(before, messagesPerFolder());
  }

  @ParameterizedTest(name = "fmt={0}, meta={1}")
  @MethodSource("archiveFormats")
  void importIntoExportedNestedFolderRestoresHierarchyInPlace(String format, boolean meta) throws Exception {
    createFolder("/Inbox/Work");
    createFolder("/Inbox/Work/Sub");
    addMessages("/Inbox/Work", 2);
    addMessages("/Inbox/Work/Sub", 1);
    final Map<String, Integer> before = messagesPerFolder();
    final byte[] archive = export("/Inbox/Work", format, meta);
    emptyFolder("/Inbox/Work");

    importArchive("Inbox/Work", format, archive);

    assertEquals(before, messagesPerFolder());
  }

  @ParameterizedTest(name = "fmt={0}, meta={1}")
  @MethodSource("archiveFormats")
  void importIntoAncestorOfExportedFolderRestoresOriginalLocation(String format, boolean meta) throws Exception {
    createFolder("/Inbox/Work");
    addMessages("/Inbox/Work", 2);
    final Map<String, Integer> before = messagesPerFolder();
    final byte[] archive = export("/Inbox/Work", format, meta);
    emptyFolder("/Inbox/Work");

    importArchive("Inbox", format, archive);

    assertEquals(before, messagesPerFolder());
  }

  @ParameterizedTest(name = "fmt={0}, meta={1}")
  @MethodSource("archiveFormats")
  void importIntoMailboxRootRestoresOriginalLocation(String format, boolean meta) throws Exception {
    addMessages("/Inbox", 2);
    final Map<String, Integer> before = messagesPerFolder();
    final byte[] archive = export("/Inbox", format, meta);
    emptyFolder("/Inbox");

    importArchive("", format, archive);

    assertEquals(before, messagesPerFolder());
  }

  @ParameterizedTest(name = "fmt={0}, meta={1}")
  @MethodSource("archiveFormats")
  void importIntoUnrelatedFolderKeepsExportedFolderAsSubfolder(String format, boolean meta) throws Exception {
    createFolder("/Restore");
    addMessages("/Inbox", 2);
    final byte[] archive = export("/Inbox", format, meta);

    importArchive("Restore", format, archive);

    final Map<String, Integer> after = messagesPerFolder();
    assertEquals(0, after.get("/Restore"));
    assertEquals(2, after.get("/Restore/Inbox"));
    assertEquals(2, after.get("/Inbox"));
  }


  static Stream<Arguments> archiveFormats() {
    return Stream.of(
        Arguments.of("tgz", true),
        Arguments.of("tgz", false),
        Arguments.of("zip", true),
        Arguments.of("zip", false));
  }

  private void createFolder(String path) throws Exception {
    mailbox.createFolder(null, path, new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  }

  private void addMessages(String folderPath, int count) throws Exception {
    final int folderId = mailbox.getFolderByPath(null, folderPath).getId();
    for (int i = 0; i < count; i++) {
      mailbox.addMessage(
          null,
          MailboxTestUtil.generateMessage(folderPath + " message " + i),
          new DeliveryOptions().setFolderId(folderId),
          null);
    }
  }

  private void emptyFolder(String path) throws Exception {
    mailbox.emptyFolder(null, mailbox.getFolderByPath(null, path).getId(), true);
  }

  private Map<String, Integer> messagesPerFolder() throws Exception {
    final Map<String, Integer> result = new TreeMap<>();
    for (Folder folder : mailbox.getFolderById(null, Mailbox.ID_FOLDER_USER_ROOT).getSubfolderHierarchy()) {
      result.put(
          folder.getPath(),
          mailbox.listItemIds(null, MailItem.Type.MESSAGE, folder.getId()).size());
    }
    return result;
  }

  private byte[] export(String folderPath, String format, boolean meta) throws Exception {
    final int folderId = mailbox.getFolderByPath(null, folderPath).getId();
    final HttpGet get = new HttpGet("/");
    get.setUri(
        uri(
            "",
            "fmt=" + format + "&meta=" + (meta ? "1" : "0")
                + "&types=message,conversation&id=" + folderId));
    try (CloseableHttpClient client = authenticatedClient()) {
      return client.execute(
          get,
          response -> {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            return EntityUtils.toByteArray(response.getEntity());
          });
    }
  }

  private void importArchive(String folder, String format, byte[] archive) throws Exception {
    final HttpPost post = new HttpPost("/");
    post.setUri(uri(folder, "fmt=" + format));
    post.setEntity(new ByteArrayEntity(archive, null));
    try (CloseableHttpClient client = authenticatedClient()) {
      client.execute(
          post,
          response -> {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            return null;
          });
    }
  }

  private URI uri(String folder, String query) {
    return URI.create(server.getURI().toString() + "~/" + folder + "?auth=co&" + query);
  }

  private CloseableHttpClient authenticatedClient() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(account);
    final BasicCookieStore cookieStore = new BasicCookieStore();
    final BasicClientCookie cookie =
        new BasicClientCookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded());
    cookie.setDomain(SERVER_HOST);
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
  }
}
