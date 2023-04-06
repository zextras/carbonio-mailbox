package com.zimbra.cs.service.admin;

import static com.zimbra.common.soap.AdminConstants.A_DOMAIN;
import static com.zimbra.common.soap.AdminConstants.E_MESSAGE;
import static com.zimbra.common.soap.AdminConstants.ISSUE_CERT_REQUEST;
import static com.zimbra.soap.DocumentHandler.getRequestedMailbox;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.rmgmt.RemoteCertbot;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;

public class IssueCertTest {
  private static final MockedStatic<RemoteManager> staticRemoteManager
      = mockStatic(RemoteManager.class);
  private static final MockedStatic<RemoteCertbot> staticRemoteCertbot
      = mockStatic(RemoteCertbot.class);
  private static final MockedStatic<CertificateNotificationManager> staticNotificationManager
      = mockStatic(CertificateNotificationManager.class);

  private final Map<String, Object> context = new HashMap<>();
  private final Map<String, Object> domainAttributes = new HashMap<>();

  private final String domainName = "example.com";
  private final String publicServiceHostName = "public.example.com";
  private final String virtualHostName = "virtual.example.com";

  private final String mail = "admin@example.com";

  private final RemoteManager remoteManager = mock(RemoteManager.class);
  private final RemoteCertbot remoteCertbot = mock(RemoteCertbot.class);
  private final CertificateNotificationManager notificationManager
      = mock(CertificateNotificationManager.class);

  private final IssueCert handler = new IssueCert();
  private final XMLElement request = new XMLElement(ISSUE_CERT_REQUEST);

  private Provisioning provisioning;
  private ZimbraSoapContext zsc;

  @Rule public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();

    this.provisioning = Provisioning.getInstance();

    RightManager.getInstance();

    String domainId = "domainId";
    String password = "testPwd";

    Account account =
        provisioning.createAccount(
            mail,
            password,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
                put(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE");
                put(ZAttrProvisioning.A_mail, mail);
              }
            });

    domainAttributes.put(ZAttrProvisioning.A_zimbraDomainName, domainName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraId, domainId);

    this.zsc =  new ZimbraSoapContext(
        AuthProvider.getAuthToken(account, true),
        account.getId(),
        SoapProtocol.Soap12,
        SoapProtocol.Soap12);
    this.context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

    this.request.addNonUniqueElement(A_DOMAIN).addText(domainId);
  }

  @After
  public void clearData() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void tearDown() {
    staticRemoteManager.close();
    staticRemoteCertbot.close();
    staticNotificationManager.close();
  }

  @Test
  public void shouldSupplyAsyncAndReturnResponse() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);

    Domain expectedDomain = provisioning.createDomain(domainName, domainAttributes);

    final String serverName = "serverName";
    final Server server =
        provisioning.createServer(
            serverName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_cn, serverName);
                put(ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_PROXY);
              }
            });

    staticRemoteManager.when(() -> RemoteManager.getRemoteManager(server))
        .thenReturn(remoteManager);
    staticRemoteCertbot.when(() -> RemoteCertbot.getRemoteCertbot(remoteManager))
        .thenReturn(remoteCertbot);

    Mailbox expectMailbox = getRequestedMailbox(zsc);
    staticNotificationManager.when(() -> CertificateNotificationManager
        .getCertificateNotificationManager(expectMailbox, expectedDomain))
          .thenReturn(notificationManager);

    String expectedCommand = "certbot certonly --agree-tos --email admin@example.com"
        + " -n --keep --webroot -w /opt/zextras "
        + "--cert-name example.com "
        + "-d public.example.com -d virtual.example.com";

    when(remoteCertbot.createCommand(
        RemoteCommands.CERTBOT_CERTONLY,
        mail,
        AdminConstants.DEFAULT_CHAIN,
        domainName,
        publicServiceHostName,
        expectedDomain.getVirtualHostname())).thenReturn(expectedCommand);

    final Element response = handler.handle(request, context);
    final Element message = response.getElement(E_MESSAGE);

    assertEquals(domainName, message.getAttribute(A_DOMAIN));
    assertEquals(IssueCert.RESPONSE, message.getText());

    verify(remoteCertbot).supplyAsync(notificationManager, expectedCommand);
  }

  @Test
  public void shouldReturnInvalidIfNoSuchDomain() throws Exception {
    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage("Domain with id domainId could not be found.");

    handler.handle(request, context);
  }

  @Test
  public void shouldReturnInvalidIfNoPublicServiceHostName() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);
    provisioning.createDomain(domainName, domainAttributes);

    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage("must have PublicServiceHostname");

    handler.handle(request, context);
  }

  @Test
  public void shouldReturnInvalidIfNoVirtualHostName() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);

    provisioning.createDomain(domainName, domainAttributes);

    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage("must have at least one VirtualHostName.");

    handler.handle(request, context);
  }

  @Test
  public void shouldReturnFailureIfNoServerWithProxy() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);

    provisioning.createDomain(domainName, domainAttributes);

    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage("Issuing LetsEncrypt certificate command requires carbonio-proxy.");

    handler.handle(request, context);
  }
}
