package com.zimbra.cs.service.admin;

import static com.zimbra.common.soap.AdminConstants.A_DOMAIN;
import static com.zimbra.common.soap.AdminConstants.E_MESSAGE;
import static com.zimbra.common.soap.AdminConstants.ISSUE_CERT_REQUEST;
import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.rmgmt.RemoteCertbot;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.IssueCertRequest;
import io.vavr.control.Try;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.shaded.com.google.common.collect.Maps;

// FIXME
public class IssueCertTest extends SoapTestSuite {

  private Account adminAccount;

  @BeforeEach
  public void setUp() throws Exception {
    adminAccount = createAccount().asGlobalAdmin().create();
  }

//  @Test
//  void shouldProxyRequestIfAccountIsOnAnotherServer() throws Exception {
//    // FIXME: these tests are crooked because they rely too much on mocks and were made with unreal mocked scenarios.
//    //  the reality is much more complex. The httprequest checks the context when proxying.
//    //  Also is not possible to create a user with a server that does not exists, which is what this test was relying on.
//    var request = getRequest(domain.getId());
//    IssueCert issueCert = Mockito.spy(getIssueCert());
//    provisioning.createServer("otherServer.com", new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
//    accountMakingRequest.setMailHost("otherServer.com");
//
//    context.put(SoapServlet.SERVLET_REQUEST, new MockRequest());
//
//    ServiceException serviceException = assertThrows(ServiceException.class,
//        () -> issueCert.handle(request, context));
//
//    assertEquals(ServiceException.PROXY_ERROR, serviceException.getCode());
//    verify(issueCert).proxyRequestToAccountServer(Mockito.any(), Mockito.any(), Mockito.any());;
//  }
//
//  @Test
//  void handleShouldSupplyAsyncAndReturnResponse() throws Exception {
//    var request = getRequest(domain.getId());
//    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
//    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);
//
//    domain.modify(domainAttributes);
//
//    final String serverName = "serverName";
//
//    provisioning.createServer(
//        serverName, new HashMap<>() {
//            {
//                put(ZAttrProvisioning.A_cn, serverName);
//                put(ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_PROXY);
//            }
//        });
//
//    final String expectedCommand =
//        "certbot certonly --agree-tos --email " + accountMakingRequest.getName()
//            + " -n --keep --webroot -w /opt/zextras "
//            + "--cert-name  " + domain.getName()
//            + "-d " + publicServiceHostName + " -d " + virtualHostName;
//
//    when(remoteCertbot.createCommand(
//        RemoteCommands.CERTBOT_CERTONLY,
//        accountMakingRequest.getName(),
//        domain.getName(),
//        publicServiceHostName,
//        domain.getVirtualHostname()))
//        .thenReturn(expectedCommand);
//
//    final Element response = handler.handle(request, context);
//    final Element message = response.getElement(E_MESSAGE);
//
//    assertEquals(domain.getName(), message.getAttribute(A_DOMAIN));
//    assertEquals(IssueCert.RESPONSE, message.getText());
//
//    verify(remoteCertbot).supplyAsync(notificationManager, expectedCommand);
//  }

  @Test
  void shouldReturnInvalidRequest_WhenNoSuchDomain() throws Exception {
      String nonExistingDomain = "nonExistingDomain.com";

      String body = executeIssueCertApi(nonExistingDomain).body();

      assertTrue(body.contains(String.format("invalid request: Domain with id %s could not be found.", nonExistingDomain)));
  }

  @Test
  void shouldFail_WhenNoPublicServiceHostName() throws Exception {
    final Domain domain = createDomain();
    final HashMap<String, Object> attrs = Maps.newHashMap();
    attrs.put(ZAttrProvisioning.A_zimbraVirtualHostname, "virtual." + domain.getName());
    domain.modify(attrs);

    String body = executeIssueCertApi(domain.getId()).body();

    final String errorMessage = String.format(
        "system failure: Domain %s must have PublicServiceHostname.", domain.getName());
    assertTrue(
        body.contains(errorMessage));
  }


  @Test
  void shouldFail_whenNoVirtualHostName() throws Exception {
    final Domain domain = createDomain();
    final HashMap<String, Object> attrs = Maps.newHashMap();
    attrs.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, "public." + domain.getName());
    domain.modify(attrs);

    String body = executeIssueCertApi(domain.getId()).body();

    assertTrue(body.contains(String.format("system failure: Domain %s must have at least one VirtualHostName.", domain.getName())));
  }

  private SoapResponse executeIssueCertApi(String domainId) throws Exception {
    return getSoapClient().executeSoap(adminAccount,
        new IssueCertRequest(domainId));
  }
}
