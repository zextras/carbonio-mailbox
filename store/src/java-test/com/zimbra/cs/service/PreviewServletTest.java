package com.zimbra.cs.service;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.account.Auth;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.servlet.CsrfFilter;
import com.zimbra.soap.SoapServlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.URIUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class PreviewServletTest {
  private static PreviewServlet previewServlet;
  private static String authTokenString;

  @BeforeClass
  public static void init() throws Exception {

    previewServlet = new PreviewServlet();

    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("test@zimbra.com", "secret", attrs);

    authTokenString = getAuthTokenString();
  }

  static String getAuthTokenString() throws Exception {
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

    return response.getElement(AccountConstants.E_AUTH_TOKEN).getText();
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @After
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldReturn400WhenRequiredParametersAreNotPassedInDoGetRequest() throws Exception {
    URL url = new URL("http://localhost:7070/service/preview/");
    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", authTokenString);
    MockHttpServletRequest mockRequest = PowerMockito.mock(MockHttpServletRequest.class);
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    PowerMockito.when(mockRequest.getRequestURL()).thenReturn(urlBuffer);
    mockRequest.setCookies(cookie);
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    previewServlet.doGet(mockRequest, mockResponse);
    assertEquals(400, mockResponse.getStatus());
  }

  @Test
  public void shouldReturn401WhenCheckAuthTokenFromCookieOrRespondWithErrorIfCookieNotSet()
      throws Exception {
    URL url = new URL("http://localhost:7070/service/preview/");
    MockHttpServletRequest mockRequest = PowerMockito.mock(MockHttpServletRequest.class);
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    PowerMockito.when(mockRequest.getRequestURL()).thenReturn(urlBuffer);
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    final AuthToken authToken = AuthProvider.getAuthToken(mockRequest, false);
    previewServlet.checkAuthTokenFromCookieOrRespondWithError(authToken, mockRequest, mockResponse);
    assertEquals(401, mockResponse.getStatus());
  }

  @Test
  public void shouldProvideCorrectPreviewQueryParametersWhenCalledParseQueryParameters()
      throws Exception {
    URL url =
        new URL(
            "http://localhost:7070/service/preview/image/290/4/200x200/thumbnail/?quality=high&shape=rounded&output_format=png");

    PreviewQueryParameters.Quality expQuality = PreviewQueryParameters.Quality.HIGH;
    PreviewQueryParameters.Format expFormat = PreviewQueryParameters.Format.PNG;
    PreviewQueryParameters.Shape expShape = PreviewQueryParameters.Shape.ROUNDED;

    PreviewQueryParameters expPreviewQueryParameters =
        new PreviewQueryParameters(expQuality, expFormat, expShape);

    final PreviewQueryParameters previewQueryParameters =
        previewServlet.parseQueryParameters(url.getQuery());

    assertEquals(expPreviewQueryParameters.getQuality(), previewQueryParameters.getQuality());
    assertEquals(expPreviewQueryParameters.getShape(), previewQueryParameters.getShape());
    assertEquals(
        expPreviewQueryParameters.getOutputFormat(), previewQueryParameters.getOutputFormat());
  }

  @Test
  public void shouldReturnFullUrlWhenCalledGetUrlWithQueryParams()
      throws MalformedURLException, URISyntaxException {
    URL url =
        new URL(
            "http://localhost:7070/service/preview/image/290/4/200x200/thumbnail/?quality=high&shape=rounded&output_format=png");
    HttpServletRequest mockRequest = PowerMockito.mock(HttpServletRequest.class);
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    PowerMockito.when(mockRequest.getRequestURL()).thenReturn(urlBuffer);

    final String expectedUrlWithQueryParams = previewServlet.getUrlWithQueryParams(mockRequest);
    Assert.assertEquals(expectedUrlWithQueryParams, url.toString());
  }

  @Test
  public void shouldRespondWithSuccessWhenCalledRespondWithSuccess() {
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    final String content = "test input stream";
    final InputStream inputStream =
        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    final String filename = "filename.txt";
    BlobResponseStore bs =
        new BlobResponseStore(inputStream, filename, (long) content.length(), "text/plain", "i");

    previewServlet.respondWithSuccess(mockResponse, bs);
    assertEquals(content, mockResponse.output.toString());
  }

  @Test
  public void shouldRespondWithErrorWhenCalledRespondWithError() {
    // test status code
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    previewServlet.respondWithError(mockResponse, HttpServletResponse.SC_NOT_FOUND, "Not Found");
    assertEquals(404, mockResponse.getStatus());

    // test status message
    MockHttpServletResponse mockResponseErrorMessage = new MockHttpServletResponse();
    previewServlet.respondWithError(
        mockResponseErrorMessage, HttpServletResponse.SC_NOT_FOUND, "Not Found");
    assertEquals("Not Found", mockResponse.getMsg());
  }

  /**
   * Wrapper around {@link HttpServletResponse} class to enable mocking with desired customizations
   */
  private static class MockHttpServletResponse implements HttpServletResponse {

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    private int status = 0;
    private String msg = null;

    @Override
    public void flushBuffer() {}

    @Override
    public int getBufferSize() {
      return 0;
    }

    @Override
    public void setBufferSize(int arg0) {}

    @Override
    public String getCharacterEncoding() {
      return null;
    }

    @Override
    public void setCharacterEncoding(String arg0) {}

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public void setContentType(String arg0) {}

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public void setLocale(Locale arg0) {}

    @Override
    public ServletOutputStream getOutputStream() {
      return new MockServletOutputStream();
    }

    @Override
    public PrintWriter getWriter() {
      return new PrintWriter(output);
    }

    @Override
    public boolean isCommitted() {
      return false;
    }

    @Override
    public void reset() {}

    @Override
    public void resetBuffer() {}

    @Override
    public void setContentLength(int arg0) {}

    @Override
    public void setContentLengthLong(long arg0) {}

    @Override
    public void addCookie(Cookie arg0) {}

    @Override
    public void addDateHeader(String arg0, long arg1) {}

    @Override
    public void addHeader(String arg0, String arg1) {}

    @Override
    public void addIntHeader(String arg0, int arg1) {}

    @Override
    public boolean containsHeader(String arg0) {
      return false;
    }

    @Override
    public String encodeRedirectURL(String arg0) {
      return null;
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
      return null;
    }

    @Override
    public String encodeURL(String arg0) {
      return null;
    }

    @Override
    public String encodeUrl(String arg0) {
      return null;
    }

    @Override
    public String getHeader(String arg0) {
      return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
      return null;
    }

    @Override
    public Collection<String> getHeaders(String arg0) {
      return null;
    }

    @Override
    public int getStatus() {
      return status;
    }

    @Override
    public void setStatus(int arg0) {}

    @Override
    public void sendError(int arg0) {}

    @Override
    public void sendError(int status, String msg) {
      this.status = status;
      this.msg = msg;
    }

    @Override
    public void sendRedirect(String arg0) {}

    @Override
    public void setDateHeader(String arg0, long arg1) {}

    @Override
    public void setHeader(String arg0, String arg1) {}

    @Override
    public void setIntHeader(String arg0, int arg1) {}

    @Override
    public void setStatus(int arg0, String arg1) {}

    public String getMsg() {
      return msg;
    }

    class MockServletOutputStream extends ServletOutputStream {
      @Override
      public void write(int b) throws IOException {
        output.write(b);
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener listener) {}
    }
  }
}
