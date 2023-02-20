package com.zimbra.cs.service;

import static com.zimbra.cs.service.PreviewServlet.DOC_THUMBNAIL_REGEX;
import static com.zimbra.cs.service.PreviewServlet.IMG_THUMBNAIL_REGEX;
import static com.zimbra.cs.service.PreviewServlet.PDF_PREVIEW_REGEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zextras.carbonio.preview.queries.Query;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.URIUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PreviewServletTest {

  private static final String TEST_ACCOUNT_NAME = "test@zimbra.com";

  static String getAuthTokenString() throws Exception {
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, TEST_ACCOUNT_NAME);
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

  @AfterClass
  public static void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount(TEST_ACCOUNT_NAME, "secret", attrs);
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldReturn400WhenRequiredParametersAreNotPassedInDoGetRequest() throws Exception {
    URL url = new URL("http://localhost:7070/service/preview/");
    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", getAuthTokenString());
    MockHttpServletRequest mockRequest = mock(MockHttpServletRequest.class);
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    when(mockRequest.getRequestURL()).thenReturn(urlBuffer);
    mockRequest.setCookies(cookie);
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    final PreviewServlet previewServlet = new PreviewServlet();
    previewServlet.doGet(mockRequest, mockResponse);
    assertEquals(400, mockResponse.getStatus());
  }

  @Test
  public void shouldReturn401WhenGetAuthTokenFromCookieOrRespondWithErrorIfCookieNotSet()
      throws Exception {
    URL url = new URL("http://localhost:7070/service/preview/");
    MockHttpServletRequest mockRequest = mock(MockHttpServletRequest.class);
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    when(mockRequest.getRequestURL()).thenReturn(urlBuffer);
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    final PreviewServlet previewServlet = new PreviewServlet();
    previewServlet.getAuthTokenFromCookieOrRespondWithError(mockRequest, mockResponse);
    assertEquals(401, mockResponse.getStatus());
  }

  @Test // CO-300
  public void shouldProxy5xxErrorsWith404() throws IOException {
    final PreviewServlet previewServlet = new PreviewServlet();
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    previewServlet.sendError(mockResponse, 500, "Internal ServerError");
    assertEquals(HttpServletResponse.SC_NOT_FOUND, mockResponse.getStatus());
  }

  @Test // CO-300
  public void shouldProxy5xxErrorWith404WhenCalledDoGet() throws Exception {
    URL url =
        new URL(
            "http://localhost:7070/service/preview/image/290/4/200x200/thumbnail/?quality=high&shape=rounded&output_format=png");
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PreviewServlet servlet = new PreviewServlet();
    Cookie cookie = new Cookie("ZM_AUTH_TOKEN", getAuthTokenString());
    when(request.getRequestURL()).thenReturn(urlBuffer);
    when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    servlet.doGet(request, response);
    // should actually throw 5xx since there is no preview client, but our proxy will handle the
    // conversion
    verify(response, atLeastOnce()).sendError(404, "Preview service is down/not ready");
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
        PreviewServlet.parseQueryParameters(url.getQuery());

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
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    StringBuffer urlBuffer = new StringBuffer(128);
    urlBuffer.append(
        URIUtil.newURI(
            url.toURI().getScheme(), url.getHost(), url.getPort(), url.getPath(), url.getQuery()));
    when(mockRequest.getRequestURL()).thenReturn(urlBuffer);

    final PreviewServlet previewServlet = new PreviewServlet();
    final String expectedUrlWithQueryParams = previewServlet.getUrlWithQueryParams(mockRequest);
    assertEquals(expectedUrlWithQueryParams, url.toString());
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

    final PreviewServlet previewServlet = new PreviewServlet();
    previewServlet.respondWithSuccess(mockResponse, bs);
    assertEquals(content, mockResponse.output.toString());
  }

  @Test
  public void shouldRespondWithErrorWhenCalledRespondWithError() {
    // test status code
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    final PreviewServlet previewServlet = new PreviewServlet();
    previewServlet.respondWithError(mockResponse, HttpServletResponse.SC_NOT_FOUND, "Not Found");
    assertEquals(404, mockResponse.getStatus());

    // test status message
    MockHttpServletResponse mockResponseErrorMessage = new MockHttpServletResponse();
    previewServlet.respondWithError(
        mockResponseErrorMessage, HttpServletResponse.SC_NOT_FOUND, "Not Found");
    assertEquals("Not Found", mockResponse.getMsg());
  }

  @Test
  public void shouldReturnCorrectDispositionTypeWhenCalledGetDispositionType() {
    // case 1: inline
    String requestUrl =
        "https://nbm-s01.demo.zextras.io/service/preview/document/562/3/800x800/thumbnail/?disp=inline";
    String dispositionType = PreviewServlet.getDispositionType(requestUrl);
    assertEquals("inline", dispositionType);

    // case 2: a
    requestUrl =
        "https://nbm-s01.demo.zextras.io/service/preview/document/562/3/800x800/thumbnail/?disp=a";
    dispositionType = PreviewServlet.getDispositionType(requestUrl);
    assertEquals("a", dispositionType);
  }

  @Test
  public void
  shouldReturnCorrectUrlForPreviewWhenDispQueryParameterIsAtFirstPosAndIsOnlyQueryParam() {
    String requestUrl =
        "https://nbm-s01.demo.zextras.io/service/preview/document/562/3/800x800/thumbnail/?disp=i";
    final String dispositionType = PreviewServlet.getDispositionType(requestUrl);

    String requestUrlForPreview =
        PreviewServlet.getRequestUrlForPreview(requestUrl, dispositionType);
    assertEquals(
        "https://nbm-s01.demo.zextras.io/service/preview/document/562/3/800x800/thumbnail/",
        requestUrlForPreview);
  }

  @Test
  public void shouldReturnCorrectUrlForPreviewWhenDispQueryParameterIsAtFirstPos() {
    String requestUrl =
        "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/?disp=a&first_page=1&last_page=1";
    final String dispositionType = PreviewServlet.getDispositionType(requestUrl);
    String requestUrlForPreview =
        PreviewServlet.getRequestUrlForPreview(requestUrl, dispositionType);
    assertEquals(
        "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/?first_page=1&last_page=1",
        requestUrlForPreview);
  }

  @Test
  public void shouldReturnCorrectUrlForPreviewWhenDispQueryParameterIsAtLastPos() {
    String requestUrl =
        "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/?first_page=1&last_page=1&disp=a";
    final String dispositionType = PreviewServlet.getDispositionType(requestUrl);
    String requestUrlForPreview =
        PreviewServlet.getRequestUrlForPreview(requestUrl, dispositionType);
    assertEquals(
        "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/?first_page=1&last_page=1",
        requestUrlForPreview);
  }

  @Test
  public void shouldReturnCorrectUrlForPreviewWhenDispQueryParameterIsAtSecondPos() {
    String requestUrl =
        "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/?first_page=1&disp=a&last_page=1";
    final String dispositionType = PreviewServlet.getDispositionType(requestUrl);
    String requestUrlForPreview =
        PreviewServlet.getRequestUrlForPreview(requestUrl, dispositionType);
    assertEquals(
        "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/?first_page=1&last_page=1",
        requestUrlForPreview);
  }

  @Test
  public void shouldReturnDefaultDispQueryWhenQueryParameterIsMissing() {
    String requestUrl = "https://nbm-s01.demo.zextras.io/service/preview/pdf/531/2/";
    final String dispositionType = PreviewServlet.getDispositionType(requestUrl);
    assertEquals("i", dispositionType);
  }

  @Test
  public void shouldGetContentServletResourceUrlWhenCalled() throws ServiceException {

    Account account = Provisioning.getInstance().get(Key.AccountBy.name, TEST_ACCOUNT_NAME);
    AuthToken mockAuthToken = mock(AuthToken.class);
    when(mockAuthToken.getAccount()).thenReturn(account);
    String messageId = "16e683a8-288b-40e2-810e-d0482363a541:315";
    String part = "2";
    String contentServletResourceUrl =
        PreviewServlet.getContentServletResourceUrl(mockAuthToken, messageId, part);
    assertEquals(
        "http://localhost:80/home/test@zimbra.com?auth=co&id=16e683a8-288b-40e2-810e-d0482363a541:315&part=2",
        contentServletResourceUrl);
  }

  @Test
  public void shouldReturnCorrectQueryWhenCalledGenerateQuery() {
    String requestUrlForPreview =
        "https://nbm-s01.demo.zextras.io/service/preview/image/258/3/200x200/thumbnail?quality=high&shape=rounded&output_format=png&disp=inline";

    Matcher thumbnailImageMatcher =
        Pattern.compile(IMG_THUMBNAIL_REGEX)
            .matcher(PreviewServlet.getRequestUrlForPreview(requestUrlForPreview, "inline"));

    assertTrue(thumbnailImageMatcher.find());
    String previewArea = thumbnailImageMatcher.group(3);
    PreviewQueryParameters queryParameters =
        PreviewServlet.parseQueryParameters(thumbnailImageMatcher.group(4));
    final Query query = PreviewServlet.generateQuery(previewArea, queryParameters);

    assertEquals("200x200", query.getPreviewArea().get());
    assertEquals("high", query.getQuality().get());
    assertEquals("rounded", query.getShape().get());
    assertEquals("png", query.getOutputFormat().get());
  }

  /**
   * CO-594
   */
  @Test
  public void shouldMatchNestedPartNumberFromRequestQuery() {
    final String requestUrlForPdfPreview = "https://example.com/service/preview/pdf/14473/1.3.2.1.2.3";

    final Matcher matcher =
        Pattern.compile(PDF_PREVIEW_REGEX)
            .matcher(PreviewServlet.getRequestUrlForPreview(requestUrlForPdfPreview, "inline"));

    final String EXPECTED_PART_NUMBER = "1.3.2.1.2.3";

    assertTrue(matcher.find());
    assertEquals(EXPECTED_PART_NUMBER, matcher.group(2));
  }

  /**
   * CO-594
   */
  @Test
  public void shouldMatchNestedPartNumberFromThumbnailRequestQuery() {
    final String requestUrlForPdfPreview = "https://example.com/service/preview/document/14473/1.3.2.1.2.3/20x20/thumbnail/";

    final Matcher matcher =
        Pattern.compile(DOC_THUMBNAIL_REGEX)
            .matcher(PreviewServlet.getRequestUrlForPreview(requestUrlForPdfPreview, "inline"));

    final String EXPECTED_PART_NUMBER = "1.3.2.1.2.3";

    assertTrue(matcher.find());
    assertEquals(EXPECTED_PART_NUMBER, matcher.group(2));
  }

  /**
   * Wrapper around {@link HttpServletResponse} class to enable mocking with desired customizations
   */
  private static class MockHttpServletResponse implements HttpServletResponse {

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    private int status = 0;
    private String msg = null;

    @Override
    public void flushBuffer() {
    }

    @Override
    public int getBufferSize() {
      return 0;
    }

    @Override
    public void setBufferSize(int arg0) {
    }

    @Override
    public String getCharacterEncoding() {
      return null;
    }

    @Override
    public void setCharacterEncoding(String arg0) {
    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public void setContentType(String arg0) {
    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public void setLocale(Locale arg0) {
    }

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
    public void reset() {
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public void setContentLength(int arg0) {
    }

    @Override
    public void setContentLengthLong(long arg0) {
    }

    @Override
    public void addCookie(Cookie arg0) {
    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
    }

    @Override
    public void addHeader(String arg0, String arg1) {
    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
    }

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
    public void setStatus(int arg0) {
    }

    @Override
    public void sendError(int arg0) {
    }

    @Override
    public void sendError(int status, String msg) {
      this.status = status;
      this.msg = msg;
    }

    @Override
    public void sendRedirect(String arg0) {
    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
    }

    @Override
    public void setHeader(String arg0, String arg1) {
    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
    }

    @Override
    public void setStatus(int arg0, String arg1) {
    }

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
      public void setWriteListener(WriteListener listener) {
      }
    }
  }
}
