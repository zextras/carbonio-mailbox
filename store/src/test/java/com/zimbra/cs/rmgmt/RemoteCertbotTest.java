package com.zimbra.cs.rmgmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import org.junit.jupiter.api.Test;


public class RemoteCertbotTest {

  private final String domainName = "example.com";
  private final String publicServiceHostName = "public.example.com";
  private final String[] virtualHostName = {"virtual1.example.com", "virtual2.example.com"};
  private final String mail = "admin@example.com";



 @Test
 void shouldCreateDefaultCommand() throws ServiceException {
  final String expectedCommand = "certbot certonly --agree-tos "
    + "--email admin@example.com "
    + "-n --keep --webroot -w /opt/zextras "
    + "--cert-name example.com "
    + "-d public.example.com "
    + "-d virtual1.example.com -d virtual2.example.com";
  final String actualCommand = getRemoteCertbot().createCommand(RemoteCommands.CERTBOT_CERTONLY,
    mail, domainName, publicServiceHostName, virtualHostName);
  assertEquals(expectedCommand, actualCommand);
 }

	private RemoteCertbot getRemoteCertbot() throws ServiceException {

		return RemoteCertbot.getRemoteCertbot(mock(RemoteManager.class));
	}
}