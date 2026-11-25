// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.ZimbraCustomAuth;
import com.zimbra.cs.service.account.Auth;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthRequestEnumerationTest extends MailboxTestSuite {

  private static final String PASSWORD = "rightPassword";
  private static final String WRONG_PASSWORD = "wrongPassword";
  private static final String UNKNOWN_ACCOUNT = "unknown@" + DEFAULT_DOMAIN_NAME;
  private static final String CUSTOM_AUTH_DOMAIN = "co4784-custom-auth.com";
  private static final String REJECTING_HANDLER = "co4784RejectingHandler";

  private static Map<String, Object> requestContext;

  @BeforeAll
  static void setUp() throws Exception {
    ZimbraCustomAuth.register(
        REJECTING_HANDLER,
        new ZimbraCustomAuth() {
          @Override
          public void authenticate(
              Account acct, String password, Map<String, Object> context, List<String> args)
              throws Exception {
            throw new Exception("Invalid Credentials");
          }
        });
    Provisioning.getInstance().createDomain(CUSTOM_AUTH_DOMAIN, new HashMap<>(Map.of(
        ZAttrProvisioning.A_zimbraAuthMech, "custom:" + REJECTING_HANDLER,
        ZAttrProvisioning.A_zimbraAuthFallbackToLocal, "FALSE")));
    requestContext = ServiceTestUtil.getRequestContext(createAccount().create());
  }

  @Test
  void unknownAccountReturnsAuthFailed() {
    final ServiceException fault = accountAuth(UNKNOWN_ACCOUNT, WRONG_PASSWORD);

    assertEquals("account.AUTH_FAILED", fault.getCode());
    assertEquals("authentication failed for [unknown@test.com]", fault.getMessage());
  }

  @Test
  void wrongPasswordReturnsAuthFailed() throws Exception {
    final Account account = createAccount().withPassword(PASSWORD).create();

    final ServiceException fault = accountAuth(account.getName(), WRONG_PASSWORD);

    assertEquals("account.AUTH_FAILED", fault.getCode());
    assertEquals("authentication failed for [" + account.getName() + "]", fault.getMessage());
  }

  @Test
  void customAuthRejectionReturnsAuthFailedWithoutHandlerReason() throws Exception {
    final Account account =
        createAccount().withDomain(CUSTOM_AUTH_DOMAIN).withPassword(PASSWORD).create();

    final ServiceException fault = accountAuth(account.getName(), WRONG_PASSWORD);

    assertEquals("account.AUTH_FAILED", fault.getCode());
    assertEquals("authentication failed for [" + account.getName() + "]", fault.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"maintenance", "locked", "closed", "pending"})
  void inactiveAccountWithWrongPasswordReturnsAuthFailed(String status) throws Exception {
    final Account account = createAccount()
        .withPassword(PASSWORD)
        .withAttribute(ZAttrProvisioning.A_zimbraAccountStatus, status)
        .create();

    final ServiceException fault = accountAuth(account.getName(), WRONG_PASSWORD);

    assertEquals("account.AUTH_FAILED", fault.getCode());
    assertEquals("authentication failed for [" + account.getName() + "]", fault.getMessage());
  }

  @Test
  void maintenanceAccountWithRightPasswordReturnsMaintenanceMode() throws Exception {
    final Account account = createAccount()
        .withPassword(PASSWORD)
        .withAttribute(ZAttrProvisioning.A_zimbraAccountStatus, "maintenance")
        .create();

    final ServiceException fault = accountAuth(account.getName(), PASSWORD);

    assertEquals("account.MAINTENANCE_MODE", fault.getCode());
    assertEquals("account is in maintenance mode", fault.getMessage());
  }

  @Test
  void unknownAccountWithoutPasswordReturnsInvalidRequest() {
    final ServiceException fault = accountAuth(UNKNOWN_ACCOUNT, null);

    assertEquals("service.INVALID_REQUEST", fault.getCode());
    assertEquals("invalid request: must specify password", fault.getMessage());
  }

  @Test
  void existingAccountWithoutPasswordReturnsInvalidRequest() throws Exception {
    final Account account = createAccount().withPassword(PASSWORD).create();

    final ServiceException fault = accountAuth(account.getName(), null);

    assertEquals("service.INVALID_REQUEST", fault.getCode());
    assertEquals("invalid request: must specify password", fault.getMessage());
  }

  @Test
  void adminAuthOfUnknownAccountReturnsAuthFailed() {
    final ServiceException fault = adminAuth(UNKNOWN_ACCOUNT, WRONG_PASSWORD);

    assertEquals("account.AUTH_FAILED", fault.getCode());
    assertEquals("authentication failed for [unknown@test.com]", fault.getMessage());
  }

  @Test
  void adminAuthOfNonAdminAccountReturnsAuthFailed() throws Exception {
    final Account account = createAccount().withPassword(PASSWORD).create();

    final ServiceException fault = adminAuth(account.getName(), WRONG_PASSWORD);

    assertEquals("account.AUTH_FAILED", fault.getCode());
    assertEquals("authentication failed for [" + account.getName() + "]", fault.getMessage());
  }

  private ServiceException accountAuth(String name, String password) {
    final Element request = new Element.JSONElement(AccountConstants.E_AUTH_REQUEST);
    request.addUniqueElement(AccountConstants.E_ACCOUNT)
        .addAttribute(AccountConstants.A_BY, AccountConstants.A_NAME)
        .addText(name);
    if (password != null) {
      request.addUniqueElement(AccountConstants.E_PASSWORD).addText(password);
    }
    return assertThrows(ServiceException.class, () -> new Auth().handle(request, requestContext));
  }

  private ServiceException adminAuth(String name, String password) {
    final Element request = new Element.JSONElement(AdminConstants.E_AUTH_REQUEST);
    request.addUniqueElement(AdminConstants.E_NAME).addText(name);
    request.addUniqueElement(AdminConstants.E_PASSWORD).addText(password);
    return assertThrows(
        ServiceException.class,
        () -> new com.zimbra.cs.service.admin.Auth().handle(request, requestContext));
  }
}
