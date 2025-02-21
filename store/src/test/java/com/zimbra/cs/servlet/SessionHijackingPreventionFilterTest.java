package com.zimbra.cs.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionHijackingPreventionFilterTest {

  @InjectMocks
  private SessionHijackingPreventionFilter filter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;

  @BeforeAll
  static void init() throws Exception {
    MailboxTestUtil.setUp();
  }

  @AfterAll
  static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void should_allowRequest_when_request_contains_validSessionToken_and_No_hiJackingDetected()
      throws IOException, ServletException {
    var authToken = new Cookie("ZM_AUTH_TOKEN", "token");
    when(request.getCookies()).thenReturn(new Cookie[]{authToken});
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});
    try (var mockedZimbraServlet = mockStatic(ZimbraServlet.class)) {
      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      filter.doFilter(request, response, chain);

      verify(chain).doFilter(request, response);
      verify(response, never()).sendError(anyInt(), anyString());
    }
  }

  @Test
  void should_blockRequest_when_sessionHijackingDetected() throws IOException, ServletException {
    var authToken = new Cookie("ZM_AUTH_TOKEN", "token");
    when(request.getCookies()).thenReturn(new Cookie[]{authToken});
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});
    try (var mockedZimbraServlet = mockStatic(ZimbraServlet.class)) {
      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      // First request: Allowed
      filter.doFilter(request, response, chain);
      verify(chain, atMostOnce()).doFilter(request, response);

      Mockito.reset(chain, response);

      // Second request (different IP - hijacking attempt simulation)
      when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.200");
      filter.doFilter(request, response, chain);

      // Ensure the request was blocked
      verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Session hijacking attempt detected");
      verify(chain, never()).doFilter(request, response);
    }
  }

  @Test
  void should_allowRequest_when_noSessionTokenPresent_in_the_request() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(null);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
  }

  @Test
  void should_sendNotificationEmail_when_sessionHijackingDetected() throws Exception {
    var authTokenCookie = new Cookie("ZM_AUTH_TOKEN", "token");
    when(request.getCookies()).thenReturn(new Cookie[]{authTokenCookie});
    when(request.getRemoteAddr()).thenReturn("127.0.0.1"); // Initial login IP
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

    var trustedIPs = new RemoteIP.TrustedIPs(new String[]{"192.168.1.1", "127.0.0.1"});

    try (var mockedZimbraServlet = mockStatic(ZimbraServlet.class)) {
      mockedZimbraServlet.when(ZimbraServlet::getTrustedIPs).thenReturn(trustedIPs);

      // First request (valid login)
      filter.doFilter(request, response, chain);
      verify(chain, atMostOnce()).doFilter(request, response);

      Mockito.reset(chain, response);

      // Second request (different IP - hijacking attempt simulation)
      when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.200");

      try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
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

        try (var mockedMailboxManager = mockStatic(MailboxManager.class)) {
          mockedMailboxManager.when(MailboxManager::getInstance).thenReturn(mock(MailboxManager.class));
          when(MailboxManager.getInstance().getMailboxByAccount(account)).thenReturn(mailbox);
          when(mailbox.getMailSender()).thenReturn(mailSender);
          when(mailSender.getCurrentSession()).thenReturn(Session.getDefaultInstance(new Properties()));

          // Trigger hijacking detection
          filter.doFilter(request, response, chain);

          // Verify response is forbidden and notification is sent
          verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Session hijacking attempt detected");
          verify(chain, never()).doFilter(request, response);
          verify(mailSender).sendMimeMessage(any(), eq(mailbox), any(MimeMessage.class));
        }
      }
    }
  }

}