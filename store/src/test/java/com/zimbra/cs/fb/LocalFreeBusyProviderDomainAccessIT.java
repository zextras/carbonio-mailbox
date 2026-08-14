// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.fb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocalFreeBusyProvider - Domain-Level Access Control for Public Free/Busy (Integration Tests)")
class LocalFreeBusyProviderDomainAccessIT extends MailboxTestSuite {

  private Domain testDomain;
  private Account testAccount;
  private Provisioning provisioning;

  @BeforeEach
  void setUp() throws Exception {
    provisioning = Provisioning.getInstance();

    // Create test domain
    testDomain = provisioning.createDomain(
        UUID.randomUUID() + ".test.com",
        new HashMap<>());

    // Create test account
    testAccount = provisioning.createAccount(
        "fbtest." + UUID.randomUUID() + "@" + testDomain.getDomainName(),
        "testPassword",
        new HashMap<>());
  }

  @Test
  @DisplayName("Should allow public access when domain attribute is TRUE")
  void testPublicAccessAllowedWhenDomainAttributeTrue() throws ServiceException {
    // Given: Domain attribute set to TRUE
    provisioning.modifyAttrs(
        testDomain,
        attrs(Provisioning.A_carbonioPublicFreeBusyAllowed, "TRUE"));

    // Verify attribute is set correctly
    Domain reloadedDomain = provisioning.getDomain(testAccount);
    boolean isPublicFreeBusyAllowed = reloadedDomain.getBooleanAttr(
        Provisioning.A_carbonioPublicFreeBusyAllowed, false);
    assertTrue(isPublicFreeBusyAllowed, "Domain attribute should be TRUE");

    // When: Getting free/busy as anonymous (GuestAccount)
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    long startTime = System.currentTimeMillis();
    long endTime = startTime + 3600000;

    FreeBusy result = LocalFreeBusyProvider.getFreeBusyList(
        com.zimbra.cs.account.GuestAccount.ANONYMOUS_ACCT,
        false,
        mailbox,
        testAccount.getName(),
        startTime,
        endTime,
        FreeBusyQuery.CALENDAR_FOLDER_ALL,
        null);

    // Then: Should return FreeBusy object (access allowed)
    assertNotNull(result, "FreeBusy result should not be null when domain attribute is TRUE");
    assertEquals(result.getName(), testAccount.getName(), "FreeBusy name should match account");
  }

  @Test
  @DisplayName("Should deny public access when domain attribute is FALSE")
  void testPublicAccessDeniedWhenDomainAttributeFalse() throws ServiceException {
    // Given: Domain attribute set to FALSE
    provisioning.modifyAttrs(
        testDomain,
        attrs(Provisioning.A_carbonioPublicFreeBusyAllowed, "FALSE"));

    // Verify attribute is set correctly
    Domain reloadedDomain = provisioning.getDomain(testAccount);
    boolean isPublicFreeBusyAllowed = reloadedDomain.getBooleanAttr(
        Provisioning.A_carbonioPublicFreeBusyAllowed, true);
    assertFalse(isPublicFreeBusyAllowed, "Domain attribute should be FALSE");

    // When: Getting free/busy as anonymous (GuestAccount)
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    long startTime = System.currentTimeMillis();
    long endTime = startTime + 3600000;

    FreeBusy result = LocalFreeBusyProvider.getFreeBusyList(
        com.zimbra.cs.account.GuestAccount.ANONYMOUS_ACCT,
        false,
        mailbox,
        testAccount.getName(),
        startTime,
        endTime,
        FreeBusyQuery.CALENDAR_FOLDER_ALL,
        null);

    // Then: Should return FreeBusy but with no busy data (access denied)
    assertNotNull(result, "FreeBusy result should not be null");
    String vCalendar = result.toVCalendar(FreeBusy.Method.PUBLISH, testAccount.getName(), null, null, null);
    assertFalse(vCalendar.contains("FREEBUSY;FBTYPE="), "Denied access should not expose busy intervals");
  }

  @Test
  @DisplayName("Should deny public access when domain attribute not explicitly set")
  void testPublicAccessDeniedWhenDomainAttributeNotSet() throws ServiceException {
    // Given: Domain attribute NOT explicitly set (will use default FALSE)
    // Domain created without setting the attribute explicitly
    Domain reloadedDomain = provisioning.getDomain(testAccount);
    String attrValue = reloadedDomain.getAttr(Provisioning.A_carbonioPublicFreeBusyAllowed, false);
    assertNull(attrValue, "Domain attribute should not be explicitly set");

    // When: Getting free/busy as anonymous (GuestAccount)
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    long startTime = System.currentTimeMillis();
    long endTime = startTime + 3600000;

    FreeBusy result = LocalFreeBusyProvider.getFreeBusyList(
        com.zimbra.cs.account.GuestAccount.ANONYMOUS_ACCT,
        false,
        mailbox,
        testAccount.getName(),
        startTime,
        endTime,
        FreeBusyQuery.CALENDAR_FOLDER_ALL,
        null);

    // Then: Should deny access (default FALSE)
    assertNotNull(result, "FreeBusy result should not be null");
    String vCalendar = result.toVCalendar(FreeBusy.Method.PUBLISH, testAccount.getName(), null, null, null);
    assertFalse(vCalendar.contains("FREEBUSY;FBTYPE="), "Default FALSE should not expose busy intervals");
  }

  @Test
  @DisplayName("Should use global config when domain attribute not set")
  void testGlobalConfigUsedWhenDomainNotSet() throws ServiceException {
    // Given: Global config set to FALSE and domain attribute not explicitly set
    provisioning.modifyAttrs(
        provisioning.getConfig(),
        attrs(Provisioning.A_carbonioPublicFreeBusyAllowed, "FALSE"));

    // Verify global config is set
    Config reloadedConfig = provisioning.getConfig();
    boolean globalValue = reloadedConfig.getBooleanAttr(
        Provisioning.A_carbonioPublicFreeBusyAllowed, true);
    assertFalse(globalValue, "Global config should be FALSE");

    // Domain doesn't override, so will use global config
    Domain reloadedDomain = provisioning.getDomain(testAccount);
    String domainAttrValue = reloadedDomain.getAttr(Provisioning.A_carbonioPublicFreeBusyAllowed, false);
    assertNull(domainAttrValue, "Domain attribute should not be explicitly set");

    // When: Getting free/busy as anonymous (GuestAccount)
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    long startTime = System.currentTimeMillis();
    long endTime = startTime + 3600000;

    FreeBusy result = LocalFreeBusyProvider.getFreeBusyList(
        com.zimbra.cs.account.GuestAccount.ANONYMOUS_ACCT,
        false,
        mailbox,
        testAccount.getName(),
        startTime,
        endTime,
        FreeBusyQuery.CALENDAR_FOLDER_ALL,
        null);

    // Then: Should deny access (uses global config FALSE)
    assertNotNull(result, "FreeBusy result should not be null");
    String vCalendar = result.toVCalendar(FreeBusy.Method.PUBLISH, testAccount.getName(), null, null, null);
    assertFalse(vCalendar.contains("FREEBUSY;FBTYPE="), "Denied access should not expose busy intervals");
  }

  @Test
  @DisplayName("Should allow domain attribute to override global config")
  void testDomainOverridesGlobalConfig() throws ServiceException {
    // Given: Global config set to FALSE, domain attribute set to TRUE
    provisioning.modifyAttrs(
        provisioning.getConfig(),
        attrs(Provisioning.A_carbonioPublicFreeBusyAllowed, "FALSE"));

    provisioning.modifyAttrs(
        testDomain,
        attrs(Provisioning.A_carbonioPublicFreeBusyAllowed, "TRUE"));

    // Verify global is FALSE
    Config reloadedConfig = provisioning.getConfig();
    boolean globalValue = reloadedConfig.getBooleanAttr(
        Provisioning.A_carbonioPublicFreeBusyAllowed, true);
    assertFalse(globalValue, "Global config should be FALSE");

    // Verify domain is TRUE
    Domain reloadedDomain = provisioning.getDomain(testAccount);
    boolean domainValue = reloadedDomain.getBooleanAttr(
        Provisioning.A_carbonioPublicFreeBusyAllowed, false);
    assertTrue(domainValue, "Domain attribute should be TRUE (overrides global)");

    // When: Getting free/busy as anonymous (GuestAccount)
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    long startTime = System.currentTimeMillis();
    long endTime = startTime + 3600000;

    FreeBusy result = LocalFreeBusyProvider.getFreeBusyList(
        com.zimbra.cs.account.GuestAccount.ANONYMOUS_ACCT,
        false,
        mailbox,
        testAccount.getName(),
        startTime,
        endTime,
        FreeBusyQuery.CALENDAR_FOLDER_ALL,
        null);

    // Then: Should allow access (domain TRUE overrides global FALSE)
    assertNotNull(result, "FreeBusy result should not be null");
    assertEquals(result.getName(), testAccount.getName(),
        "FreeBusy name should match account (domain overrides global)");
  }

  @Test
  @DisplayName("Should not check domain when requester is authenticated")
  void testAuthenticatedAccessNotCheckedByDomain() throws Exception {
    // Given: Domain attribute set to FALSE, but we're using an authenticated account
    provisioning.modifyAttrs(
        testDomain,
        attrs(Provisioning.A_carbonioPublicFreeBusyAllowed, "FALSE"));

    // Create another account to query as authenticated user
    Account queryingAccount = provisioning.createAccount(
        "query." + UUID.randomUUID() + "@" + testDomain.getDomainName(),
        "testPassword",
        new HashMap<>());

    // When: Getting free/busy as authenticated account (not anonymous)
    Mailbox targetMailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    long startTime = System.currentTimeMillis();
    long endTime = startTime + 3600000;

    FreeBusy result = LocalFreeBusyProvider.getFreeBusyList(
        queryingAccount, // Authenticated account, not GuestAccount
        false,
        targetMailbox,
        testAccount.getName(),
        startTime,
        endTime,
        FreeBusyQuery.CALENDAR_FOLDER_ALL,
        null);

    // Then: Should allow access (domain check only applies to anonymous)
    assertNotNull(result, "FreeBusy result should not be null for authenticated user");
    assertEquals(result.getName(), testAccount.getName(),
        "Authenticated user should not be restricted by domain attribute");
  }

  private static HashMap<String, Object> attrs(String key, Object value) {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put(key, value);
    return attrs;
  }
}
