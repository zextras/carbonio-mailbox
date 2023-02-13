package com.zimbra.cs.service.admin;

import static com.zimbra.common.soap.AdminConstants.A_DOMAIN;
import static com.zimbra.common.soap.AdminConstants.E_MESSAGE;
import static com.zimbra.common.soap.AdminConstants.ISSUE_CERT_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.rmgmt.RemoteResult;
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

  private Provisioning provisioning;

  private final Map<String, Object> context = new HashMap<>();
  private final Map<String, Object> domainAttributes = new HashMap<>();

  private final String domainName = "test.demo.zextras.io";
  private final String publicServiceHostName = "public.test.demo.zextras.io";
  private final String virtualHostName = "virtual.test.demo.zextras.io";

  private final String mail = "admin@test.demo.zextras.io";

  private final String command = "certbot certonly --agree-tos --email admin@test.demo.zextras.io"
      + " -n --keep --webroot -w /opt/zextras "
      + "-d public.test.demo.zextras.io -d virtual.test.demo.zextras.io";

  private final static MockedStatic<RemoteManager> mockedStatic = mockStatic(RemoteManager.class);
  private final RemoteManager remoteManager = mock(RemoteManager.class);
  private final RemoteResult remoteResult = mock(RemoteResult.class);

  private final IssueCert handler = new IssueCert();
  private final XMLElement request = new XMLElement(ISSUE_CERT_REQUEST);

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

    this.context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(account, true),
            account.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12));

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
      mockedStatic.close();
  }

  @Test
  public void shouldReturnSuccessMessageIfAllChecksPassed() throws Exception {
    createInfrastructure();

    final String result = "SUCCESS";

    when(remoteManager.execute(command)).thenReturn(remoteResult);
    when(remoteResult.getMStdout()).thenReturn(result.getBytes());

    final Element response = handler.handle(request, context);
    final Element message = response.getElement(E_MESSAGE);
    assertEquals(message.getAttribute(A_DOMAIN), domainName);
    assertEquals(message.getText(), result);
  }

  @Test
  public void shouldReturnFailureMessageIfRemoteExecutionFailed() throws Exception {
    createInfrastructure();

    final String result = "FAILURE";
    final String expectedMessage = "system failure: FAILURE";

    when(remoteManager.execute(command)).thenThrow(ServiceException.FAILURE(result));

    final Element response = handler.handle(request, context);
    final Element message = response.getElement(E_MESSAGE);
    assertEquals(message.getAttribute(A_DOMAIN), domainName);
    assertEquals(message.getText(), expectedMessage);
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
  public void shouldReturnNotFoundIfNoServerWithProxy() throws Exception {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);

    provisioning.createDomain(domainName, domainAttributes);

    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage("Issuing a LetsEncrypt certificate command requires carbonio-proxy node.");

    handler.handle(request, context);
  }

  private void createInfrastructure() throws ServiceException {
    domainAttributes.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, publicServiceHostName);
    domainAttributes.put(ZAttrProvisioning.A_zimbraVirtualHostname, virtualHostName);

    provisioning.createDomain(domainName, domainAttributes);

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

    mockedStatic.when(() -> RemoteManager.getRemoteManager(server)).thenReturn(remoteManager);
  }
}
