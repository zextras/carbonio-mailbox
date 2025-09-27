package com.zimbra.cs.service.admin;

import static com.zimbra.common.soap.AdminConstants.A_DOMAIN;
import static com.zimbra.common.soap.AdminConstants.E_MESSAGE;
import static com.zimbra.common.soap.AdminConstants.ISSUE_CERT_REQUEST;
import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;
import static com.zimbra.soap.DocumentHandler.getRequestedMailbox;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.MailboxTestSuite;
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
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.rmgmt.RemoteCertbot;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.shaded.com.google.common.collect.Maps;

public class IssueCertTest extends MailboxTestSuite {

  private static MockedStatic<RemoteManager> staticRemoteManager;

  private final Map<String, Object> context = new HashMap<>();
  private final Map<String, Object> domainAttributes = new HashMap<>();

  private String publicServiceHostName;
  private String virtualHostName;
  private static final RemoteCertbot remoteCertbot = mock(RemoteCertbot.class);
  private static final CertificateNotificationManager notificationManager = mock(CertificateNotificationManager.class);

  private final IssueCert handler = getIssueCert();

  private Provisioning provisioning;
	private Account accountMakingRequest;
  private Domain domain;

  @BeforeAll
  static void init() throws Exception {
    staticRemoteManager = mockStatic(RemoteManager.class);;
  }

  private static IssueCert getIssueCert() {
    return new IssueCert((RemoteManager remoteManager) -> remoteCertbot, (Mailbox mbox, Domain domain) -> notificationManager);
  }
  @BeforeEach
  public void setUp() throws Exception {
    clearData();
    initData();
    Mockito.reset(remoteCertbot);
    Mockito.reset(notificationManager);
    provisioning = Provisioning.getInstance();
    var domainName = UUID.randomUUID() + ".example.com";
    virtualHostName = "virtual." + domainName;
    publicServiceHostName = "public." + domainName;
    domain = provisioning.createDomain(domainName, Maps.newHashMap());
    accountMakingRequest = provisioning.createAccount(
        UUID.randomUUID() + "@" + domain.getDomainName(),
        "secret",
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
          }
        });
    final String domainId = domain.getId();

		ZimbraSoapContext zsc = new ZimbraSoapContext(
				AuthProvider.getAuthToken(accountMakingRequest, true),
				accountMakingRequest.getId(),
				SoapProtocol.Soap12,
				SoapProtocol.Soap12);
    this.context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
  }

  Element getRequest(String domainId) {
    var request = new XMLElement(ISSUE_CERT_REQUEST);
    request.addNonUniqueElement(A_DOMAIN).addText(domainId);
    return request;
  }

  @Test
  @Disabled
  void shouldProxyRequestIfAccountIsOnAnotherServer() throws Exception {
    // FIXME: these tests are crooked because they rely too much on mocks and were made with unreal mocked scenarios.
    //  the reality is much more complex. The httprequest checks the context when proxying.
    //  Also is not possible to create a user with a server that does not exists, which is what this test was relying on.
    var request = getRequest(domain.getId());
    IssueCert issueCert = Mockito.spy(getIssueCert());
    provisioning.createServer("otherServer.com", new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    accountMakingRequest.setMailHost("otherServer.com");

    context.put(SoapServlet.SERVLET_REQUEST, new MockRequest());

    ServiceException serviceException = assertThrows(ServiceException.class,
        () -> issueCert.handle(request, context));

    assertEquals(ServiceException.PROXY_ERROR, serviceException.getCode());
    verify(issueCert).proxyRequestToAccountServer(Mockito.any(), Mockito.any(), Mockito.any());;
  }

  @Test
  void handleShouldSupplyAsyncAndReturnResponse() throws Exception {
    var request = getRequest(domain.getId());
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);

    domain.modify(domainAttributes);

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

    final String expectedCommand =
        "certbot certonly --agree-tos --email " + accountMakingRequest.getName()
            + " -n --keep --webroot -w /opt/zextras "
            + "--cert-name  " + domain.getName()
            + "-d " + publicServiceHostName + " -d " + virtualHostName;

    when(remoteCertbot.createCommand(
        RemoteCommands.CERTBOT_CERTONLY,
        accountMakingRequest.getName(),
        AdminConstants.DEFAULT_CHAIN,
        domain.getName(),
        publicServiceHostName,
        domain.getVirtualHostname()))
        .thenReturn(expectedCommand);

    final Element response = handler.handle(request, context);
    final Element message = response.getElement(E_MESSAGE);

    assertEquals(domain.getName(), message.getAttribute(A_DOMAIN));
    assertEquals(IssueCert.RESPONSE, message.getText());

    verify(remoteCertbot).supplyAsync(notificationManager, expectedCommand);
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoSuchDomain() {
    var request = getRequest("domainId");
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        "invalid request: Domain with id domainId could not be found.", exception.getMessage());
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoPublicServiceHostName() throws Exception {
    var request = getRequest(domain.getId());
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);
    domain.modify(domainAttributes);
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        String.format("system failure: Domain %s must have PublicServiceHostname.", domain.getName()),
        exception.getMessage());
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoVirtualHostName() throws Exception {
    var request = getRequest(domain.getId());
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domain.modify(domainAttributes);
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        String.format("system failure: Domain %s must have at least one VirtualHostName.", domain.getName()),
        exception.getMessage());
  }

  @Test
  void handleShouldThrowServiceExceptionWhenNoServerWithProxyIsAvailable() throws Exception {
    var request = getRequest(domain.getId());
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);
    domain.modify(domainAttributes);
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> handler.handle(request, context));
    assertEquals(
        "system failure: Issuing LetsEncrypt certificate command requires carbonio-proxy. Make sure"
            + " carbonio-proxy is installed, up and running.",
        exception.getMessage());
  }

  private static class MockRequest extends MockHttpServletRequest {

    public MockRequest() throws MalformedURLException, UnsupportedEncodingException {
      super("test".getBytes("UTF-8"), new URL("http://localhost:8080/service"), "");
    }

    @Override
    public String getRequestURI() {
      return "";
    }
  }
}
