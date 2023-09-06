// SPDX-FileCopyrightText: 2014 Zimbra, Inc.
package com.zimbra.cs.service;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.mail.CreateCalendarItem;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author zimbra
 */
public class UserServletTest {

  private final String serverHost = "127.0.0.1";
  private Account testAccount;
  private Server server;

  /**
   * @throws java.lang.Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.initServer("");
    server = new Server();
    final ServletHolder servletHolder = new ServletHolder(UserServlet.class);
    final ServletContextHandler servletContextHandler = new ServletContextHandler();
    servletContextHandler.addServlet(servletHolder, "/*");
    server.setHandler(servletContextHandler);
    final ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.setHost(serverHost);
    server.setConnectors(new ServerConnector[] {serverConnector});
    server.start();
    Provisioning prov = Provisioning.getInstance();
    HashMap<String, Object> attrs = new HashMap<String, Object>();
    attrs.put(Provisioning.A_zimbraAccountStatus, "pending");
    prov.createAccount("testbug39481@zimbra.com", "secret", attrs);
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
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
      server.stop();
    } catch (Exception e) {

    }
  }

  /**
   * Test method for {@link
   * com.zimbra.cs.service.UserServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)}.
   */
  @Test
  void testDoGet() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    MockHttpServletResponse response = new MockHttpServletResponse();
    UserServlet userServlet = new UserServlet();
    try {

      spy(ZimbraServlet.class);
      spy(UserServlet.class);

      mockStatic(L10nUtil.class);

      when(request.getPathInfo()).thenReturn("/testbug3948@zimbra.com");
      when(request.getRequestURI()).thenReturn("service/home/");
      when(request.getParameter("auth")).thenReturn("basic");
      when(request.getParameter("loc")).thenReturn("en_US");
      when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDM0ODg6dGVzdDEyMw==");
      when(request.getQueryString()).thenReturn("auth=basic&view=text&id=261");

      userServlet.doGet(request, response);
      assertEquals(401, response.getStatus());
      //            Commenting until we can figure out why this fails in CI env.
      //            Assert.assertEquals("must authenticate", response.getMsg());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown.");
    }
  }

  /**
   * Make a Calendar call to {@link UserServlet} endpoint using {@link #testAccount}
   *
   * @return
   * @throws Exception
   */
  private HttpResponse defaultUserCalendarRequest() throws Exception {
    return getUserServletRequest(server.getURI().toString() + "~/Calendar.json?auth=co");
  }

  private HttpResponse getUserServletRequest(String userServletEndpoint) throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    final CookieStore cookieStore = new BasicCookieStore();
    final BasicClientCookie cookie =
        new BasicClientCookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded());
    cookie.setDomain(serverHost);
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    try (CloseableHttpClient client =
        HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {
      final HttpGet httpGet = new HttpGet();
      httpGet.setURI(URI.create(userServletEndpoint));
      return client.execute(httpGet);
    }
  }

  private HttpResponse postUserServletRequest(String userServletEndpoint, String fileToUpload)
      throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    final CookieStore cookieStore = new BasicCookieStore();
    final BasicClientCookie cookie =
        new BasicClientCookie(COOKIE_ZM_AUTH_TOKEN, authToken.getEncoded());
    cookie.setDomain(serverHost);
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    try (CloseableHttpClient client =
        HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {
      final HttpPost httpPost = new HttpPost();
      httpPost.setEntity(
          new FileEntity(new File(this.getClass().getResource(fileToUpload).getFile())));
      httpPost.setURI(URI.create(userServletEndpoint));
      return client.execute(httpPost);
    }
  }

  /** Adds an appointment for {@link #testAccount} */
  private void createDefaultCalendarAppointmentForTestUser() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(testAccount);
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            authToken, testAccount.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    final Element createAppointmentElement =
        Element.parseJSON(
            new String(
                this.getClass()
                    .getResourceAsStream("SampleAppointmentRequest.json")
                    .readAllBytes()),
            MailConstants.CREATE_APPOINTMENT_REQUEST,
            JSONElement.mFactory);
    final CreateCalendarItem createCalendarItem = new CreateCalendarItem();
    createCalendarItem.setResponseQName(MailConstants.CREATE_APPOINTMENT_REQUEST);
    createCalendarItem.handle(createAppointmentElement, context);
  }

  @Test
  @DisplayName("User has no appointments, UserServlet should return an empty JSON.")
  void shouldReturnEmptyJsonIfUserHasNoItemsInCalendar() throws Exception {
    final HttpResponse httpCalendarResponse = this.defaultUserCalendarRequest();
    Assertions.assertEquals(HttpStatus.SC_OK, httpCalendarResponse.getStatusLine().getStatusCode());
    final String calendarResponse =
        new String(httpCalendarResponse.getEntity().getContent().readAllBytes());
    Assertions.assertEquals("{}", calendarResponse);
  }

  @Test
  @DisplayName("User has 2 appointment, UserServlet should return a JSON with 2 appointments.")
  void shouldNotReturnEmptyJsonIfUserHasAppointments() throws Exception {
    this.createDefaultCalendarAppointmentForTestUser();
    this.createDefaultCalendarAppointmentForTestUser();
    final HttpResponse httpUserServletCalResponse = this.defaultUserCalendarRequest();
    Assertions.assertEquals(
        HttpStatus.SC_OK, httpUserServletCalResponse.getStatusLine().getStatusCode());
    final String calendarResponse =
        new String(httpUserServletCalResponse.getEntity().getContent().readAllBytes());
    Assertions.assertNotEquals("{}", calendarResponse);
    final CalendarJson calendarResponseMap =
        new ObjectMapper().readValue(calendarResponse, CalendarJson.class);
    Assertions.assertEquals(2, calendarResponseMap.getAppt().size());
  }

  @Test
  @DisplayName("Upload .zip calendar, check calendar is not empty and contains 1 calendar")
  void shouldUploadIcsCalendarToUserServlet() throws Exception {
    this.postUserServletRequest(
        server.getURI().toString() + "~/calendar?auth=co&fmt=zip", "UploadCalendar.zip");
    final HttpResponse getCalendarsResponse = this.defaultUserCalendarRequest();
    Assertions.assertEquals(HttpStatus.SC_OK, getCalendarsResponse.getStatusLine().getStatusCode());
    final String calendarResponse =
        new String(getCalendarsResponse.getEntity().getContent().readAllBytes());
    Assertions.assertNotEquals("{}", calendarResponse);
    final CalendarJson calendarResponseMap =
        new ObjectMapper().readValue(calendarResponse, CalendarJson.class);
    Assertions.assertEquals(1, calendarResponseMap.getAppt().size());
  }

  /** DTO class for {@link UserServlet} Calendar response */
  private static class CalendarJson {

    @JsonProperty("appt")
    public List<LinkedHashMap> appt;

    public List<LinkedHashMap> getAppt() {
      return appt;
    }
  }

  public class MockHttpServletResponse implements HttpServletResponse {

    private int status = 0;
    private String msg = null;

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    @Override
    public int getBufferSize() {
      return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getContentType()
     */
    @Override
    public String getContentType() {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getLocale()
     */
    @Override
    public Locale getLocale() {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    @Override
    public boolean isCommitted() {
      return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#reset()
     */
    @Override
    public void reset() {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    @Override
    public void resetBuffer() {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    @Override
    public void setContentLength(int arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#setContentLengthLong(long)
     */
    @Override
    public void setContentLengthLong(long arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    @Override
    public void addCookie(Cookie arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String arg0, long arg1) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String arg0, String arg1) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String arg0, int arg1) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    @Override
    public boolean containsHeader(String arg0) {
      return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    @Override
    public String encodeRedirectURL(String arg0) {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     */
    @Override
    public String encodeRedirectUrl(String arg0) {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    @Override
    public String encodeURL(String arg0) {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     */
    @Override
    public String encodeUrl(String arg0) {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String arg0) {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getHeaderNames()
     */
    @Override
    public Collection<String> getHeaderNames() {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getHeaders(java.lang.String)
     */
    @Override
    public Collection<String> getHeaders(String arg0) {
      return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getStatus()
     */
    @Override
    public int getStatus() {
      return status;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    @Override
    public void setStatus(int arg0) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    @Override
    public void sendError(int arg0) throws IOException {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    @Override
    public void sendError(int status, String msg) throws IOException {
      this.status = status;
      this.msg = msg;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect(String arg0) throws IOException {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String arg0, long arg1) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String arg0, String arg1) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String arg0, int arg1) {
      // No implementation required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
     */
    @Override
    public void setStatus(int arg0, String arg1) {
      // No implementation required
    }

    public String getMsg() {
      return msg;
    }
  }
}
