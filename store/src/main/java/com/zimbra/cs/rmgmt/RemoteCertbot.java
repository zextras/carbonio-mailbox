package com.zimbra.cs.rmgmt;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.service.admin.CertificateNotificationManager;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * RemoteCertbot class interacts with "Certbot" - an acme client for managing Let’s Encrypt
 * certificates, using {@link com.zimbra.cs.rmgmt.RemoteManager}.
 *
 * @author Yuliya Aheeva
 * @since 23.3.0
 */
public class RemoteCertbot {
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

  private RemoteCertbot(RemoteManager remoteManager) {
    this.remoteManager = remoteManager;
  }

  /**
   * Creates a command to be executed by the Certbot acme client.
   *
   * <p>E.g. certbot certonly --agree-tos --email admin@test.com -n --webroot -w /opt/zextras
   * --cert-name demo.zextras.io -d acme.demo.zextras.io -d webmail-acme.demo.zextras.io
   *
   * @param remoteCommand {@link com.zimbra.cs.rmgmt.RemoteCommands}
   * @param email domain admin email who tries to execute a command (should be agreed to the ACME
   *     server's Subscriber Agreement)
   * @param domainName a value of domain attribute zimbraDomainName
   * @param publicServiceHostName a value of domain attribute zimbraPublicServiceHostname
   * @param virtualHosts a value/ values of domain attribute zimbraVirtualHostname
   * @return created command
   */
  public String createCommand(String remoteCommand, String email, String domainName,
      String publicServiceHostName, String[] virtualHosts) {

    this.stringBuilder = new StringBuilder();

    stringBuilder.append(remoteCommand);

    addSubCommand(" ", AGREEMENT, EMAIL, email, NON_INTERACTIVELY, KEEP,
        WEBROOT, WEBROOT_PATH, CERT_NAME, domainName);

    addSubCommand(D, publicServiceHostName);
    addSubCommand(D, virtualHosts);

    return stringBuilder.toString();
  }

  /**
   * Executes a command asynchronously and notifies global and domain recipients about the command
   * execution using {@link com.zimbra.cs.service.admin.CertificateNotificationManager}.
   *
   * @param notificationManager an object of {@link com.zimbra.cs.service.admin.CertificateNotificationManager}
   * @param command a Certbot command to be executed remotely
   *
   * @author Yuliya Aheeva
   * @since 23.5.0
   */
  public void supplyAsync(CertificateNotificationManager notificationManager, String command) {
    CompletableFuture.supplyAsync(() -> execute(command))
        .thenApply(notificationManager::createIssueCertNotificationMap)
        .thenAccept(notificationManager::notify);
  }

  /**
   * Executes a command using {@link com.zimbra.cs.rmgmt.RemoteManager}.
   *
   * @param command a command to be executed
   * @return a sting message of successful remote execution or detailed exception message in case of
   *     failure.
   */
  public String execute(String command) {
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

  public static class RemoteCertbotProvider {
      private final Provisioning provisioning;
      private final Function<Server, RemoteManager> remoteManagerProvider;

      public RemoteCertbotProvider(Provisioning provisioning,
          Function<Server, RemoteManager> remoteManagerProvider) {
          this.provisioning = provisioning;
          this.remoteManagerProvider = remoteManagerProvider;
      }

      public RemoteCertbot getRemoteCertbot() throws ServiceException {
          final Server proxyServer =
                  provisioning.getAllServers().stream()
                          .filter(Server::hasProxyService)
                          .findFirst()
                          .orElseThrow(
                                  () ->
                                          ServiceException.FAILURE(
                                                  "Issuing LetsEncrypt certificate command requires carbonio-proxy. "
                                                          + "Make sure carbonio-proxy is installed, up and running."));
          return new RemoteCertbot(remoteManagerProvider.apply(proxyServer));
      }
  }
}
