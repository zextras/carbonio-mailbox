package com.zimbra.cs.rmgmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.rmgmt.RemoteCertbot.RemoteCertbotProvider;
import com.zimbra.cs.service.admin.CertificateNotificationManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class RemoteCertbotTest extends MailboxTestSuite {

  private final String domainName = "example.com";
  private final String publicServiceHostName = "public.example.com";
  private final String[] virtualHostNames = {"virtual1.example.com", "virtual2.example.com"};
  private final String mail = "admin@example.com";

	@BeforeAll
	static void setUp() throws ServiceException {
		createProxyServer();
	}

  @Test
  void handleShouldSupplyAsyncAndReturnResponse() throws Exception {
		var fakeRemotemanager = mock(RemoteManager.class);
		final RemoteCertbot remoteCertbot = new RemoteCertbotProvider(Provisioning.getInstance(),
				(Server server) -> fakeRemotemanager).getRemoteCertbot();
		final String command = remoteCertbot.createCommand(RemoteCommands.CERTBOT_CERTONLY,
				mail, domainName, publicServiceHostName, virtualHostNames);
		remoteCertbot.supplyAsync(Mockito.mock(CertificateNotificationManager.class), command);

    final String expectedCommand =
        "certbot certonly --agree-tos --email " + mail
            + " -n --keep --webroot -w /opt/zextras "
            + "--cert-name " + domainName
            + " -d " + publicServiceHostName + " -d " + String.join(" -d ", virtualHostNames);
		Mockito.verify(fakeRemotemanager, Mockito.times(1)).execute(expectedCommand);
  }

 @Test
 void shouldCreateDefaultCommand() throws ServiceException {
  final String expectedCommand = "certbot certonly --agree-tos "
    + "--email admin@example.com "
    + "-n --keep --webroot -w /opt/zextras "
    + "--cert-name example.com "
    + "-d public.example.com "
    + "-d virtual1.example.com -d virtual2.example.com";
  final String actualCommand = getRemoteCertbot().createCommand(RemoteCommands.CERTBOT_CERTONLY,
    mail, domainName, publicServiceHostName, virtualHostNames);
  assertEquals(expectedCommand, actualCommand);
 }

	private RemoteCertbot getRemoteCertbot() throws ServiceException {

		return new RemoteCertbotProvider(Provisioning.getInstance(), (Server server) -> Mockito.mock(RemoteManager.class)).getRemoteCertbot();
	}
}