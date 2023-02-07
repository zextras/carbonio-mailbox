package com.zimbra.cs.rmgmt;

import com.zimbra.common.service.ServiceException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class RemoteCertbot {
  // "certbot certonly --agree-tos --email admin@test.com --preferred-chain \"ISRG Root X1\"
  // --webroot -w /opt/zextras -d acme.demo.zextras.io -d webmail-acme.demo.zextras.io --dry-run";
  private static final String CHAIN = "--preferred-chain";
  private static final String DEFAULT_CHAIN = "\"ISRG Root X1\"";
  private static final String CHAIN_TYPE = "long";
  private static final String WEBROOT = "--webroot -w";
  private static final String WEBROOT_PATH = "/opt/zextras";
  private static final String AGREEMENT = "--agree-tos --email";

  private final RemoteManager remoteManager;
  private StringBuilder stringBuilder;

  public RemoteCertbot(RemoteManager remoteManager) {
    this.remoteManager = remoteManager;
  }

  public String createCommand(String remoteCommand,
      String email, String chain, String publicServiceHostName, String[] virtualHosts)
      throws ServiceException {

    this.stringBuilder = new StringBuilder();

    stringBuilder.append(remoteCommand);

    if (!Objects.equals(chain, CHAIN_TYPE)) {
      addCommand(" ", CHAIN, DEFAULT_CHAIN);
    }

    addCommand(" ", AGREEMENT, email, WEBROOT, WEBROOT_PATH);
    addCommand(" -d ", publicServiceHostName);
    addCommand(" -d ", virtualHosts);

    return stringBuilder.toString();
  }

  public String execute(String command) {
    try {
      RemoteResult remoteResult = remoteManager.execute(command);
      return new String(remoteResult.getMStdout(), StandardCharsets.UTF_8);
    } catch (ServiceException e) {
      return e.getMessage();
    }
  }

  private void addCommand(String delimiter, String... params) {
    for (String param : params) {
      this.stringBuilder.append(delimiter).append(param);
    }
  }
}
