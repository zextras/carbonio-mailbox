package com.zimbra.cs.servlet;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.servlet.util.AuthUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
    var httpRequest = (HttpServletRequest) request;
    var httpResponse = (HttpServletResponse) response;

    var trustedIPs = ZimbraServlet.getTrustedIPs();
    var remoteIP = new RemoteIP(httpRequest, trustedIPs);
    var clientIP = remoteIP.getOrigIP();

    for (var tokenName : SESSION_TOKENS) {
      var tokenValue = getSessionToken(httpRequest, tokenName);

      if (tokenValue != null && !validateSessionToken(tokenValue, clientIP)) {
        remoteIP.addToLoggingContext();
        ZimbraLog.misc.warn("Session hijacking attempt detected! Used session token: %s", tokenName);
        ZimbraLog.clearContext();

        notifyUser(httpRequest, tokenName, clientIP);
        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Session hijacking attempt detected");
        return;
      }
    }

    // Pass the request through,if everything is fine
    chain.doFilter(request, response);
  }

  private void notifyUser(HttpServletRequest request, String tokenName, String clientIP) {
    var isAdminRequest = "ZM_ADMIN_AUTH_TOKEN".equals(tokenName);
    final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, isAdminRequest);
    if (authToken != null) {
      try {
        var account = authToken.getAccount();
        var mailboxByAccount = MailboxManager.getInstance().getMailboxByAccount(account);
        var mailSender = mailboxByAccount.getMailSender();
        var notificationMessage = getNotificationMessage(account, mailSender, clientIP);
        if (notificationMessage != null) {
          mailSender.sendMimeMessage(new OperationContext(mailboxByAccount), mailboxByAccount,
              notificationMessage);
        }
      } catch (ServiceException e) {
        ZimbraLog.misc.warn("Failed to send notification email: %s", e.getMessage());
      }
    }
  }

  private MimeMessage getNotificationMessage(Account senderAccount, MailSender sender, String clientIP) {
    var session = sender.getCurrentSession();
    var mimeMessage = new MimeMessage(session);
    try {
      mimeMessage.setFrom(new InternetAddress(senderAccount.getMail()));
      var postmaster = senderAccount.getAttr(ZAttrProvisioning.A_zimbraNewMailNotificationFrom);
      Map<String, String> vars = new HashMap<>();
      vars.put("RECIPIENT_DOMAIN", senderAccount.getDomainName());
      postmaster = StringUtil.fillTemplate(postmaster, vars);
      mimeMessage.setSender(new InternetAddress(postmaster));
      mimeMessage.setRecipients(RecipientType.TO, new InternetAddress[]{new InternetAddress(senderAccount.getMail())});
      mimeMessage.setSubject("Session hijacking attempt detected");
      mimeMessage.setText("Session hijacking attempt detected from IP: " + clientIP);
      mimeMessage.saveChanges();
    } catch (MessagingException e) {
      return null;
    }
    return mimeMessage;
  }

  @Override
  public void destroy() {
    sessionIpMap.clear();
  }

  private boolean validateSessionToken(String token, String currentIp) {
    return sessionIpMap.computeIfAbsent(token, key -> currentIp).equals(currentIp);
  }

  private String getSessionToken(HttpServletRequest request, String tokenName) {
    var cookies = request.getCookies();
    if (cookies != null) {
      for (var cookie : cookies) {
        if (tokenName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
