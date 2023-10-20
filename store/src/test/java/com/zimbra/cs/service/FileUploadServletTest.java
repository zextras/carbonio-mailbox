// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.HeaderUtils.ByteBuilder;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.account.Auth;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.servlet.CsrfFilter;
import com.zimbra.soap.SoapServlet;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FileUploadServletTest {

  public static final String boundary = "----WebKitFormBoundaryBf0g3B57jaNA7SC6";
  public static final String filename1 = "\u6771\u65e5\u672c\u5927\u9707\u707d.txt";
  public static final String filename2 =
      "\u6771\u5317\u5730\u65b9\u592a\u5e73\u6d0b\u6c96\u5730\u9707.txt";
  public static final String content1 =
      "3 \u6708 11 \u65e5\u5348\u5f8c 2 \u6642 46"
          + " \u5206\u3054\u308d\u3001\u30de\u30b0\u30cb\u30c1\u30e5\u30fc\u30c9 9.0";
  private static final String content2 =
      "\u884c\u65b9\u4e0d\u660e\u8005\u76f8\u8ac7\u30c0\u30a4\u30e4\u30eb: \u5ca9\u624b\u770c:"
          + " 0120-801-471";
  private static FileUploadServlet servlet;
  private static Account testAccount;

  private Server server;

  @BeforeAll
  public static void init() throws Exception {

    LC.zimbra_tmp_directory.setDefault("build/test");

    servlet = new FileUploadServlet();

    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createAccount("test@zimbra.com", "secret", attrs);
    testAccount = prov.get(Key.AccountBy.name, "test@zimbra.com");

    attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("test2@zimbra.com", "secret", attrs);
  }

  public static ByteBuilder addFormField(ByteBuilder bb, String name, String value) {
    bb.append("--").append(boundary).append("\r\n");
    bb.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n");
    bb.append("\r\n");
    bb.append(value == null ? "" : value).append("\r\n");
    return bb;
  }

  public static ByteBuilder addFormFile(
      ByteBuilder bb, String filename, String ctype, String contents) {
    bb.append("--").append(boundary).append("\r\n");
    bb.append("Content-Disposition: form-data; name=\"_attFile_\"; filename=\"")
        .append(filename == null ? "" : filename)
        .append("\"\r\n");
    bb.append("Content-Type: ")
        .append(ctype == null ? "application/octet-stream" : ctype)
        .append("\r\n");
    bb.append("\r\n");
    bb.append(contents == null ? "" : contents).append("\r\n");
    return bb;
  }

  public static ByteBuilder endForm(ByteBuilder bb) {
    return bb.append("--").append(boundary).append("--\r\n");
  }

  @BeforeEach
  public void setUp() throws Exception {
    final InetSocketAddress address = new InetSocketAddress("localhost", 8080);
    server = new Server(address);
    final ServletHolder servletHolder = new ServletHolder(FileUploadServlet.class);
    final ServletContextHandler servletContextHandler = new ServletContextHandler();
    servletContextHandler.addServlet(servletHolder, "/*");
    server.setHandler(servletContextHandler);
    server.start();

    Provisioning prov = Provisioning.getInstance();
    testAccount =
        prov.createAccount(
            "test@test.com",
            "password",
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraAccountStatus, "active");
                put(ZAttrProvisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
    server.stop();
  }

  private List<Upload> uploadForm(byte[] form) throws Exception {
    URL url = new URL("http://localhost:7070/service/upload?fmt=extended");
    MockHttpServletRequest req =
        new MockHttpServletRequest(form, url, "multipart/form-data; boundary=" + boundary);
    HashMap<String, String> headersMap = new HashMap<String, String>();
    headersMap.put("Content-length", Integer.toString(form.length));
    req.headers = headersMap;
    MockHttpServletResponse resp = new MockHttpServletResponse();
    return servlet.handleMultipartUpload(req, resp, "extended", testAccount, false, null, true);
  }

  private void compareUploads(Upload up, String expectedFilename, byte[] expectedContent)
      throws Exception {
    assertEquals(expectedFilename, up.getName());
    assertArrayEquals(expectedContent, ByteUtil.getContent(up.getInputStream(), -1));
  }

  @Test
  void testFilenames() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1);
    addFormFile(bb, filename1, "text/plain", content1);
    addFormField(bb, "filename2", filename2);
    addFormFile(bb, filename2, "text/plain", content2);
    addFormField(bb, "filename3", "");
    addFormFile(bb, "", null, null);
    endForm(bb);

    List<Upload> uploads = uploadForm(bb.toByteArray());
    assertEquals(2, uploads == null ? 0 : uploads.size());
    compareUploads(uploads.get(0), filename1, content1.getBytes(CharsetUtil.UTF_8));
    compareUploads(uploads.get(1), filename2, content2.getBytes(CharsetUtil.UTF_8));
  }

  @Test
  void testConsecutiveFilenames() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1 + "\r\n" + filename2);
    addFormFile(bb, filename1, "text/plain", content1);
    addFormFile(bb, filename2, "text/plain", content2);
    addFormField(bb, "filename2", "");
    addFormFile(bb, "", null, null);
    addFormField(bb, "filename3", "");
    addFormFile(bb, "", null, null);
    endForm(bb);

    List<Upload> uploads = uploadForm(bb.toByteArray());
    assertEquals(2, uploads == null ? 0 : uploads.size());
    compareUploads(uploads.get(0), filename1, content1.getBytes(CharsetUtil.UTF_8));
    compareUploads(uploads.get(1), filename2, content2.getBytes(CharsetUtil.UTF_8));
  }

  @Test
  void testExtraFilenames() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1 + "\r\nextra\r\ndata.txt");
    addFormFile(bb, filename1, "text/plain", content1);
    addFormField(bb, "filename2", filename2);
    addFormFile(bb, filename2, "text/plain", content2);
    addFormField(bb, "filename3", "bar.gif");
    addFormFile(bb, "", null, null);
    endForm(bb);

    List<Upload> uploads = uploadForm(bb.toByteArray());
    assertEquals(2, uploads == null ? 0 : uploads.size());
    compareUploads(uploads.get(0), filename1, content1.getBytes(CharsetUtil.UTF_8));
    compareUploads(uploads.get(1), filename2, content2.getBytes(CharsetUtil.UTF_8));
  }

  @Test
  void testMissingFilenames() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", "");
    addFormFile(bb, "x", "text/plain", content1);
    addFormFile(bb, "y", "text/plain", content2);
    addFormField(bb, "filename2", "");
    addFormFile(bb, "", null, null);
    addFormField(bb, "filename3", "");
    addFormFile(bb, "", null, null);
    endForm(bb);

    List<Upload> uploads = uploadForm(bb.toByteArray());
    assertEquals(2, uploads == null ? 0 : uploads.size());
    compareUploads(uploads.get(0), "x", content1.getBytes(CharsetUtil.UTF_8));
    compareUploads(uploads.get(1), "y", content2.getBytes(CharsetUtil.UTF_8));
  }

  @Test
  void testFileUploadAuthTokenNotCsrfEnabled() throws Exception {
    URL url = new URL("http://localhost:7070/service/upload?lbfums=");
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1);
    addFormFile(bb, filename1, "text/plain", content1);

    endForm(bb);

    byte[] form = bb.toByteArray();
    HashMap<String, String> headers = new HashMap<String, String>();
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
    XMLElement req = new XMLElement(AccountConstants.AUTH_REQUEST);
    com.zimbra.common.soap.Element a = req.addUniqueElement(AccountConstants.E_ACCOUNT);
    a.addAttribute(AccountConstants.A_BY, "name");
    a.setText(acct.getName());
    req.addUniqueElement(AccountConstants.E_PASSWORD).setText("secret");
    Element response = new Auth().handle(req, ServiceTestUtil.getRequestContext(acct));
    String authToken = response.getElement(AccountConstants.E_AUTH_TOKEN).getText();

    MockHttpServletRequest mockreq =
        new MockHttpServletRequest(
            form, url, "multipart/form-data; boundary=" + boundary, 7070, "test", headers);
    mockreq.setAttribute(CsrfFilter.CSRF_TOKEN_CHECK, Boolean.FALSE);

    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", authToken);
    mockreq.setCookies(cookie);

    MockHttpServletResponse resp = new MockHttpServletResponse();
    servlet.doPost(mockreq, resp);
    String respStrg = resp.output.toString();
    System.out.println(respStrg);
    assertTrue(respStrg.contains("200"));
  }

  @Test
  void testFileUploadAuthTokenCsrfEnabled() throws Exception {
    URL url = new URL("http://localhost:7070/service/upload?lbfums=");
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1);
    addFormFile(bb, filename1, "text/plain", content1);

    endForm(bb);

    byte[] form = bb.toByteArray();
    HashMap<String, String> headers = new HashMap<String, String>();
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

    XMLElement req = new XMLElement(AccountConstants.AUTH_REQUEST);
    req.addAttribute(AccountConstants.A_CSRF_SUPPORT, "1");
    com.zimbra.common.soap.Element a = req.addUniqueElement(AccountConstants.E_ACCOUNT);
    a.addAttribute(AccountConstants.A_BY, "name");
    a.setText(acct.getName());
    req.addUniqueElement(AccountConstants.E_PASSWORD).setText("secret");
    Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
    MockHttpServletRequest authReq =
        (MockHttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
    authReq.setAttribute(Provisioning.A_zimbraCsrfTokenCheckEnabled, Boolean.TRUE);
    Random nonceGen = new Random();
    authReq.setAttribute(CsrfFilter.CSRF_SALT, nonceGen.nextInt() + 1);
    Element response = new Auth().handle(req, context);
    String authToken = response.getElement(AccountConstants.E_AUTH_TOKEN).getText();
    String csrfToken = response.getElement("csrfToken").getText();
    headers.put(Constants.CSRF_TOKEN, csrfToken);

    MockHttpServletRequest mockreq =
        new MockHttpServletRequest(
            form, url, "multipart/form-data; boundary=" + boundary, 7070, "test", headers);
    mockreq.setAttribute(CsrfFilter.CSRF_TOKEN_CHECK, Boolean.TRUE);

    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", authToken);
    mockreq.setCookies(cookie);

    MockHttpServletResponse resp = new MockHttpServletResponse();

    servlet.doPost(mockreq, resp);
    String respStrg = resp.output.toString();
    System.out.println(respStrg);
    assertTrue(respStrg.contains("200"));
  }

  @Test
  void testFileUploadAuthTokenCsrfEnabledButNoCsrfToken() throws Exception {
    URL url = new URL("http://localhost:7070/service/upload?lbfums=");
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1);
    addFormFile(bb, filename1, "text/plain", content1);

    endForm(bb);

    byte[] form = bb.toByteArray();
    HashMap<String, String> headers = new HashMap<String, String>();
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
    XMLElement req = new XMLElement(AccountConstants.AUTH_REQUEST);
    req.addAttribute(AccountConstants.A_CSRF_SUPPORT, "1");
    com.zimbra.common.soap.Element a = req.addUniqueElement(AccountConstants.E_ACCOUNT);
    a.addAttribute(AccountConstants.A_BY, "name");
    a.setText(acct.getName());
    req.addUniqueElement(AccountConstants.E_PASSWORD).setText("secret");
    Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
    MockHttpServletRequest authReq =
        (MockHttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
    authReq.setAttribute(Provisioning.A_zimbraCsrfTokenCheckEnabled, Boolean.TRUE);
    Random nonceGen = new Random();
    authReq.setAttribute(CsrfFilter.CSRF_SALT, nonceGen.nextInt() + 1);
    Element response = new Auth().handle(req, context);
    String authToken = response.getElement(AccountConstants.E_AUTH_TOKEN).getText();

    MockHttpServletRequest mockreq =
        new MockHttpServletRequest(
            form, url, "multipart/form-data; boundary=" + boundary, 7070, "test", headers);
    mockreq.setAttribute(CsrfFilter.CSRF_TOKEN_CHECK, Boolean.TRUE);

    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", authToken);
    mockreq.setCookies(cookie);

    MockHttpServletResponse resp = new MockHttpServletResponse();

    servlet.doPost(mockreq, resp);
    // <html><head><script language='javascript'>function doit() {
    // window.parent._uploadManager.loaded(401,'null'); }
    // </script></head><body onload='doit()'></body></html>
    String respStrg = resp.output.toString();
    assertTrue(respStrg.contains("401"));
  }

  @Test
  void testFileUploadAuthTokenCsrfEnabled2() throws Exception {

    HashMap<String, String> headers = new HashMap<String, String>();
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");

    XMLElement req = new XMLElement(AccountConstants.AUTH_REQUEST);
    req.addAttribute(AccountConstants.A_CSRF_SUPPORT, "1");
    com.zimbra.common.soap.Element a = req.addUniqueElement(AccountConstants.E_ACCOUNT);
    a.addAttribute(AccountConstants.A_BY, "name");
    a.setText(acct.getName());
    req.addUniqueElement(AccountConstants.E_PASSWORD).setText("secret");
    Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
    MockHttpServletRequest authReq =
        (MockHttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
    authReq.setAttribute(Provisioning.A_zimbraCsrfTokenCheckEnabled, Boolean.TRUE);
    Random nonceGen = new Random();
    authReq.setAttribute(CsrfFilter.CSRF_SALT, nonceGen.nextInt() + 1);
    Element response = new Auth().handle(req, context);
    String authToken = response.getElement(AccountConstants.E_AUTH_TOKEN).getText();
    String csrfToken = response.getElement("csrfToken").getText();

    URL url = new URL("http://localhost:7070/service/upload?lbfums=");
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    addFormField(bb, "_charset_", "");
    addFormField(bb, "filename1", filename1);
    addFormField(bb, "csrfToken", csrfToken);
    addFormFile(bb, filename1, "text/plain", content1);

    endForm(bb);

    byte[] form = bb.toByteArray();

    MockHttpServletRequest mockreq =
        new MockHttpServletRequest(
            form, url, "multipart/form-data; boundary=" + boundary, 7070, "test", headers);
    mockreq.setAttribute(CsrfFilter.CSRF_TOKEN_CHECK, Boolean.TRUE);

    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", authToken);
    mockreq.setCookies(cookie);

    MockHttpServletResponse resp = new MockHttpServletResponse();

    servlet.doPost(mockreq, resp);
    String respStrg = resp.output.toString();
    assertTrue(respStrg.contains("200"));
  }

  /**
   * Helper method to create {@link CookieStore} instance with {@link AuthToken} cookie for given
   * host and path.
   *
   * @param authToken
   * @return
   * @throws AuthTokenException
   */
  private CookieStore createCookieStoreWithAuthToken(
      AuthToken authToken, String domain, String path) throws AuthTokenException {
    final CookieStore cookieStore = new BasicCookieStore();
    final BasicClientCookie cookie =
        new BasicClientCookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded());
    cookie.setDomain(domain);
    cookie.setPath(path);
    cookieStore.addCookie(cookie);
    return cookieStore;
  }

  @Test
  @DisplayName(
      "Extended filename should be preferred if the multipart upload request contains "
          + "Content-Disposition header with extended filename param as defined in RFC-6266")
  void handleMultipartUpload_should_parseExtendedFileNameFromContentDisposition() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    final Path filePath =
        Paths.get(
            Objects.requireNonNull(
                    getClass().getResource("\u0421\u043e\u0431\u044b\u0442\u0438\u044f.txt"))
                .toURI()); // События.txt
    final String fileName = Paths.get(filePath.toUri()).getFileName().toString();
    final String asciiFileName = "Events.txt";
    final String asciiFileName1 = "Fruten Fraten.txt";
    final String utf8EncodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    final String utf8EncodeFileName1 =
        URLEncoder.encode("Фрутен Фриат.txt", StandardCharsets.UTF_8);

    final String contentDispositionForPart =
        "form-data; name=\"file\"; filename=\""
            + asciiFileName
            + "\"; filename*=utf-8''"
            + utf8EncodeFileName;
    final FormBodyPart part1 =
        FormBodyPartBuilder.create()
            .setName("file")
            .setBody(new FileBody(filePath.toFile()))
            .setField(MIME.CONTENT_DISPOSITION, contentDispositionForPart) // this is necessary
            .setField(MIME.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString())
            .build();

    final String contentDispositionForPart1 =
        "form-data; name=\"file\"; filename=\""
            + asciiFileName1
            + "\"; filename*=utf-8''"
            + utf8EncodeFileName1;
    final FormBodyPart part2 =
        FormBodyPartBuilder.create()
            .setName("file2")
            .setBody(new FileBody(filePath.toFile()))
            .setField(MIME.CONTENT_DISPOSITION, contentDispositionForPart1) // this is necessary
            .setField(MIME.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString())
            .build();

    final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
    final HttpEntity multipartEntity =
        multipartEntityBuilder
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addPart(part1)
            .addPart(part2)
            .build();
    final HttpPost httpPost =
        new HttpPost(URI.create(server.getURI().toString() + "upload?fmt=extended,raw"));
    httpPost.setEntity(multipartEntity);

    try (CloseableHttpClient httpClient =
        HttpClientBuilder.create()
            .setDefaultCookieStore(
                createCookieStoreWithAuthToken(authToken, server.getURI().getHost(), "/"))
            .build()) {
      final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

      final int statusCode = httpResponse.getStatusLine().getStatusCode();
      assertEquals(HttpStatus.SC_OK, statusCode);

      final String responseContent =
          EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

      final JSONArray jsonArray = new JSONArray("[" + responseContent + "]");
      assertEquals(200, jsonArray.getInt(0));
      assertEquals("null", jsonArray.getString(1));

      final JSONArray responseDataArray = jsonArray.getJSONArray(2);
      assertEquals(2, responseDataArray.length());

      final JSONObject firstItem = responseDataArray.getJSONObject(0);
      assertNotNull(firstItem.getString("aid"));

      final String contentType = firstItem.getString("ct");
      assertNotNull(contentType);
      assertTrue(contentType.toLowerCase().contains("text/plain"));

      final String filenameInResponse = firstItem.getString("filename");
      assertNotNull(filenameInResponse);
      assertEquals(fileName, filenameInResponse);
    }
  }

  @Test
  @DisplayName(
      "Filename should be parsed correctly when multipart upload is composed with "
          + "MultipartEntityBuilder and charset set to UTF-8")
  void handleMultipartUpload_should_parseFileNameFromContentDisposition() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    final Path filePath =
        Paths.get(
            Objects.requireNonNull(
                    getClass().getResource("\u0421\u043e\u0431\u044b\u0442\u0438\u044f.txt"))
                .toURI()); // События.txt
    final String fileName = Paths.get(filePath.toUri()).getFileName().toString();
    final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
    multipartEntityBuilder.setCharset(StandardCharsets.UTF_8); // this is necessary
    final HttpEntity multipartEntity =
        multipartEntityBuilder
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("file", filePath.toFile(), ContentType.TEXT_PLAIN, fileName)
            .addBinaryBody("file2", filePath.toFile(), ContentType.TEXT_PLAIN, fileName)
            .build();

    final HttpPost httpPost =
        new HttpPost(URI.create(server.getURI().toString() + "upload?fmt=extended,raw"));
    httpPost.setEntity(multipartEntity);

    try (CloseableHttpClient httpClient =
        HttpClientBuilder.create()
            .setDefaultCookieStore(
                createCookieStoreWithAuthToken(authToken, server.getURI().getHost(), "/"))
            .build()) {
      final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

      final int statusCode = httpResponse.getStatusLine().getStatusCode();
      assertEquals(HttpStatus.SC_OK, statusCode);

      final String responseContent =
          EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

      final JSONArray jsonArray = new JSONArray("[" + responseContent + "]");
      assertEquals(200, jsonArray.getInt(0));
      assertEquals("null", jsonArray.getString(1));

      final JSONArray responseDataArray = jsonArray.getJSONArray(2);
      assertEquals(2, responseDataArray.length());

      final JSONObject firstItem = responseDataArray.getJSONObject(0);
      assertNotNull(firstItem.getString("aid"));

      final String contentType = firstItem.getString("ct");
      assertNotNull(contentType);
      assertTrue(contentType.toLowerCase().contains("text/plain"));

      final String filenameInResponse = firstItem.getString("filename");
      assertNotNull(filenameInResponse);
      assertEquals(fileName, filenameInResponse);
    }
  }

  @Test
  @DisplayName(
      "Extended filename should be preferred if the plain upload request contains "
          + "Content-Disposition header with extended filename param as defined in RFC-6266")
  void handlePlainUpload_should_parseExtendedFileNameFromContentDisposition() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    final Path filePath =
        Paths.get(
            Objects.requireNonNull(
                    getClass().getResource("\u0421\u043e\u0431\u044b\u0442\u0438\u044f.txt"))
                .toURI()); // События.txt
    final String fileName = Paths.get(filePath.toUri()).getFileName().toString();
    final String asciiFileName = "Events.txt";
    final String utf8EncodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

    final HttpPost httpPost =
        new HttpPost(URI.create(server.getURI().toString() + "upload?fmt=extended,raw"));
    httpPost.setEntity(new ByteArrayEntity(Files.readAllBytes(filePath)));
    httpPost.setHeader(
        "Content-Disposition",
        "attachment; filename=" + asciiFileName + "; filename*=utf-8''" + utf8EncodeFileName);

    try (CloseableHttpClient httpClient =
        HttpClientBuilder.create()
            .setDefaultCookieStore(
                createCookieStoreWithAuthToken(authToken, server.getURI().getHost(), "/"))
            .build()) {
      final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

      final int statusCode = httpResponse.getStatusLine().getStatusCode();
      assertEquals(HttpStatus.SC_OK, statusCode);

      final String responseContent =
          EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

      final JSONArray jsonArray = new JSONArray("[" + responseContent + "]");
      assertEquals(200, jsonArray.getInt(0));
      assertEquals("null", jsonArray.getString(1));

      final JSONArray responseDataArray = jsonArray.getJSONArray(2);
      assertTrue(responseDataArray.length() > 0);

      final JSONObject firstItem = responseDataArray.getJSONObject(0);
      assertNotNull(firstItem.getString("aid"));

      final String contentType = firstItem.getString("ct");
      assertNotNull(contentType);
      assertEquals("text/plain", contentType);

      final String filenameInResponse = firstItem.getString("filename");
      assertNotNull(filenameInResponse);
      assertEquals(fileName, filenameInResponse);
    }
  }
}
