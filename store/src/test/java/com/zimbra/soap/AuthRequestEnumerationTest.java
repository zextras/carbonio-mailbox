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
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.ZimbraCustomAuth;
import com.zimbra.cs.service.account.Auth;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthRequestEnumerationTest extends MailboxTestSuite {

  private static final String PASSWORD = "rightPassword";
  private static final String WRONG_PASSWORD = "wrongPassword";
  private static final String REJECTING_HANDLER = "co4784RejectingHandler";
  private static final String NAME_PLACEHOLDER = "<name>";

  private static Map<String, Object> requestContext;

  record Fault(String code, String message, boolean receiversFault, List<String> args) {}

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
    requestContext = ServiceTestUtil.getRequestContext(createAccount().create());
  }

  @Test
  void wrongPasswordFaultMatchesUnknownAccountFault() throws Exception {
    final Account account = createAccount().withPassword(PASSWORD).create();

    assertEquals(
        unknownAccountFault(DEFAULT_DOMAIN_NAME),
        accountAuthFault(account.getName(), WRONG_PASSWORD));
  }

  @Test
  void customAuthRejectionFaultMatchesUnknownAccountFault() throws Exception {
    final String domainName = createDomain(Map.<String, Object>of(
        ZAttrProvisioning.A_zimbraAuthMech, "custom:" + REJECTING_HANDLER,
        ZAttrProvisioning.A_zimbraAuthFallbackToLocal, "FALSE"));
    final Account account = createAccount().withDomain(domainName).withPassword(PASSWORD).create();

    assertEquals(
        unknownAccountFault(domainName), accountAuthFault(account.getName(), WRONG_PASSWORD));
  }

  @ParameterizedTest
  @ValueSource(strings = {"maintenance", "locked", "closed", "pending"})
  void inactiveAccountWithWrongPasswordFaultMatchesUnknownAccountFault(String status)
      throws Exception {
    final Account account = createAccount()
        .withPassword(PASSWORD)
        .withAttribute(ZAttrProvisioning.A_zimbraAccountStatus, status)
        .create();

    assertEquals(
        unknownAccountFault(DEFAULT_DOMAIN_NAME),
        accountAuthFault(account.getName(), WRONG_PASSWORD));
  }

  @Test
  void maintenanceAccountWithRightPasswordStillGetsMaintenanceFault() throws Exception {
    final Account account = createAccount()
        .withPassword(PASSWORD)
        .withAttribute(ZAttrProvisioning.A_zimbraAccountStatus, "maintenance")
        .create();

    assertEquals(
        AccountServiceException.MAINTENANCE_MODE,
        accountAuthFault(account.getName(), PASSWORD).code());
  }

  @Test
  void missingPasswordFaultIsTheSameForExistingAndUnknownAccount() throws Exception {
    final Account account = createAccount().withPassword(PASSWORD).create();

    assertEquals(
        accountAuthFault(unknownName(DEFAULT_DOMAIN_NAME), null),
        accountAuthFault(account.getName(), null));
  }

  @Test
  void adminAuthOfNonAdminAccountFaultMatchesUnknownAccountFault() throws Exception {
    final Account account = createAccount().withPassword(PASSWORD).create();
    final String unknownName = unknownName(DEFAULT_DOMAIN_NAME);

    assertEquals(
        normalize(adminAuthFault(unknownName, WRONG_PASSWORD), unknownName),
        normalize(adminAuthFault(account.getName(), WRONG_PASSWORD), account.getName()));
  }

  private Fault unknownAccountFault(String domainName) throws Exception {
    final String unknownName = unknownName(domainName);
    final Fault fault = accountAuthFault(unknownName, WRONG_PASSWORD);
    assertEquals(AccountServiceException.AUTH_FAILED, fault.code());
    return normalize(fault, unknownName);
  }

  private Fault accountAuthFault(String name, String password) throws Exception {
    final Element request = new Element.JSONElement(AccountConstants.E_AUTH_REQUEST);
    request.addUniqueElement(AccountConstants.E_ACCOUNT)
        .addAttribute(AccountConstants.A_BY, AccountConstants.A_NAME)
        .addText(name);
    if (password != null) {
      request.addUniqueElement(AccountConstants.E_PASSWORD).addText(password);
    }
    final ServiceException exception =
        assertThrows(ServiceException.class, () -> new Auth().handle(request, requestContext));
    return normalize(toFault(exception), name);
  }

  private Fault adminAuthFault(String name, String password) {
    final Element request = new Element.JSONElement(AdminConstants.E_AUTH_REQUEST);
    request.addUniqueElement(AdminConstants.E_NAME).addText(name);
    request.addUniqueElement(AdminConstants.E_PASSWORD).addText(password);
    final ServiceException exception = assertThrows(
        ServiceException.class,
        () -> new com.zimbra.cs.service.admin.Auth().handle(request, requestContext));
    return toFault(exception);
  }

  private static Fault toFault(ServiceException exception) {
    return new Fault(
        exception.getCode(),
        exception.getMessage(),
        exception.isReceiversFault(),
        exception.getArgs().stream()
            .filter(ServiceException.Argument::externalVisible)
            .map(ServiceException.Argument::toString)
            .collect(Collectors.toList()));
  }

  private static Fault normalize(Fault fault, String name) {
    return new Fault(
        fault.code(),
        fault.message().replace(name, NAME_PLACEHOLDER),
        fault.receiversFault(),
        fault.args());
  }

  private static String unknownName(String domainName) {
    return "unknown-" + UUID.randomUUID() + "@" + domainName;
  }

  private static String createDomain(Map<String, Object> attributes) throws ServiceException {
    final String domainName = UUID.randomUUID() + ".com";
    Provisioning.getInstance().createDomain(domainName, new HashMap<>(attributes));
    return domainName;
  }
}
