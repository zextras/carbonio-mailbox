package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.admin.message.IssueCertRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.Maps;

public class IssueCertTest extends SoapTestSuite {

  private Account adminAccount;

  @BeforeEach
  public void setUp() throws Exception {
    adminAccount = createAccount().asGlobalAdmin().create();
  }

  @Test
  void shouldProxyRequestIfAccountIsOnAnotherServer() throws Exception {
    final Server server = createMailServer();
    final Account globalAdminOnOtherServer = createAccount().asGlobalAdmin().create();
    globalAdminOnOtherServer.modify(Maps.newHashMap(Map.of(ZAttrProvisioning.A_zimbraMailHost, server.getName())));
    final Domain domain = createDomain();

    final SoapResponse soapResponse = executeIssueCertApi(globalAdminOnOtherServer, domain.getId());

    // Note: target server does not exists, but whate matters is that it tries to proxy
    assertTrue(soapResponse.body().contains("error while proxying request to target server"));
  }
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
    return executeIssueCertApi(adminAccount, domainId);
  }

  private SoapResponse executeIssueCertApi(Account account, String domainId) throws Exception {
    return getSoapClient().executeSoap(account,
        new IssueCertRequest(domainId));
  }
}
