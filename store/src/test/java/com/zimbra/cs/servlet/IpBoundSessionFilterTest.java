package com.zimbra.cs.servlet;

import static com.zimbra.cs.servlet.IpBoundSessionFilter.AUTHENTICATE_AND_TRY_AGAIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.servlet.util.AuthUtil;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IpBoundSessionFilterTest {

  private static Provisioning provisioning;
  @InjectMocks
  private IpBoundSessionFilter filter;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain chain;

  @BeforeAll
  static void init() throws Exception {
    MailboxTestUtil.setUp();

    provisioning = Provisioning.getInstance();
  }

  @AfterAll
  static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void should_allowRequest_when_filterIsDisabled() throws ServletException, IOException, ServiceException {
    // Disable the filter
    provisioning.getConfig().setCarbonioIpBoundSessionFilterEnabled(false);

    filter.doFilter(request, response, chain);

    // Verify the request is allowed
    verify(chain, times(1)).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
  }

  @Test
  void should_allowRequest_when_sessionTokenAndNoHijackingDetected()
      throws IOException, ServletException, ServiceException {
    // Enable the filter
    provisioning.getConfig().setCarbonioIpBoundSessionFilterEnabled(true);

    // Mock request with at least one valid session token
    var authToken = new Cookie("ZM_AUTH_TOKEN", "token");
    when(request.getCookies()).thenReturn(new Cookie[]{authToken});

    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});
    try (MockedStatic<ZimbraServlet> mockedZimbraServlet = Mockito.mockStatic(ZimbraServlet.class)) {
      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      filter.doFilter(request, response, chain);

      // Verify the request is allowed
      verify(chain).doFilter(request, response);
      verify(response, never()).sendError(anyInt(), anyString());
    }
  }

  @Test
  void should_blockRequest_when_sessionHijackingDetected() throws IOException, ServletException, ServiceException {
    // Enable the filter
    provisioning.getConfig().setCarbonioIpBoundSessionFilterEnabled(true);

    // Mock request with a valid session token
    var authTokenCookie = new Cookie("ZM_AUTH_TOKEN", "token");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));
    when(request.getCookies()).thenReturn(new Cookie[]{authTokenCookie});
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

    // Mock trusted IPs
    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});
    try (MockedStatic<ZimbraServlet> mockedZimbraServlet = Mockito.mockStatic(ZimbraServlet.class);
        MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class);
        MockedStatic<MailboxManager> mockedMailboxManager = Mockito.mockStatic(MailboxManager.class)) {
      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      // Mock AuthToken and Account
      var authToken = mock(AuthToken.class);
      var account = mock(Account.class);
      var mailbox = mock(Mailbox.class);
      var mailSender = mock(MailSender.class);

      mockedAuthUtil.when(() -> AuthUtil.getAuthTokenFromHttpReq(request, false)).thenReturn(authToken);
      when(authToken.getAccount()).thenReturn(account);
      when(account.getMail()).thenReturn("user@test.com");
      when(account.getDomainName()).thenReturn("test.com");
      when(account.getAttr(ZAttrProvisioning.A_zimbraNewMailNotificationFrom))
          .thenReturn("postmaster@${RECIPIENT_DOMAIN}");

      // Mock MailboxManager and MailSender
      mockedMailboxManager.when(MailboxManager::getInstance).thenReturn(mock(MailboxManager.class));
      when(MailboxManager.getInstance().getMailboxByAccount(account)).thenReturn(mailbox);
      when(mailbox.getMailSender()).thenReturn(mailSender);
      when(mailSender.getCurrentSession()).thenReturn(Session.getDefaultInstance(new Properties()));

      // First request (valid login)
      filter.doFilter(request, response, chain);
      verify(chain, atMostOnce()).doFilter(request, response);

      // Reset mocks for the second request
      Mockito.reset(chain, response);

      // Second request (hijacking attempt with a different IP)
      when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.200");
      filter.doFilter(request, response, chain);

      // Verify the request is blocked
      verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, AUTHENTICATE_AND_TRY_AGAIN);
      verify(chain, never()).doFilter(request, response);
    }
  }

  @Test
  void should_allowRequest_when_noSessionTokenPresent() throws IOException, ServletException, ServiceException {
    // Enable the filter
    provisioning.getConfig().setCarbonioIpBoundSessionFilterEnabled(true);

    // Mock request with no session token
    when(request.getCookies()).thenReturn(null);

    filter.doFilter(request, response, chain);

    // Verify the request is allowed
    verify(chain).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
  }

  @Test
  void should_sendNotificationEmail_when_sessionHijackingDetected() throws Exception {
    // Enable the filter
    provisioning.getConfig().setCarbonioIpBoundSessionFilterEnabled(true);

    var authTokenCookie = new Cookie("ZM_AUTH_TOKEN", "token");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));
    when(request.getCookies()).thenReturn(new Cookie[]{authTokenCookie});
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});
    try (MockedStatic<ZimbraServlet> mockedZimbraServlet = Mockito.mockStatic(ZimbraServlet.class);
        MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class);
        MockedStatic<MailboxManager> mockedMailboxManager = Mockito.mockStatic(MailboxManager.class)) {

      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      var authToken = mock(AuthToken.class);
      var account = mock(Account.class);
      var mailbox = mock(Mailbox.class);
      var mailSender = mock(MailSender.class);

      mockedAuthUtil.when(() -> AuthUtil.getAuthTokenFromHttpReq(request, false)).thenReturn(authToken);
      when(authToken.getAccount()).thenReturn(account);
      when(account.getMail()).thenReturn("user@test.com");
      when(account.getDomainName()).thenReturn("test.com");
      when(account.getAttr(ZAttrProvisioning.A_zimbraNewMailNotificationFrom))
          .thenReturn("postmaster@${RECIPIENT_DOMAIN}");

      mockedMailboxManager.when(MailboxManager::getInstance).thenReturn(mock(MailboxManager.class));
      when(MailboxManager.getInstance().getMailboxByAccount(account)).thenReturn(mailbox);
      when(mailbox.getMailSender()).thenReturn(mailSender);
      when(mailSender.getCurrentSession()).thenReturn(Session.getDefaultInstance(new Properties()));

      // First request (valid login)
      filter.doFilter(request, response, chain);
      verify(chain, atMostOnce()).doFilter(request, response);

      Mockito.reset(chain, response);

      // Second request (hijacking attempt with a different IP)
      when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.200");
      filter.doFilter(request, response, chain);

      // Verify the request is blocked and notification is sent
      verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, AUTHENTICATE_AND_TRY_AGAIN);
      verify(mailSender).sendMimeMessage(any(), eq(mailbox), any(MimeMessage.class));
      verify(chain, never()).doFilter(request, response);
    }
  }

  @Test
  void should_sendNotificationEmail_one_per_mailboxAccount_with_multiple_token_when_sessionHijackingDetected()
      throws Exception {
    // Enable the filter
    provisioning.getConfig().setCarbonioIpBoundSessionFilterEnabled(true);

    // Mock request with multiple session tokens for the same account
    var authTokenCookie1 = new Cookie("ZM_AUTH_TOKEN", "token1");
    var authTokenCookie2 = new Cookie("ZX_AUTH_TOKEN", "token2");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));
    when(request.getCookies()).thenReturn(new Cookie[]{authTokenCookie1, authTokenCookie2});
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});
    try (MockedStatic<ZimbraServlet> mockedZimbraServlet = Mockito.mockStatic(ZimbraServlet.class);
        MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class);
        MockedStatic<MailboxManager> mockedMailboxManager = Mockito.mockStatic(MailboxManager.class)) {

      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      var authToken = mock(AuthToken.class);
      var account = mock(Account.class);
      var mailbox = mock(Mailbox.class);
      var mailSender = mock(MailSender.class);

      mockedAuthUtil.when(() -> AuthUtil.getAuthTokenFromHttpReq(request, false)).thenReturn(authToken);
      when(authToken.getAccount()).thenReturn(account);
      when(account.getMail()).thenReturn("user@test.com");
      when(account.getDomainName()).thenReturn("test.com");
      when(account.getAttr(ZAttrProvisioning.A_zimbraNewMailNotificationFrom))
          .thenReturn("postmaster@${RECIPIENT_DOMAIN}");

      mockedMailboxManager.when(MailboxManager::getInstance).thenReturn(mock(MailboxManager.class));
      when(MailboxManager.getInstance().getMailboxByAccount(account)).thenReturn(mailbox);
      when(mailbox.getMailSender()).thenReturn(mailSender);
      when(mailSender.getCurrentSession()).thenReturn(Session.getDefaultInstance(new Properties()));

      // First request (valid login)
      filter.doFilter(request, response, chain);
      verify(chain, atMostOnce()).doFilter(request, response);

      Mockito.reset(chain, response);

      // Second request (hijacking attempt with a different IP)
      when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.200");
      filter.doFilter(request, response, chain);

      // Verify the request is blocked and only one notification email is sent
      verify(response, times(1)).sendError(HttpServletResponse.SC_FORBIDDEN, AUTHENTICATE_AND_TRY_AGAIN);
      verify(mailSender, times(1)).sendMimeMessage(any(), eq(mailbox), any(MimeMessage.class));
      verify(chain, never()).doFilter(request, response);
    }
  }
}