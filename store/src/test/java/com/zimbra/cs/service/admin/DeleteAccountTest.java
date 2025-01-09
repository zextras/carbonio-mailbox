// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.events.services.mailbox.DeleteUserRequested;
import com.zextras.mailbox.account.usecase.DeleteUserUseCase;
import com.zextras.mailbox.acl.AclService;
import com.zextras.mailbox.messageBroker.MessageBrokerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
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

import java.nio.file.Files;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class DeleteAccountTest {

  private static final String OTHER_DOMAIN = "other.com";
  private static Provisioning provisioning;
  private static DeleteAccount deleteAccount;
  private static MessageBrokerClient mockMessageBrokerClient;
  private static AccountCreator.Factory accountCreatorFactory;
  private static ClientAndServer consulServer;


  /**
   * Sets up the environment using {@link MailboxTestUtil}. Note: unfortunately it is not possible
   * to start the SoapServlet with {@link AdminService}. The reason is some code calls
   * System.exit(1) presumably, so the VM exits and maven fails.
   *
   */
  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    final MailboxManager mailboxManager = MailboxManager.getInstance();
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
    mockMessageBrokerClient = MessageBrokerFactory.getMessageBrokerClientInstance();
    deleteAccount =
        new DeleteAccount(
            new DeleteUserUseCase(
                provisioning,
                mailboxManager,
                new AclService(mailboxManager, provisioning),
                ZimbraLog.security));
    provisioning.createDomain(OTHER_DOMAIN, new HashMap<>());

    consulServer = startClientAndServer(8500);

		consulServer
        .when(request().withPath("/v1/kv/carbonio-message-broker/default/username"))
				.respond(response().withStatusCode(200).withBody("[" +
						"{\"Value\": \"test\"}" +
						"]"));
    consulServer
        .when(request().withPath("/v1/kv/carbonio-message-broker/default/password"))
				.respond(response().withStatusCode(200).withBody("[" +
						"{\"Value\": \"test\"}" +
						"]"));
    consulServer
        .when(request().withPath("/v1/health/checks/carbonio-files"))
        .respond(response().withStatusCode(200).withBody("[]"));
  }

  @AfterAll
  static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  private static Stream<Arguments> getHappyPathCases() throws ServiceException {
    return Stream.of(
        Arguments.of(accountCreatorFactory.get().asGlobalAdmin().create(),
            accountCreatorFactory.get().create()),
        Try.of(
                () -> {
                  final Account delegatedAdminWithDomainAdminRight =
                      accountCreatorFactory.get()
                          .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
                          .create();
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
                      accountCreatorFactory.get().create());
                })
            .get(),
        Try.of(
                () -> {
                  final Account toDelete = accountCreatorFactory.get().create();
                  final Account admin = accountCreatorFactory.get()
                      .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
                      .create();
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
                  final Account toDelete = accountCreatorFactory.get().create();
                  final Account admin = accountCreatorFactory.get()
                      .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
                      .create();
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
                  return Arguments.of(admin, accountCreatorFactory.get().create());
                })
            .get(),
        Try.of(
                () -> {
                  final Account toDelete = accountCreatorFactory.get().create();
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
                  final Account toDelete = accountCreatorFactory.get().create();
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

  private void doDeleteAccount(Account caller, String toDeleteId) throws ServiceException {
    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(caller),
            caller.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    deleteAccount.handle(
        JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDeleteId)), context);
  }

  @ParameterizedTest
  @MethodSource("getHappyPathCases")
  void shouldDeleteUser(Account caller, Account toDelete) throws Exception {
    try (MockedStatic<Files> mockFileSystem = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
      mockFileSystem.when(() -> Files.readString(any())).thenReturn("");
      Mockito.when(mockMessageBrokerClient.publish(any(DeleteUserRequested.class))).thenReturn(true);

      final String toDeleteId = toDelete.getId();
      this.doDeleteAccount(caller, toDeleteId);
      Assertions.assertNull(provisioning.getAccountById(toDeleteId));
    }
  }

  private static Stream<Arguments> getPermissionDeniedCases() throws ServiceException {
    return Stream.of(
        Arguments.of(
            accountCreatorFactory.get()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
                .create(),
            accountCreatorFactory.get().create()),
        Try.of(
                () -> {
                  final Account standardUser = accountCreatorFactory.get().create();
                  final Account toDelete = accountCreatorFactory.get().create();
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
            accountCreatorFactory.get()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
                .create(),
            accountCreatorFactory.get().create()));
  }

  @ParameterizedTest
  @MethodSource("getPermissionDeniedCases")
  void shouldGetPermissionDenied(Account caller, Account toDelete) throws ServiceException {
      Mockito.when(mockMessageBrokerClient.publish(any(DeleteUserRequested.class))).thenReturn(true);

      final String toDeleteId = toDelete.getId();
      final ServiceException serviceException =
          Assertions.assertThrows(
              ServiceException.class, () -> this.doDeleteAccount(caller, toDeleteId));
      Assertions.assertEquals(ServiceException.PERM_DENIED, serviceException.getCode());
      Assertions.assertNotNull(provisioning.getAccountById(toDeleteId));
  }

    @ParameterizedTest
    @MethodSource("getHappyPathCases")
    void shouldDeleteUserThrowsException(Account caller, Account toDelete) throws Exception {
      try (MockedStatic<Files> mockFileSystem = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
        mockFileSystem.when(() -> Files.readString(any())).thenReturn("");
        Mockito.when(mockMessageBrokerClient.publish(any(DeleteUserRequested.class))).thenReturn(true);
        DeleteUserUseCase deleteUserUseCase = Mockito.mock(DeleteUserUseCase.class);

        final String toDeleteId = toDelete.getId();
        Map<String, Object> context = new HashMap<String, Object>();
        ZimbraSoapContext zsc =
            new ZimbraSoapContext(
                AuthProvider.getAuthToken(caller),
                caller.getId(),
                SoapProtocol.Soap12,
                SoapProtocol.Soap12);
        context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
        DeleteAccount deleteAccountHandler =
            new DeleteAccount(
                deleteUserUseCase);
        Mockito.when(deleteUserUseCase.delete(toDeleteId)).thenReturn(Try.failure(new RuntimeException("message")));
        DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest(toDeleteId);
        Element request = JaxbUtil.jaxbToElement(deleteAccountRequest);
        final ServiceException serviceException =
            Assertions.assertThrows(ServiceException.class, () -> deleteAccountHandler.handle(request, context));
        Assertions.assertEquals("service.FAILURE", serviceException.getCode());
        Assertions.assertTrue(serviceException.getMessage().startsWith("system failure: Delete account "));
        Assertions.assertTrue(serviceException.getMessage().endsWith("has an error: message"));
      }
    }
}
