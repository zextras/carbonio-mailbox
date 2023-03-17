package com.zimbra.cs.rmgmt;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.mail.Message.RecipientType;
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
   * @author Yuliya Aheeva
   * @since 23.4.0
   */
  public void supplyAsync(Mailbox mbox, Domain domain, String command) {
    CompletableFuture.supplyAsync(() -> execute(command))
        .thenAccept(message -> notify(mbox, domain, message));
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

  /**
   * Notifies domain recipients about certificate generation request
   * @param mbox
   * @param domain
   * @param message
   * @author Yuliya Aheeva
   * @since 23.4.0
   */
  private void notify(Mailbox mbox, Domain domain, String message) {
    ZimbraLog.rmgmt.info(
        "Issuing LetsEncrypt cert command for domain " + domain.getName()
            + " was finished with the following result: " + message);
    try {
      String from = Optional.ofNullable(domain.getCarbonioNotificationFrom())
          .orElseThrow(() -> ServiceException.FAILURE("no from", null));
      String[] to = Optional.ofNullable(domain.getCarbonioNotificationRecipients())
          .orElseThrow(() -> ServiceException.FAILURE("no to", null));
      String subject = "Let's Encrypt Certificate generation request";

      MailSender sender = mbox.getMailSender(domain);

      MimeMessage mm = new MimeMessage(sender.getCurrentSession());
      mm.setText(message);
      mm.setRecipients(RecipientType.TO, "zextras@demo.zextras.io");
      mm.setSubject(subject);
      mm.setFrom();
      mm.saveChanges();

      sender.sendMimeMessage(mbox, mm);

    } catch (Exception e) {
      ZimbraLog.rmgmt.info("Notification about LetsEncrypt certificate generation wasn't sent "
          + "for the domain " + domain.getName() + ". Sending failure: " + e.getMessage());
    }
  }
}
