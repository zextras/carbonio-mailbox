package com.zimbra.cs.servlet;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.common.util.RemoteIP.TrustedIPs;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.servlet.RequestMetadataUtil.RequestMetadataAsStringBuilder;
import com.zimbra.cs.servlet.util.AuthUtil;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IpBoundSessionFilter implements Filter {

  public static final String AUTHENTICATE_AND_TRY_AGAIN = "Invalid use of session token, Re-Authenticate and try again!";
  private static final List<String> SESSION_TOKENS = Arrays.asList("ZM_AUTH_TOKEN", "ZM_ADMIN_AUTH_TOKEN",
      "ZX_AUTH_TOKEN");
  private final Map<String, String> sessionIpMap = new ConcurrentHashMap<>();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization logic (if needed)
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      if (!isFilterEnabled()) {
        chain.doFilter(request, response);
        return;
      }

      filter(request, response, chain);
    } catch (ServiceException e) {
      ZimbraLog.misc.warn("Failed to process request: %s", e.getMessage());
      ((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void filter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final var httpServletRequest = (HttpServletRequest) request;
    final var httpServletResponse = (HttpServletResponse) response;
    final Map<String, String> sessionTokensInRequest = getSessionTokensInRequest(httpServletRequest);
    if (sessionTokensInRequest.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }

    final List<Tuple2<String, AccountMailbox>> hijackingAttempts = checkAllSessionTokens(httpServletRequest,
        sessionTokensInRequest);

    if (!hijackingAttempts.isEmpty()) {
      logAndNotifyHijackingAttempts(hijackingAttempts, request);
      httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
          AUTHENTICATE_AND_TRY_AGAIN);
      return;
    }

    chain.doFilter(request, response);
  }

  private List<Tuple2<String, AccountMailbox>> checkAllSessionTokens(HttpServletRequest request,
      Map<String, String> sessionTokens) {

    return sessionTokens.entrySet().stream()
        .flatMap(entry -> {
          String tokenName = entry.getKey();
          String tokenValue = entry.getValue();

          return Try.of(() -> {
                boolean isAdminRequest = "ZM_ADMIN_AUTH_TOKEN".equals(tokenName);
                AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(request, isAdminRequest);

                if (authToken != null) {
                  AccountMailbox accountAndMailbox = getAccountAndMailboxFromAuthToken(authToken);
                  String clientOriginalIP = getClientOriginalIP(request, accountAndMailbox.account());

                  if (!validateSessionToken(tokenValue, clientOriginalIP)) {
                    String messageTitle = "Session hijacking attempt detected for token: " + tokenName +
                        " from IP: " + clientOriginalIP;
                    return Option.of(Tuple.of(messageTitle, accountAndMailbox));
                  }
                }
                return Option.<Tuple2<String, AccountMailbox>>none();
              })
              .onFailure(e -> ZimbraLog.misc.warn("Failed to validate token %s: %s", tokenName, e.getMessage()))
              .getOrElse(Option.none())
              .toJavaStream();
        })
        .toList();
  }

  private boolean validateSessionToken(String token, String currentIp) {
    return sessionIpMap.computeIfAbsent(token, key -> currentIp).equals(currentIp);
  }

  private void logAndNotifyHijackingAttempts(List<Tuple2<String, AccountMailbox>> hijackingAttempts,
      ServletRequest request) {
    if (!hijackingAttempts.isEmpty()) {
      String requestMetadata = new RequestMetadataAsStringBuilder((HttpServletRequest) request)
          .withUrl()
          .withQueryString()
          .withMethod()
          .withHeaders()
          .build();

      Map<AccountMailbox, List<String>> attemptsByAccount = hijackingAttempts.stream()
          .collect(Collectors.groupingBy(
              attempt -> attempt._2,
              Collectors.mapping(
                  attempt -> attempt._1,
                  Collectors.toList()
              )
          ));

      attemptsByAccount.forEach((accountMailbox, messages) -> {
        String combinedMessage = String.join("\n\n", messages);
        ZimbraLog.misc.warn("[SECURITY ALERT] Account %s : %s", accountMailbox.account().getName(),
            combinedMessage + "\nREQUEST DETAILS:\n" + requestMetadata);
        String fullMessage = combinedMessage + "\n\n\nREQUEST DETAILS:\n\n" + requestMetadata;
        sendNotificationEmail(accountMailbox, fullMessage);
      });
    }
  }

  private void sendNotificationEmail(AccountMailbox accountMailbox, String message) {
    Try.run(() -> {
      MimeMessage notificationMessage = createNotificationMessage(accountMailbox, message);
      accountMailbox.mailSender().sendMimeMessage(
          new OperationContext(accountMailbox.mailboxByAccount()),
          accountMailbox.mailboxByAccount(),
          notificationMessage
      );
    }).onFailure(e -> ZimbraLog.misc.warn("Failed to send notification email: %s", e.getMessage()));
  }

  private MimeMessage createNotificationMessage(AccountMailbox accountMailbox, String message)
      throws MessagingException {
    MimeMessage mimeMessage = new MimeMessage(accountMailbox.mailSender().getCurrentSession());
    mimeMessage.setFrom(new InternetAddress(accountMailbox.account().getMail()));
    mimeMessage.setSender(new InternetAddress(getPostmasterAddress(accountMailbox.account())));
    mimeMessage.setRecipients(RecipientType.TO, accountMailbox.account().getMail());
    mimeMessage.setSubject("Session hijacking attempt detected");
    mimeMessage.setText(message);
    mimeMessage.saveChanges();
    return mimeMessage;
  }

  private boolean isFilterEnabled() throws ServiceException {
    return Provisioning.getInstance().getConfig().isCarbonioIpBoundSessionFilterEnabled();
  }

  private String getClientOriginalIP(HttpServletRequest request, Account account) {
    var trustedIPs = getAllTrustedIPs(account, ZimbraServlet.getTrustedIPs());
    return new RemoteIP(request, trustedIPs).getOrigIP();
  }

  private String getPostmasterAddress(Account account) {
    var postmaster = account.getAttr(ZAttrProvisioning.A_zimbraNewMailNotificationFrom);
    var vars = Map.of("RECIPIENT_DOMAIN", account.getDomainName());
    return StringUtil.fillTemplate(postmaster, vars);
  }

  private Option<String> getSessionToken(HttpServletRequest request, String tokenName) {
    return Option.ofOptional(Optional.ofNullable(request.getCookies())
        .flatMap(cookies -> Arrays.stream(cookies)
            .filter(cookie -> tokenName.equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)));
  }

  private Map<String, String> getSessionTokensInRequest(HttpServletRequest request) {
    return SESSION_TOKENS.stream()
        .map(tokenName -> Tuple.of(tokenName, getSessionToken(request, tokenName)))
        .filter(tuple -> tuple._2.isDefined())
        .collect(Collectors.toMap(
            tuple -> tuple._1, // Token name
            tuple -> tuple._2.get() // Token value
        ));
  }

  private AccountMailbox getAccountAndMailboxFromAuthToken(AuthToken authToken) throws ServiceException {
    var account = authToken.getAccount();
    var mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    var mailSender = mailbox.getMailSender();
    return new AccountMailbox(account, mailbox, mailSender);
  }

  private TrustedIPs getAllTrustedIPs(Account account, TrustedIPs existingTrustedIPs) {
    List<String> trustedIPs = new ArrayList<>();
    if (existingTrustedIPs != null) {
      trustedIPs.addAll(existingTrustedIPs.getTrustedIPs());
    }
    trustedIPs.addAll(Arrays.asList(account.getCarbonioIpBoundSessionTrustedIPs()));
    return new TrustedIPs(trustedIPs.toArray(new String[0]));
  }

  @Override
  public void destroy() {
    sessionIpMap.clear();
  }

  private record AccountMailbox(Account account, Mailbox mailboxByAccount, MailSender mailSender) {

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      AccountMailbox that = (AccountMailbox) o;
      return account.getMail().equalsIgnoreCase(that.account.getMail());
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(account.getMail().toLowerCase());
    }
  }
}