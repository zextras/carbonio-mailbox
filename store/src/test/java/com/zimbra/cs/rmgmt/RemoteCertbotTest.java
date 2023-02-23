package com.zimbra.cs.rmgmt;

import static org.mockito.Mockito.mock;

import com.zimbra.common.soap.AdminConstants;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class RemoteCertbotTest {
  RemoteManager remoteManager = mock(RemoteManager.class);
  RemoteCertbot remoteCertbot = new RemoteCertbot(remoteManager);

  private final String publicServiceHostName = "example.com";
  private final String[] virtualHostName = {"virtual1.example.com", "virtual2.example.com"};
  private final String mail = "admin@example.com";

  @Test
  public void shouldCreateCommandForLongChain() {
    final String expectedCommand = "certbot certonly --agree-tos "
        + "--email admin@example.com "
        + "-n --keep --webroot -w /opt/zextras "
        + "-d example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, AdminConstants.DEFAULT_CHAIN, false, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }

  @Test
  public void shouldCreateCommandWithLongChainForRandomText() {
    final String expectedCommand = "certbot certonly --agree-tos "
        + "--email admin@example.com "
        + "-n --keep --webroot -w /opt/zextras "
        + "-d example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, "random", false, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }

  @Test
  public void shouldCreateCommandForShortChain() {
    final String expectedCommand = "certbot certonly --preferred-chain \"ISRG Root X1\" "
        + "--agree-tos --email admin@example.com -n --keep --webroot -w /opt/zextras "
        + "-d example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, "short", false, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }

  @Test
  public void shouldCreateCommandWithExpandFlag() {
    final String expectedCommand = "certbot certonly --agree-tos "
        + "--email admin@example.com "
        + "-n --keep --webroot -w /opt/zextras "
        + "--cert-name example.com -d example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, AdminConstants.DEFAULT_CHAIN, true, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }
}