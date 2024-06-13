package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Provisioning;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoSFilterTest {

  public static final String WHITELISTED_GET_FOLDER_REQUEST = "GetFolderRequest";
  public static final String NON_WHITELISTED_REQUEST = "a_non_whitelisted_request";
  private static String mailboxSrv1Ip;
  private static String mailboxSrv1Hostname;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain chain;
  @Mock
  private FilterConfig filterConfig;

  @BeforeAll
  static void init() throws Exception {
    MailboxTestUtil.setUp();

    mockStatic(InetAddress.class);
    var mockInetAddress = mock(InetAddress.class);
    mailboxSrv1Ip = "178.4.5.345";
    when(mockInetAddress.getHostAddress()).thenReturn(mailboxSrv1Ip);
    mailboxSrv1Hostname = "google.com";
    when(InetAddress.getByName(mailboxSrv1Hostname)).thenReturn(mockInetAddress);
    when(InetAddress.getAllByName(mailboxSrv1Hostname)).thenReturn(new InetAddress[]{mockInetAddress});

    Provisioning.getInstance().createServer(
        "mailbox_srv1",
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_cn, "mailbox_srv1");
            put(ZAttrProvisioning.A_zimbraServiceHostname, mailboxSrv1Hostname);
            put(ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_MAILBOX);
          }
        });
  }

  @AfterAll
  static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void should_whitelist_ip_of_mailbox_nodes() throws Exception {
    var doSFilter = spy(DoSFilter.class);
    doSFilter.init(filterConfig);

    assertTrue(doSFilter.getWhitelist().contains(mailboxSrv1Ip));

    assertFalse(doSFilter.getWhitelistedRequests().contains(NON_WHITELISTED_REQUEST));
    when(request.getRequestURI()).thenReturn(NON_WHITELISTED_REQUEST);
    when(request.getRemoteAddr()).thenReturn(mailboxSrv1Ip);

    doSFilter.doFilter(request, response, chain);

    //request not filtered out by whiteListedRequests filter
    verify(doSFilter, times(0)).skipDosFilter(request, response, chain);

    //request allowed
    verify(doSFilter, times(1)).doFilter(request, response, chain);
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void should_filter_out_requests_which_are_whitelisted_and_authenticated() throws Exception {
    var doSFilter = spy(DoSFilter.class);
    doSFilter.init(filterConfig);

    assertTrue(doSFilter.getWhitelistedRequests().contains(WHITELISTED_GET_FOLDER_REQUEST));
    when(request.getRequestURI()).thenReturn(WHITELISTED_GET_FOLDER_REQUEST);
    when(doSFilter.extractUserId(request)).thenReturn("4552");

    doSFilter.doFilter(request, response, chain);

    verify(doSFilter, times(1)).skipDosFilter(request, response, chain);
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void should_not_allow_requests_which_are_whitelisted_but_not_authenticated() throws Exception {
    var doSFilter = spy(DoSFilter.class);
    doSFilter.init(filterConfig);

    assertTrue(doSFilter.getWhitelistedRequests().contains(WHITELISTED_GET_FOLDER_REQUEST));
    when(request.getRequestURI()).thenReturn(WHITELISTED_GET_FOLDER_REQUEST);
    when(request.getRemoteAddr()).thenReturn("localhost");
    when(doSFilter.extractUserId(request)).thenReturn(null);

    doSFilter.doFilter(request, response, chain);

    verify(doSFilter, times(0)).skipDosFilter(request, response, chain);
    verify(doSFilter, times(1)).doFilter(request, response, chain);
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void should_allow_whitelisted_ip_addresses() throws Exception {
    var throttleSafeIp = "192.0.2.146";
    var clientIp = "178.3.6.120";
    Provisioning.getInstance().getLocalServer().setHttpThrottleSafeIPs(new String[]{throttleSafeIp});

    var doSFilter = spy(DoSFilter.class);
    doSFilter.init(filterConfig);
    doSFilter.addWhitelistAddress(clientIp);

    //IPs were added to be whitelisted
    assertTrue(doSFilter.getWhitelist().contains(clientIp));
    assertTrue(doSFilter.getWhitelist().contains(throttleSafeIp));

    when(request.getRemoteAddr()).thenReturn(clientIp);
    var nonWhiteListedUri = NON_WHITELISTED_REQUEST;
    assertFalse(doSFilter.getWhitelistedRequests().contains(nonWhiteListedUri));
    when(request.getRequestURI()).thenReturn(nonWhiteListedUri);

    doSFilter.doFilter(request, response, chain);

    //request not filtered out by whiteListedRequests filter
    verify(doSFilter, times(0)).skipDosFilter(request, response, chain);

    //request allowed
    verify(doSFilter, times(1)).doFilter(request, response, chain);
    verify(chain, times(1)).doFilter(request, response);
  }


  @Test
  void should_not_filter_out_requests_when_they_are_not_whitelisted() throws Exception {
    var doSFilter = spy(DoSFilter.class);
    doSFilter.init(filterConfig);

    when(request.getRequestURI()).thenReturn(NON_WHITELISTED_REQUEST);
    when(request.getRemoteAddr()).thenReturn("localhost");
    when(doSFilter.extractUserIdOptional(any())).thenReturn(Optional.of("23124"));

    doSFilter.doFilter(request, response, chain);

    verify(doSFilter, times(0)).skipDosFilter(request, response, chain);
    verify(doSFilter, times(1)).doFilter(request, response, chain);
  }

  @Test
  void should_not_filter_out_requests_which_are_whitelisted_but_not_authenticated()
      throws Exception {
    var doSFilter = spy(DoSFilter.class);
    doSFilter.init(filterConfig);

    assertTrue(doSFilter.getWhitelistedRequests().contains(WHITELISTED_GET_FOLDER_REQUEST));
    when(request.getRequestURI()).thenReturn(WHITELISTED_GET_FOLDER_REQUEST);
    when(doSFilter.extractUserId(request)).thenReturn("");

    doSFilter.doFilter(request, response, chain);

    verify(doSFilter, times(0)).skipDosFilter(request, response, chain);
    verify(doSFilter, times(1)).doFilter(request, response, chain);
  }
}
