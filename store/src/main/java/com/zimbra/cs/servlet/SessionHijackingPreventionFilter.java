package com.zimbra.cs.servlet;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
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
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

        notifyUser(httpRequest, tokenName, clientIP);
        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Session hijacking attempt detected");
        return;
      }
    }

    // Pass the request through,if everything is fine
    chain.doFilter(request, response);
  }

  private void notifyUser(HttpServletRequest request, String tokenName, String clientIP) {
    boolean isAdminRequest = "ZM_ADMIN_AUTH_TOKEN".equals(tokenName);
    final AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(request, isAdminRequest);
    if (authToken != null) {
      try {
        Account account = authToken.getAccount();
        Mailbox mailboxByAccount = MailboxManager.getInstance().getMailboxByAccount(account);
        MailSender mailSender = mailboxByAccount.getMailSender();
        MimeMessage notificationMessage = getNotificationMessage(account, mailSender, clientIP);
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
    Session session = sender.getCurrentSession();
    MimeMessage mm = new MimeMessage(session);
    try {
      mm.setFrom(new InternetAddress(senderAccount.getMail()));
      String postmaster = senderAccount.getAttr(ZAttrProvisioning.A_zimbraNewMailNotificationFrom);
      Map<String, String> vars = new HashMap<>();
      vars.put("RECIPIENT_DOMAIN", senderAccount.getDomainName());
      postmaster = StringUtil.fillTemplate(postmaster, vars);
      mm.setSender(new InternetAddress(postmaster));
      mm.setRecipients(RecipientType.TO, new InternetAddress[]{new InternetAddress(senderAccount.getMail())});
      mm.setSubject("Session hijacking attempt detected");
      mm.setText("Session hijacking attempt detected from IP: " + clientIP);
      mm.saveChanges();
    } catch (MessagingException e) {
      return null;
    }
    return mm;
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
