package com.zimbra.cs.servlet;

import com.zimbra.common.util.RemoteIP;
import com.zimbra.common.util.ZimbraLog;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class SessionHijackingPreventionFilter implements Filter {

  // List of session token names to check, we may pass this in configuration
  private static final List<String> SESSION_TOKENS = Arrays.asList("ZM_AUTH_TOKEN", "ZM_ADMIN_AUTH_TOKEN");

  // Map to track session tokens and their associated IPs
  private final Map<String, String> sessionIpMap = new ConcurrentHashMap<>();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // not yet
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    var trustedIPs = ZimbraServlet.getTrustedIPs();
    var remoteIP = new RemoteIP(httpRequest, trustedIPs);
    var clientIP = remoteIP.getOrigIP();

    for (String tokenName : SESSION_TOKENS) {
      String tokenValue = getSessionToken(httpRequest, tokenName);

      if (tokenValue != null && !validateSessionToken(tokenValue, clientIP)) {
        remoteIP.addToLoggingContext();
        ZimbraLog.misc.warn("Session hijacking attempt detected! Used session token: %s", tokenName);
        ZimbraLog.clearContext();

        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Session hijacking attempt detected");
        return;
      }
    }

    // Pass the request through,if everything is fine
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    sessionIpMap.clear();
  }

  private boolean validateSessionToken(String token, String currentIp) {
    return sessionIpMap.computeIfAbsent(token, key -> currentIp).equals(currentIp);
  }

  private String getSessionToken(HttpServletRequest request, String tokenName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (tokenName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
