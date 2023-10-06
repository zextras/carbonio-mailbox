// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.admin;

import com.zextras.mailbox.account.usecase.DeleteUserUseCase;
import com.zextras.mailbox.acl.AclService;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DeleteAccountTest {

  private static final String OTHER_DOMAIN = "other.com";
  private static Provisioning provisioning;
  private static DeleteAccount deleteAccount;

  /**
   * Sets up the environment using {@link MailboxTestUtil}.
   * Note: unfortunately it is not possible to start the SoapServlet with {@link
   * AdminService}. The reason is some code calls System.exit(1) presumably, so the VM exits and maven fails.
   *
   * @throws Exception
   */
  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    final MailboxManager mailboxManager = MailboxManager.getInstance();
    provisioning = Provisioning.getInstance();
    deleteAccount = new DeleteAccount(new DeleteUserUseCase(provisioning, mailboxManager,
        new AclService(mailboxManager, provisioning), ZimbraLog.security));
    provisioning.createDomain(OTHER_DOMAIN, new HashMap<>());
  }

  @AfterAll
  static void tearDown() throws Exception {
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

  private Element doDeleteAccount(Account caller, String toDeleteId) throws ServiceException {
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(caller),
            caller.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    return deleteAccount.handle(
        JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDeleteId)), context);
  }

  @ParameterizedTest
  @MethodSource("getHappyPathCases")
  void shouldDeleteUser(Account caller, Account toDelete) throws Exception {
    final String toDeleteId = toDelete.getId();
    this.doDeleteAccount(caller, toDeleteId);
    Assertions.assertNull(provisioning.getAccountById(toDeleteId));
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
    Assertions.assertThrows(ServiceException.class, () -> this.doDeleteAccount(caller, toDeleteId));
    Assertions.assertNotNull(provisioning.getAccountById(toDeleteId));
  }

//  @Test
//  @DisplayName("No token when calling the API -> AUTH_REQUIRED")
//  void shouldGet500WithAuthRequiredIfNoToken() throws Exception {
//    final Account toDelete = MailboxTestUtil.createAccountDefaultDomain(Map.of());
//    final HttpResponse response = this.client().build().execute(deleteRequest(toDelete.getId()));
//    final String responseEnvelope = new String(response.getEntity().getContent().readAllBytes());
//    Assertions.assertTrue(responseEnvelope.contains("<Code>" + AUTH_REQUIRED + "</Code>"));
//    Assertions.assertEquals(
//        HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
//  }
}
