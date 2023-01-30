package com.zimbra.cs.rmgmt;

import com.zimbra.common.service.ServiceException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RemoteCertbot {
  //"certbot certonly --agree-tos --email admin@test.com --preferred-chain \"ISRG Root X1\" --webroot -w /opt/zextras -d acme.demo.zextras.io -d webmail-acme.demo.zextras.io --dry-run";
  private static final String CHAIN = "--preferred-chain";
  private static final String CHAIN_TYPE = "\"ISRG Root X1\"";
  private static final String WEBROOT = "--webroot -w";
  private static final String WEBROOT_PATH = "/opt/zextras";
  private static final String AGREEMENT = "--agree-tos --email";


  private final RemoteManager remoteManager;
  private StringBuilder stringBuilder;

  public RemoteCertbot(RemoteManager remoteManager) {
    this.remoteManager = remoteManager;
  }

  public String getVersion() throws ServiceException {
    RemoteResult remoteResult = remoteManager.execute(RemoteCommands.CERTBOT_VERSION);
    return Arrays.toString(remoteResult.getMStdout());
  }

  public String dryRun(String email, String publicServiceHostName, String[] virtualHosts) throws ServiceException {
    this.stringBuilder = new StringBuilder();

    stringBuilder.append(RemoteCommands.CERTBOT_DRY_RUN);

    createCommand(" ",AGREEMENT, email, CHAIN, CHAIN_TYPE, WEBROOT, WEBROOT_PATH);
    createCommand(" -d ", publicServiceHostName);
    createCommand(" -d ", virtualHosts);

    String command = stringBuilder.toString();
    RemoteResult remoteResult = remoteManager.execute(command);
    return new String(remoteResult.getMStdout(), StandardCharsets.UTF_8);
  }

  private void createCommand(String delimiter, String ... params) {
    for (String param: params) {
      this.stringBuilder
          .append(delimiter)
          .append(param);
    }
  }
}
