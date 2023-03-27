package com.zimbra.cs.rmgmt;

import static org.mockito.Mockito.mock;

import com.zimbra.common.soap.AdminConstants;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class RemoteCertbotTest {
  RemoteManager remoteManager = mock(RemoteManager.class);
  RemoteCertbot remoteCertbot = new RemoteCertbot(remoteManager);

  private final String domainName = "example.com";
  private final String publicServiceHostName = "public.example.com";
  private final String[] virtualHostName = {"virtual1.example.com", "virtual2.example.com"};
  private final String mail = "admin@example.com";

  @Test
  public void shouldCreateCommandForLongChain() {
    final String expectedCommand = "certbot certonly --agree-tos "
        + "--email admin@example.com "
        + "-n --keep --webroot -w /opt/zextras "
        + "--cert-name example.com "
        + "-d public.example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, AdminConstants.DEFAULT_CHAIN, domainName, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }

  @Test
  public void shouldCreateCommandWithLongChainForRandomText() {
    final String expectedCommand = "certbot certonly --agree-tos "
        + "--email admin@example.com "
        + "-n --keep --webroot -w /opt/zextras "
        + "--cert-name example.com "
        + "-d public.example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, "random", domainName, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }

  @Test
  public void shouldCreateCommandForShortChain() {
    final String expectedCommand = "certbot certonly --preferred-chain \"ISRG Root X1\" "
        + "--agree-tos --email admin@example.com -n --keep --webroot -w /opt/zextras "
        + "--cert-name example.com "
        + "-d public.example.com "
        + "-d virtual1.example.com -d virtual2.example.com";
    final String actualCommand = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
        mail, "short", domainName, publicServiceHostName, virtualHostName);
    assertEquals(expectedCommand, actualCommand);
  }
}