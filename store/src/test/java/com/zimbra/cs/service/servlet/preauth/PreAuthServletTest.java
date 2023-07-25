package com.zimbra.cs.service.servlet.preauth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.PreAuthKey;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

class PreAuthServletTest {

  @Mock private AuthToken authTokenMock;

  @Mock private HttpServletResponse responseMock;

  @Mock private HttpServletRequest requestMock;

  @InjectMocks private PreAuthServlet preAuthServlet;

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> domainAttrs = Maps.newHashMap();
    String preAuthKey = PreAuthKey.generateRandomPreAuthKey();

    domainAttrs.put(Provisioning.A_zimbraPreAuthKey, preAuthKey);
    prov.createDomain("test.com", domainAttrs);

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("one@test.com", "secret", attrs);
  }

  @Test
  void testDoGetWithRawAuthToken()
      throws ServletException, IOException, ServiceException, AuthTokenException {
    // Stub mocks
    when(requestMock.getParameter(PreAuthParams.PARAM_IS_REDIRECT.getParamName())).thenReturn("1");
    when(requestMock.getParameter(PreAuthParams.PARAM_AUTHTOKEN.getParamName()))
        .thenReturn(
            "0_3eda3cae8900bb9c5fbbc4151e28d759c67e7119_69643d33363a61666465373664322d396634342d343466382d616537392d3938633536313662326639363b6578703d31333a313639303338333032353431313b747970653d363a7a696d6272613b753d313a613b7469643d31303a313236313635323935363b");

    // Call doGet
    PreAuthServlet preAuthServletSpy = spy(PreAuthServlet.class);
    preAuthServletSpy.doGet(requestMock, responseMock);

    // Test: verify if handleRawAuthTokenRequest is called with the correct parameters
    verify(preAuthServletSpy)
        .handleRawAuthTokenRequest(
            eq(requestMock),
            eq(responseMock),
            eq(Provisioning.getInstance()),
            eq(
                "0_3eda3cae8900bb9c5fbbc4151e28d759c67e7119_69643d33363a61666465373664322d396634342d343466382d616537392d3938633536313662326639363b6578703d31333a313639303338333032353431313b747970653d363a7a696d6272613b753d313a613b7469643d31303a313236313635323935363b"),
            anyString(),
            eq(true));
  }

  @Test
  void testDoGetWithPreAuth() throws ServletException, IOException, ServiceException {
    // Stub mocks
    when(requestMock.getParameter(PreAuthParams.PARAM_IS_REDIRECT.getParamName())).thenReturn("1");
    when(requestMock.getParameter(PreAuthParams.PARAM_PRE_AUTH.getParamName()))
        .thenReturn("454ff228621d67771377de58b18035e03f2be0c9");

    // Call doGet
    PreAuthServlet preAuthServletSpy = spy(PreAuthServlet.class);
    preAuthServletSpy.doGet(requestMock, responseMock);

    // Test: verify if handlePreAuthRequest is called with the correct parameters
    verify(preAuthServletSpy)
        .handlePreAuthRequest(
            eq(requestMock),
            eq(responseMock),
            eq(Provisioning.getInstance()),
            anyString(),
            eq(true));
  }

  @Test
  void testRedirectToAppForAdmin() throws ServiceException, IOException {
    final String baseUrl = "https://test.com";
    final StringBuilder sb = new StringBuilder("param1=value1&param2=value2");

    // Stub
    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    when(authTokenMock.getAccount()).thenReturn(account);

    // Test: verify redirect to web admin UI
    preAuthServlet.redirectToApp(baseUrl, responseMock, authTokenMock, true, sb);
    verify(responseMock)
        .sendRedirect(
            String.format("https://test.com:6071%s?%s", PreAuthServlet.DEFAULT_ADMIN_URL, sb));
  }

  @Test
  void testRedirectToAppForNonAdmin() throws ServiceException, IOException {

    final String baseUrl = "https://test.com";
    final StringBuilder sb = new StringBuilder("param1=value1&param2=value2");

    // Stub
    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    when(authTokenMock.getAccount()).thenReturn(account);

    // Test: verify redirect to web UI
    preAuthServlet.redirectToApp(baseUrl, responseMock, authTokenMock, false, sb);
    verify(responseMock)
        .sendRedirect(
            String.format("%s?param1=value1&param2=value2", PreAuthServlet.DEFAULT_MAIL_URL));
  }

  @Test
  void testSetCookieAndRedirect() throws IOException, ServiceException {
    Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    long expires = 1690231807995L;
    AuthToken authToken = AuthProvider.getAuthToken(account, expires, false, null);

    // Stub mocks
    when(requestMock.getScheme()).thenReturn("https");
    when(Utils.getOptionalParam(requestMock, "redirectURL", null))
        .thenReturn("https://test.com/carbonio"); // external redirect url are sanitized

    // Test: verify redirects to correct path
    preAuthServlet.setCookieAndRedirect(requestMock, responseMock, authToken);
    verify(responseMock).sendRedirect("/carbonio");
  }

  @Test
  void testSetCookieAndRedirectWhenNullRedirectUrl() throws IOException, ServiceException {
    Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    long expires = 1690231807995L;
    AuthToken authToken = AuthProvider.getAuthToken(account, expires, false, null);

    // Stub mocks
    when(requestMock.getScheme()).thenReturn("https");

    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put("preauth", new String[] {"454ff228621d67771377de58b18035e03f2be0c9"});
    parameterMap.put("isredirect", new String[] {"1"});
    when(requestMock.getParameterMap()).thenReturn(parameterMap);
    when(requestMock.getParameterNames())
        .thenAnswer(
            (Answer<Enumeration<String>>)
                invocation -> {
                  Map<String, String[]> map = requestMock.getParameterMap();
                  final Iterator<String> it = map.keySet().iterator();
                  return new Enumeration<>() {
                    @Override
                    public boolean hasMoreElements() {
                      return it.hasNext();
                    }

                    @Override
                    public String nextElement() {
                      return it.next();
                    }
                  };
                });
    // set redirect URL to be null
    when(Utils.getOptionalParam(requestMock, "redirectURL", null)).thenReturn(null);

    // Test: verify redirects to correct path
    preAuthServlet.setCookieAndRedirect(requestMock, responseMock, authToken);
    verify(responseMock).sendRedirect("/");
  }

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    MailboxTestUtil.clearData();
  }
}
