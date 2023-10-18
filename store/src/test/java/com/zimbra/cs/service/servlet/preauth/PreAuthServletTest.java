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
import org.junit.jupiter.api.AfterEach;
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

  private AutoCloseable closeable;

  @BeforeAll
  static void init() throws Exception {

    MailboxTestUtil.initServer();

    final Provisioning prov = Provisioning.getInstance();

    final Map<String, Object> domainAttrs = Maps.newHashMap();
    final String preAuthKey = PreAuthKey.generateRandomPreAuthKey();

    domainAttrs.put(Provisioning.A_zimbraPreAuthKey, preAuthKey);
    prov.createDomain("test.com", domainAttrs);

    final Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("one@test.com", "secret", attrs);
  }

  @BeforeEach
  public void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void tearDown() throws Exception {
    closeable.close();
    MailboxTestUtil.clearData();
  }

  @Test
  void doGet_should_handleRawAuthTokenRequest_when_validRawAuthToken()
      throws ServletException, IOException, ServiceException, AuthTokenException {
    when(requestMock.getParameter(PreAuthParams.PARAM_IS_REDIRECT.getParamName())).thenReturn("1");
    when(requestMock.getParameter(PreAuthParams.PARAM_AUTHTOKEN.getParamName()))
        .thenReturn("0_3eda3cae8900bb9c5fbbc4151e28d759c67e7119");

    PreAuthServlet preAuthServletSpy = spy(PreAuthServlet.class);
    preAuthServletSpy.doGet(requestMock, responseMock);

    verify(preAuthServletSpy)
        .handleRawAuthTokenRequest(
            eq(requestMock),
            eq(responseMock),
            eq(Provisioning.getInstance()),
            eq("0_3eda3cae8900bb9c5fbbc4151e28d759c67e7119"),
            anyString(),
            eq(true));
  }

  @Test
  void doGet_should_handlePreAuthRequest_when_validPreAuth()
      throws ServletException, IOException, ServiceException {
    when(requestMock.getParameter(PreAuthParams.PARAM_IS_REDIRECT.getParamName())).thenReturn("1");
    when(requestMock.getParameter(PreAuthParams.PARAM_PRE_AUTH.getParamName()))
        .thenReturn("454ff228621d67771377de58b18035e03f2be0c9");

    PreAuthServlet preAuthServletSpy = spy(PreAuthServlet.class);
    preAuthServletSpy.doGet(requestMock, responseMock);

    verify(preAuthServletSpy)
        .handlePreAuthRequest(
            eq(requestMock),
            eq(responseMock),
            eq(Provisioning.getInstance()),
            anyString(),
            eq(true));
  }

  @Test
  void redirectToApp_should_redirectToAdminUrl_when_adminUser()
      throws ServiceException, IOException {
    final String baseUrl = "https://test.com";
    final StringBuilder sb = new StringBuilder("param1=value1&param2=value2");

    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    when(authTokenMock.getAccount()).thenReturn(account);

    preAuthServlet.redirectToApp(baseUrl, responseMock, authTokenMock, true, sb);
    verify(responseMock)
        .sendRedirect(
            String.format("https://test.com:6071%s?%s", PreAuthServlet.DEFAULT_ADMIN_URL, sb));
  }

  @Test
  void redirectToApp_should_redirectToMailUrl_when_nonAdminUser()
      throws ServiceException, IOException {

    final String baseUrl = "https://test.com";
    final StringBuilder sb = new StringBuilder("param1=value1&param2=value2");

    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    when(authTokenMock.getAccount()).thenReturn(account);

    preAuthServlet.redirectToApp(baseUrl, responseMock, authTokenMock, false, sb);
    verify(responseMock)
        .sendRedirect(
            String.format("%s?param1=value1&param2=value2", PreAuthServlet.DEFAULT_MAIL_URL));
  }

  @Test
  void setCookieAndRedirect_should_redirectToCorrectPath_when_validAuthToken()
      throws IOException, ServiceException {
    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    final long expires = 1690231807995L;
    final AuthToken authToken = AuthProvider.getAuthToken(account, expires, false, null);

    when(requestMock.getScheme()).thenReturn("https");
    when(requestMock.getParameter(PreAuthParams.PARAM_REDIRECT_URL.getParamName()))
        .thenReturn("https://test.com/carbonio");

    preAuthServlet.setCookieAndRedirect(requestMock, responseMock, authToken);
    verify(responseMock).sendRedirect("/carbonio");
  }

  @Test
  void setCookieAndRedirect_should_redirectToRootPath_when_nullRedirectUrl()
      throws IOException, ServiceException {
    final Account account = Provisioning.getInstance().get(Key.AccountBy.name, "one@test.com");
    final long expires = 1690231807995L;
    final AuthToken authToken = AuthProvider.getAuthToken(account, expires, false, null);

    when(requestMock.getScheme()).thenReturn("https");

    final Map<String, String[]> parameterMap = new HashMap<>();
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

    when(Utils.getOptionalParam(requestMock, PreAuthParams.PARAM_REDIRECT_URL.getParamName(), null))
        .thenReturn(null);

    preAuthServlet.setCookieAndRedirect(requestMock, responseMock, authToken);
    verify(responseMock).sendRedirect("/");
  }
}
