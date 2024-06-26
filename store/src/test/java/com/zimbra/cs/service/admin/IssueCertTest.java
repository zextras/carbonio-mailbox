package com.zimbra.cs.service.admin;

import static com.zimbra.common.soap.AdminConstants.A_DOMAIN;
import static com.zimbra.common.soap.AdminConstants.E_MESSAGE;
import static com.zimbra.common.soap.AdminConstants.ISSUE_CERT_REQUEST;
import static com.zimbra.soap.DocumentHandler.getRequestedMailbox;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class IssueCertTest {

  private static final MockedStatic<RemoteManager> staticRemoteManager =
      mockStatic(RemoteManager.class);
  private static final MockedStatic<RemoteCertbot> staticRemoteCertbot =
      mockStatic(RemoteCertbot.class);
  private static final MockedStatic<CertificateNotificationManager> staticNotificationManager =
      mockStatic(CertificateNotificationManager.class);

  private final Map<String, Object> context = new HashMap<>();
  private final Map<String, Object> domainAttributes = new HashMap<>();

  private final String domainName = "example.com";
  private final String publicServiceHostName = "public.example.com";
  private final String virtualHostName = "virtual.example.com";

  private final String mail = "admin@example.com";

  private final RemoteManager remoteManager = mock(RemoteManager.class);
  private final RemoteCertbot remoteCertbot = mock(RemoteCertbot.class);
  private final CertificateNotificationManager notificationManager =
      mock(CertificateNotificationManager.class);

  private final IssueCert handler = new IssueCert();
  private final XMLElement request = new XMLElement(ISSUE_CERT_REQUEST);

  private Provisioning provisioning;
  private ZimbraSoapContext zsc;
  private Account account;

  @AfterAll
  public static void tearDown() {
    staticRemoteManager.close();
    staticRemoteCertbot.close();
    staticNotificationManager.close();
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();

    this.provisioning = Provisioning.getInstance();

    RightManager.getInstance();

    final String domainId = "domainId";
    final String password = "testPwd";

    account = provisioning.createAccount(
        mail,
        password,
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
            put(ZAttrProvisioning.A_mail, mail);
          }
        });

    domainAttributes.put(ZAttrProvisioning.A_zimbraDomainName, domainName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraId, domainId);

    this.zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(account, true),
            account.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    this.context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

    this.request.addNonUniqueElement(A_DOMAIN).addText(domainId);
  }

  @AfterEach
  public void clearData() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void shouldProxyRequestIfAccountIsOnAnotherServer() throws Exception {
    IssueCert issueCert = spy(IssueCert.class);
    account.setMailHost("google.com");

    ServiceException serviceException = assertThrows(ServiceException.class,
        () -> issueCert.handle(request, context));

    assertEquals(ServiceException.PROXY_ERROR, serviceException.getCode());
    assertTrue(serviceException.getMessage().contains("error while proxying request to target server: no such server"));
    verify(issueCert).proxyRequestToAccountServer(request, context, account);
  }

  @Test
  void handleShouldSupplyAsyncAndReturnResponse() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);

    final Domain expectedDomain = provisioning.createDomain(domainName, domainAttributes);

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

    staticRemoteManager
        .when(() -> RemoteManager.getRemoteManager(server))
        .thenReturn(remoteManager);
    //noinspection ResultOfMethodCallIgnored
    staticRemoteCertbot
        .when(() -> RemoteCertbot.getRemoteCertbot(remoteManager))
        .thenReturn(remoteCertbot);

    final Mailbox expectMailbox = getRequestedMailbox(zsc);
    //noinspection ResultOfMethodCallIgnored
    staticNotificationManager
        .when(
            () ->
                CertificateNotificationManager.getCertificateNotificationManager(
                    expectMailbox, expectedDomain))
        .thenReturn(notificationManager);

    final String expectedCommand =
        "certbot certonly --agree-tos --email admin@example.com"
            + " -n --keep --webroot -w /opt/zextras "
            + "--cert-name example.com "
            + "-d public.example.com -d virtual.example.com";

    when(remoteCertbot.createCommand(
        RemoteCommands.CERTBOT_CERTONLY,
        mail,
        AdminConstants.DEFAULT_CHAIN,
        domainName,
        publicServiceHostName,
        expectedDomain.getVirtualHostname()))
        .thenReturn(expectedCommand);

    final Element response = handler.handle(request, context);
    final Element message = response.getElement(E_MESSAGE);

    assertEquals(domainName, message.getAttribute(A_DOMAIN));
    assertEquals(IssueCert.RESPONSE, message.getText());

    verify(remoteCertbot).supplyAsync(notificationManager, expectedCommand);
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoSuchDomain() {
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        "invalid request: Domain with id domainId could not be found.", exception.getMessage());
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoPublicServiceHostName() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);
    provisioning.createDomain(domainName, domainAttributes);
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        "system failure: Domain example.com must have PublicServiceHostname.",
        exception.getMessage());
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoVirtualHostName() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    provisioning.createDomain(domainName, domainAttributes);
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        "system failure: Domain example.com must have at least one VirtualHostName.",
        exception.getMessage());
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoServerWithProxyIsAvailable() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);
    provisioning.createDomain(domainName, domainAttributes);
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        "system failure: Issuing LetsEncrypt certificate command requires carbonio-proxy. Make sure"
            + " carbonio-proxy is installed, up and running.",
        exception.getMessage());
  }
}
