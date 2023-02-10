package com.zimbra.cs.rmgmt;

import com.zimbra.common.service.ServiceException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * RemoteCertbot class interacts with "Certbot" - an acme client for managing Let’sEncrypt
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
  private static final String AGREEMENT = "--agree-tos --email";
  private static final String NON_INTERACTIVELY = "-n";
  private static final String EXPAND = "--cert-name";
  private static final String KEEP = "--keep";

  private final RemoteManager remoteManager;
  private StringBuilder stringBuilder;

  public RemoteCertbot(RemoteManager remoteManager) {
    this.remoteManager = remoteManager;
  }

  /**
   * Creates a command to be executed by the Certbot acme client.
   * E.g. certbot certonly --agree-tos --email admin@test.com -n --preferred-chain \"ISRG Root X1\"
   * --webroot -w /opt/zextras -d acme.demo.zextras.io -d webmail-acme.demo.zextras.io --dry-run
   *
   * @param remoteCommand {@link com.zimbra.cs.rmgmt.RemoteCommands}
   * @param email domain admin email who tries to execute a command (should be agreed to the
   *  ACME server's Subscriber Agreement)
   * @param chain long (default) or short (should be specified by domain admin in {@link
   *  com.zimbra.soap.admin.message.IssueCertRequest} request with the key word "short")
   * @param expand boolean value if domain virtual hostname was added or deleted
   * @param publicServiceHostName a value of domain attribute zimbraPublicServiceHostname
   * @param virtualHosts a value/ values of domain attribute zimbraVirtualHostname
   * @return created command
   */
  public String createCommand(String remoteCommand, String email, String chain,
      boolean expand, String publicServiceHostName, String[] virtualHosts) {

    this.stringBuilder = new StringBuilder();

    stringBuilder.append(remoteCommand);

    if (Objects.equals(chain, CHAIN_TYPE)) {
      addSubCommand(" ", CHAIN, SHORT_CHAIN);
    }

    addSubCommand(" ", AGREEMENT, email, NON_INTERACTIVELY, KEEP, WEBROOT, WEBROOT_PATH);

    if (expand) {
      addSubCommand(" ", EXPAND, publicServiceHostName);
    }

    addSubCommand(" -d ", publicServiceHostName);
    addSubCommand(" -d ", virtualHosts);

    return stringBuilder.toString();
  }

  /**
   * Executes a command using {@link com.zimbra.cs.rmgmt.RemoteManager}.
   * @param command a command to be executed
   * @return a sting message of successful remote execution or detailed exception message
   * in case of failure.
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
}
