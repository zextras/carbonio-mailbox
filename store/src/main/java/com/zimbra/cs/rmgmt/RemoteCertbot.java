package com.zimbra.cs.rmgmt;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.mailclient.smtp.SmtpTransport;
import com.zimbra.cs.util.JMSession;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

/**
 * RemoteCertbot class interacts with "Certbot" - an acme client for managing Let’s Encrypt
 * certificates, using {@link com.zimbra.cs.rmgmt.RemoteManager}.
 *
 * @author Yuliya Aheeva
 * @since 23.3.0
 */
public class RemoteCertbot {
  private static final String CHAIN = "--preferred-chain";
  private static final String SHORT_CHAIN = "\"ISRG Root X1\"";
  private static final String CHAIN_TYPE = "short";
  private static final String WEBROOT = "--webroot -w";
  private static final String WEBROOT_PATH = "/opt/zextras";
  private static final String AGREEMENT = "--agree-tos";
  private static final String EMAIL = "--email";
  private static final String NON_INTERACTIVELY = "-n";
  private static final String CERT_NAME = "--cert-name";
  private static final String KEEP = "--keep";
  // Domain names to include. You can use multiple values with -d flags.
  // The first value will be used as Subject Name on the certificate and all the values will be
  // included as Subject Alternative Names.
  private static final String D = " -d ";

  private final RemoteManager remoteManager;
  private StringBuilder stringBuilder;

  public RemoteCertbot(RemoteManager remoteManager) {
    this.remoteManager = remoteManager;
  }

  /**
   * Creates a command to be executed by the Certbot acme client.
   * E.g. certbot certonly --agree-tos --email admin@test.com -n --webroot -w /opt/zextras
   * --cert-name demo.zextras.io -d acme.demo.zextras.io -d webmail-acme.demo.zextras.io
   *
   * @param remoteCommand {@link com.zimbra.cs.rmgmt.RemoteCommands}
   * @param email domain admin email who tries to execute a command (should be agreed to the
   *  ACME server's Subscriber Agreement)
   * @param chain long (default) or short (should be specified by domain admin in {@link
   *  com.zimbra.soap.admin.message.IssueCertRequest} request with the key word "short")
   * @param domainName a value of domain attribute zimbraDomainName
   * @param publicServiceHostName a value of domain attribute zimbraPublicServiceHostname
   * @param virtualHosts a value/ values of domain attribute zimbraVirtualHostname
   * @return created command
   */
  public String createCommand(String remoteCommand, String email, String chain,
      String domainName, String publicServiceHostName, String[] virtualHosts) {

    this.stringBuilder = new StringBuilder();

    stringBuilder.append(remoteCommand);

    if (Objects.equals(chain, CHAIN_TYPE)) {
      addSubCommand(" ", CHAIN, SHORT_CHAIN);
    }

    addSubCommand(" ", AGREEMENT, EMAIL, email, NON_INTERACTIVELY, KEEP,
        WEBROOT, WEBROOT_PATH, CERT_NAME, domainName);

    addSubCommand(D, publicServiceHostName);
    addSubCommand(D, virtualHosts);

    return stringBuilder.toString();
  }

  /**
   * Executes a command asynchronously and notifies domain recipients about the command execution.
   *
   * @param domain domain
   * @param command a command to be executed
   */
  public void supplyAsync(Domain domain, String command) {
    CompletableFuture.supplyAsync(() -> execute(command))
        .thenAccept(message -> notify(domain, message));
  }

  /**
   * Executes a command using {@link com.zimbra.cs.rmgmt.RemoteManager}.
   * @param command a command to be executed
   * @return a sting message of successful remote execution or detailed exception message
   * in case of failure.
   */
  private String execute(String command) {
    try {
      RemoteResult remoteResult = remoteManager.execute(command);
      return new String(remoteResult.getMStdout(), StandardCharsets.UTF_8);
    } catch (ServiceException e) {
      return e.getMessage();
    }
  }

  private void addSubCommand(String delimiter, String... params) {
    for (String param : params) {
      this.stringBuilder.append(delimiter).append(param);
    }
  }

  private void notify(Domain domain, String message) {
    ZimbraLog.rmgmt.info(
        "Issuing LetsEncrypt cert command for domain " + domain.getName()
            + " was finished with the following result: " + message);
    try {
      String from = Optional.ofNullable(domain.getCarbonioNotificationFrom())
          .orElseThrow(() -> ServiceException.FAILURE("no from", null));
      String[] to = Optional.ofNullable(domain.getCarbonioNotificationRecipients())
          .orElseThrow(() -> ServiceException.FAILURE("no to", null));
      String subject = "Let's Encrypt Certificate generation request";
      String hostname = "127.78.0.7";

      Session session = JMSession.getSmtpSession(domain);
      session.getProperties().setProperty("mail.smtp.host", hostname);
      session.getProperties().setProperty("mail.smtp.from", "zextras@demo.zextras.io");

      MimeMessage mm = new MimeMessage(session);
      mm.setText(message);
      mm.setRecipients(RecipientType.TO, "zextras@demo.zextras.io");
      mm.setSubject(subject);
      mm.setFrom();
      mm.saveChanges();

      sendMessage(session, mm);

    } catch (Exception e) {
      ZimbraLog.rmgmt.info("Notification about LetsEncrypt certificate generation wasn't sent "
          + "for the domain " + domain.getName() + ". Sending failure: " + e.getMessage());
    }
  }

  private void sendMessage(Session mSession, MimeMessage mm) throws MessagingException {
    URLName urlName = new URLName("smtp", null, -1, null, null, null);
    SmtpTransport transport = new SmtpTransport(mSession, urlName);
    Address[] rcptAddresses = mm.getAllRecipients();

    try {
      transport.connect();
      transport.sendMessage(mm, rcptAddresses);
    } finally {
      transport.close();
    }
  }
}
