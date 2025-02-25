package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("HttpUrlsUsage")
class RequestMetadataUtilTest {

  private HttpServletRequest request;
  private HttpSession session;

  @BeforeEach
  public void setUp() {
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
  }

  @AfterEach
  public void tearDown() {
    request = null;
    session = null;
  }

  @Test
  void getRequestMetadataAsString_withAllMetadata() {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com"));
    when(request.getQueryString()).thenReturn("param1=value1&param2=value2");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("User-Agent")));
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    when(request.getCookies()).thenReturn(
        new javax.servlet.http.Cookie[]{new javax.servlet.http.Cookie("session", "12345")});
    when(request.getParameterMap()).thenReturn(Collections.singletonMap("param", new String[]{"value"}));
    when(request.getSession(false)).thenReturn(session);
    when(session.getId()).thenReturn("ABC123");
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(Collections.singletonList("attr")));
    when(session.getAttribute("attr")).thenReturn("value");

    String metadata = new RequestMetadataUtil.RequestMetadataAsStringBuilder(request)
        .withMethod()
        .withUrl()
        .withQueryString()
        .withRemoteIp()
        .withHeaders()
        .withCookies()
        .withParameters()
        .withSession()
        .build();

    assertEquals("""
        Request Method: GET
        Request URL: http://example.com
        Query String: param1=value1&param2=value2
        Remote IP: 127.0.0.1
        Headers:
          User-Agent: Mozilla/5.0
        Cookies:
          session: 12345
        Parameters:
          param: value
        Session Information:
          Session ID: ABC123
          attr: value
        """, metadata);
  }

  @Test
  void getRequestMetadataAsString_withNoQueryString() {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com"));
    when(request.getQueryString()).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("User-Agent")));
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    when(request.getCookies()).thenReturn(
        new javax.servlet.http.Cookie[]{new javax.servlet.http.Cookie("session", "12345")});
    when(request.getParameterMap()).thenReturn(Collections.singletonMap("param", new String[]{"value"}));
    when(request.getSession(false)).thenReturn(session);
    when(session.getId()).thenReturn("ABC123");
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(Collections.singletonList("attr")));
    when(session.getAttribute("attr")).thenReturn("value");

    String metadata = new RequestMetadataUtil.RequestMetadataAsStringBuilder(request)
        .withMethod()
        .withUrl()
        .withRemoteIp()
        .withHeaders()
        .withCookies()
        .withParameters()
        .withSession()
        .build();

    assertEquals("""
        Request Method: POST
        Request URL: http://example.com
        Remote IP: 127.0.0.1
        Headers:
          User-Agent: Mozilla/5.0
        Cookies:
          session: 12345
        Parameters:
          param: value
        Session Information:
          Session ID: ABC123
          attr: value
        """, metadata);
  }

  @Test
  void getRequestMetadataAsString_withNoCookies() {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com"));
    when(request.getQueryString()).thenReturn("param1=value1&param2=value2");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("User-Agent")));
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    when(request.getCookies()).thenReturn(null);
    when(request.getParameterMap()).thenReturn(Collections.singletonMap("param", new String[]{"value"}));
    when(request.getSession(false)).thenReturn(session);
    when(session.getId()).thenReturn("ABC123");
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(Collections.singletonList("attr")));
    when(session.getAttribute("attr")).thenReturn("value");

    String metadata = new RequestMetadataUtil.RequestMetadataAsStringBuilder(request)
        .withMethod()
        .withUrl()
        .withQueryString()
        .withRemoteIp()
        .withHeaders()
        .withCookies()
        .withParameters()
        .withSession()
        .build();

    assertEquals("""
        Request Method: GET
        Request URL: http://example.com
        Query String: param1=value1&param2=value2
        Remote IP: 127.0.0.1
        Headers:
          User-Agent: Mozilla/5.0
        Cookies:
          No cookies
        Parameters:
          param: value
        Session Information:
          Session ID: ABC123
          attr: value
        """, metadata);
  }

  @Test
  void getRequestMetadataAsString_withNoSession() {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com"));
    when(request.getQueryString()).thenReturn("param1=value1&param2=value2");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("User-Agent")));
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    when(request.getCookies()).thenReturn(
        new javax.servlet.http.Cookie[]{new javax.servlet.http.Cookie("session", "12345")});
    when(request.getParameterMap()).thenReturn(Collections.singletonMap("param", new String[]{"value"}));
    when(request.getSession(false)).thenReturn(null);

    String metadata = new RequestMetadataUtil.RequestMetadataAsStringBuilder(request)
        .withMethod()
        .withUrl()
        .withQueryString()
        .withRemoteIp()
        .withHeaders()
        .withCookies()
        .withParameters()
        .withSession()
        .build();

    assertEquals("""
        Request Method: GET
        Request URL: http://example.com
        Query String: param1=value1&param2=value2
        Remote IP: 127.0.0.1
        Headers:
          User-Agent: Mozilla/5.0
        Cookies:
          session: 12345
        Parameters:
          param: value
        Session Information:
          No session
        """, metadata);
  }
}