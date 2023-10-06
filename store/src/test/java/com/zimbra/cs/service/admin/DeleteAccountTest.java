// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.admin;

import static com.zimbra.common.service.ServiceException.AUTH_REQUIRED;
import static com.zimbra.common.service.ServiceException.PERM_DENIED;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("api")
@Disabled
class DeleteAccountTest {

  private static final int ADMIN_PORT = 7071;
  private static final String OTHER_DOMAIN = "other.com";
  private static Server mailboxServer;
  private static Provisioning provisioning;

  /**
   * Sets up the environment using {@link MailboxTestUtil}. Also starts the SoapServlet with {@link
   * AdminService} only Starts also {@link FirstServlet} since it is required to make the API work.
   *
   * @throws Exception
   */
  @BeforeAll
  static void setUp() throws Exception {
    System.setProperty("java.library.path", new File("./../").getAbsolutePath() + "/native/target");
    MailboxTestUtil.setUp();
    final ServletHolder firstServlet = new ServletHolder("FirstServlet", FirstServlet.class);
    firstServlet.setInitOrder(0);
    final ServletHolder adminServlet = new ServletHolder("AdminServlet", SoapServlet.class);
    adminServlet.setInitParameter("allowed.ports", Integer.toString(ADMIN_PORT));
    adminServlet.setInitOrder(1);
    adminServlet.setInitParameter("engine.handler.0", "com.zimbra.cs.service.admin.AdminService");
    provisioning = Provisioning.getInstance();
    provisioning.createDomain(OTHER_DOMAIN, new HashMap<>());
    mailboxServer =
        JettyServerFactory.create(
            ADMIN_PORT,
            Map.of(
                "/*", adminServlet,
                "/firstServlet/*", firstServlet));
    mailboxServer.start();
  }

  @AfterAll
  static void tearDown() throws Exception {
    mailboxServer.stop();
    MailboxTestUtil.tearDown();
  }

  private static Stream<Arguments> getHappyPathCases() throws ServiceException {
    return Stream.of(
        Arguments.of(
            MailboxTestUtil.createAccountDefaultDomain(
                Map.of(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE")),
            MailboxTestUtil.createAccountDefaultDomain(Map.of())),
        Try.of(
                () -> {
                  final Account delegatedAdminWithDomainAdminRight =
                      MailboxTestUtil.createAccountDefaultDomain(
                          Map.of(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE"));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      provisioning.getDomainByName(MailboxTestUtil.DEFAULT_DOMAIN),
                      Set.of(
                          new ZimbraACE(
                              delegatedAdminWithDomainAdminRight.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_domainAdminRights,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(
                      delegatedAdminWithDomainAdminRight,
                      MailboxTestUtil.createAccountDefaultDomain(Map.of()));
                })
            .get(),
        Try.of(
                () -> {
                  final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
                  final Account admin =
                      MailboxTestUtil.createAccountDefaultDomain(
                          Map.of(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE"));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      provisioning.getDomainByName(MailboxTestUtil.DEFAULT_DOMAIN),
                      Set.of(
                          new ZimbraACE(
                              admin.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_deleteAccount,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(admin, toDelete);
                })
            .get(),
        Try.of(
                () -> {
                  final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
                  final Account admin =
                      MailboxTestUtil.createAccountDefaultDomain(
                          Map.of(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE"));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      toDelete,
                      Set.of(
                          new ZimbraACE(
                              admin.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_deleteAccount,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(admin, toDelete);
                })
            .get(),
        Try.of(
                () -> {
                  final Account admin =
                      provisioning.createAccount(
                          UUID.randomUUID() + "@" + OTHER_DOMAIN,
                          "password",
                          new HashMap<>(
                              Map.of(
                                  ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount,
                                  "TRUE",
                                  Provisioning.A_zimbraMailHost,
                                  MailboxTestUtil.SERVER_NAME)));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      provisioning.getDomainByName(MailboxTestUtil.DEFAULT_DOMAIN),
                      Set.of(
                          new ZimbraACE(
                              admin.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_domainAdminRights,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(admin, MailboxTestUtil.createAccountDefaultDomain(Map.of()));
                })
            .get(),
        Try.of(
                () -> {
                  final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
                  final Account admin =
                      provisioning.createAccount(
                          UUID.randomUUID() + "@" + OTHER_DOMAIN,
                          "password",
                          new HashMap<>(
                              Map.of(
                                  ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount,
                                  "TRUE",
                                  Provisioning.A_zimbraMailHost,
                                  MailboxTestUtil.SERVER_NAME)));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      provisioning.getDomainByName(MailboxTestUtil.DEFAULT_DOMAIN),
                      Set.of(
                          new ZimbraACE(
                              admin.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_deleteAccount,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(admin, toDelete);
                })
            .get(),
        Try.of(
                () -> {
                  final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
                  final Account admin =
                      provisioning.createAccount(
                          UUID.randomUUID() + "@" + OTHER_DOMAIN,
                          "password",
                          new HashMap<>(
                              Map.of(
                                  ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount,
                                  "TRUE",
                                  Provisioning.A_zimbraMailHost,
                                  MailboxTestUtil.SERVER_NAME)));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      toDelete,
                      Set.of(
                          new ZimbraACE(
                              admin.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_deleteAccount,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(admin, toDelete);
                })
            .get());
  }

  @ParameterizedTest
  @MethodSource("getHappyPathCases")
  void shouldDeleteUser(Account caller, Account toDelete) throws Exception {
    final String toDeleteId = toDelete.getId();
    final HttpResponse deleteUserResponse =
        this.client()
            .setDefaultCookieStore(getCookie(caller))
            .build()
            .execute(deleteRequest(toDeleteId));
    Assertions.assertEquals(HttpStatus.SC_OK, deleteUserResponse.getStatusLine().getStatusCode());
    Assertions.assertNull(provisioning.getAccountById(toDeleteId));
  }

  private HttpClientBuilder client() {
    return HttpClientBuilder.create();
  }

  private CookieStore getCookie(Account account) throws AuthProviderException, AuthTokenException {
    final AuthToken authToken = AuthProvider.getAuthToken(account, true);
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(true), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return cookieStore;
  }

  private HttpPost deleteRequest(String toDeleteId)
      throws ServiceException, UnsupportedEncodingException {
    final HttpPost httpPost = new HttpPost("http://localhost:" + ADMIN_PORT);
    final Element element = JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDeleteId));
    Element envelope = SoapProtocol.Soap12.soapEnvelope(element, null);
    httpPost.setEntity(new StringEntity(envelope.toString()));
    return httpPost;
  }

  private static Stream<Arguments> getPermissionDeniedCases() throws ServiceException {
    return Stream.of(
        Arguments.of(
            MailboxTestUtil.createAccountDefaultDomain(
                Map.of(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")),
            MailboxTestUtil.createAccountDefaultDomain(Map.of())),
        Try.of(
                () -> {
                  final Account standardUser = MailboxTestUtil.createAccountDefaultDomain(Map.of());
                  final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      provisioning.getDomainByName(MailboxTestUtil.DEFAULT_DOMAIN),
                      Set.of(
                          new ZimbraACE(
                              standardUser.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_deleteAccount,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  ACLUtil.grantRight(
                      Provisioning.getInstance(),
                      toDelete,
                      Set.of(
                          new ZimbraACE(
                              standardUser.getId(),
                              GranteeType.GT_USER,
                              AdminRights.R_deleteAccount,
                              RightModifier.RM_CAN_DELEGATE,
                              null)));
                  return Arguments.of(standardUser, toDelete);
                })
            .get(),
        Arguments.of(
            MailboxTestUtil.createAccountDefaultDomain(
                Map.of(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")),
            MailboxTestUtil.createAccountDefaultDomain(Map.of())));
  }

  @ParameterizedTest
  @MethodSource("getPermissionDeniedCases")
  void shouldGetPermissionDenied(Account caller, Account toDelete)
      throws ServiceException, AuthTokenException, IOException {
    final String toDeleteId = toDelete.getId();
    final HttpResponse deleteUserResponse =
        this.client()
            .setDefaultCookieStore(getCookie(caller))
            .build()
            .execute(deleteRequest(toDeleteId));
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, deleteUserResponse.getStatusLine().getStatusCode());
    final String responseEnvelope =
        new String(deleteUserResponse.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(responseEnvelope.contains("<Code>" + PERM_DENIED + "</Code>"));
    Assertions.assertNotNull(provisioning.getAccountById(toDeleteId));
  }

  @Test
  @DisplayName("No token when calling the API -> AUTH_REQUIRED")
  void shouldGet500WithAuthRequiredIfNoToken() throws Exception {
    final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
    final HttpResponse response = this.client().build().execute(deleteRequest(toDelete.getId()));
    final String responseEnvelope = new String(response.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(responseEnvelope.contains("<Code>" + AUTH_REQUIRED + "</Code>"));
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
  }
}
