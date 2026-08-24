/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CosServiceCountIT extends MailboxTestSuite {

  private final CosService cosService = new CosService(Provisioning::getInstance);

  @Test
  void sumsTheSameCosAcrossEveryDomain() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createAccountOn(cos, createDomain().getName());

    final Map<String, Long> counts = countCos(cos);

    assertEquals(2L, counts.get(cos.getId()));
  }

  @Test
  void reportsZeroForACosWithNoAccounts() throws Exception {
    final Cos cos = createCos();

    final Map<String, Long> counts = countCos(cos);

    assertEquals(0L, counts.get(cos.getId()));
  }

  @Test
  void ignoresCosesThatWereNotAskedAbout() throws Exception {
    final Cos requested = createCos();
    final Cos ignored = createCos();
    createAccountOn(requested, DEFAULT_DOMAIN_NAME);
    createAccountOn(ignored, DEFAULT_DOMAIN_NAME);

    final Map<String, Long> counts = countCos(requested);

    assertEquals(Map.of(requested.getId(), 1L), counts);
  }

  @Test
  void matchesCosIdsExactly() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    final String upperCaseId = cos.getId().toUpperCase(Locale.ROOT);

    final Map<String, Long> counts = cosService.countCos(List.of(upperCaseId)).get();

    assertEquals(0L, counts.get(upperCaseId));
  }

  @Test
  void countsEveryCosWhenNoneIsRequested() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);

    final Map<String, Long> counts = cosService.countCos(List.of()).get();

    assertEquals(everyCosId(), counts.keySet());
    assertEquals(1L, counts.get(cos.getId()));
  }

  @Test
  void reportsZeroForAnUnusedCosWhenNoneIsRequested() throws Exception {
    final Cos unused = createCos();

    final Map<String, Long> counts = cosService.countCos(List.of()).get();

    assertEquals(0L, counts.get(unused.getId()));
  }

  @Test
  void skipsSystemAccounts() throws Exception {
    assertOnlyTheOrdinaryAccountCounts(Provisioning.A_zimbraIsSystemAccount, "TRUE");
  }

  @Test
  void skipsSystemResources() throws Exception {
    assertOnlyTheOrdinaryAccountCounts(Provisioning.A_zimbraIsSystemResource, "TRUE");
  }

  @Test
  void skipsExternalVirtualAccounts() throws Exception {
    assertOnlyTheOrdinaryAccountCounts(Provisioning.A_zimbraIsExternalVirtualAccount, "TRUE");
  }

  @Test
  void skipsClosedAccounts() throws Exception {
    assertOnlyTheOrdinaryAccountCounts(
        Provisioning.A_zimbraAccountStatus, Provisioning.ACCOUNT_STATUS_CLOSED);
  }

  @Test
  void skipsAccountsUnderMaintenance() throws Exception {
    assertOnlyTheOrdinaryAccountCounts(
        Provisioning.A_zimbraAccountStatus, Provisioning.ACCOUNT_STATUS_MAINTENANCE);
  }

  @Test
  void countsLockedAndPendingAccounts() throws Exception {
    final Cos cos = createCos();
    createAccountWith(
        cos, DEFAULT_DOMAIN_NAME, Provisioning.A_zimbraAccountStatus,
        Provisioning.ACCOUNT_STATUS_LOCKED);
    createAccountWith(
        cos, DEFAULT_DOMAIN_NAME, Provisioning.A_zimbraAccountStatus,
        Provisioning.ACCOUNT_STATUS_PENDING);

    final Map<String, Long> counts = countCos(cos);

    assertEquals(2L, counts.get(cos.getId()));
  }

  @Test
  void skipsCalendarResources() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createCalendarResourceOn(cos);

    final Map<String, Long> counts = countCos(cos);

    assertEquals(1L, counts.get(cos.getId()));
  }

  @Test
  void readsTheAccountStatusWithoutTheDomainOverride() throws Exception {
    final Cos cos = createCos();
    final Domain domain = createDomain();
    createAccountOn(cos, domain.getName());
    close(domain);

    final Map<String, Long> counts = countCos(cos);

    assertEquals(1L, counts.get(cos.getId()));
  }

  private void assertOnlyTheOrdinaryAccountCounts(String attribute, String value)
      throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createAccountWith(cos, DEFAULT_DOMAIN_NAME, attribute, value);

    final Map<String, Long> counts = countCos(cos);

    assertEquals(1L, counts.get(cos.getId()));
  }

  private Map<String, Long> countCos(Cos cos) throws Exception {
    return cosService.countCos(List.of(cos.getId())).get();
  }

  private static Cos createCos() throws ServiceException {
    return Provisioning.getInstance().createCos("cos-" + UUID.randomUUID(), new HashMap<>());
  }

  private static Domain createDomain() throws ServiceException {
    return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
  }

  private static void close(Domain domain) throws ServiceException {
    final Map<String, Object> attrs = new HashMap<>();
    attrs.put(Provisioning.A_zimbraDomainStatus, Provisioning.DOMAIN_STATUS_CLOSED);
    Provisioning.getInstance().modifyAttrs(domain, attrs);
  }

  private static Account createAccountOn(Cos cos, String domainName) throws ServiceException {
    return createAccount()
        .withDomain(domainName)
        .withAttribute(Provisioning.A_zimbraCOSId, cos.getId())
        .create();
  }

  private static void createCalendarResourceOn(Cos cos) throws ServiceException {
    final Map<String, Object> attrs = new HashMap<>();
    attrs.put(Provisioning.A_zimbraCalResType, "Location");
    attrs.put(Provisioning.A_displayName, "Room " + UUID.randomUUID());
    attrs.put(Provisioning.A_zimbraCOSId, cos.getId());
    Provisioning.getInstance()
        .createCalendarResource(
            "room-" + UUID.randomUUID() + "@" + DEFAULT_DOMAIN_NAME, "testPassword", attrs);
  }

  private static Account createAccountWith(
      Cos cos, String domainName, String attribute, String value) throws ServiceException {
    return createAccount()
        .withDomain(domainName)
        .withAttribute(Provisioning.A_zimbraCOSId, cos.getId())
        .withAttribute(attribute, value)
        .create();
  }

  private static Set<String> everyCosId() throws ServiceException {
    return Provisioning.getInstance().getAllCos().stream()
        .map(Cos::getId)
        .collect(Collectors.toSet());
  }
}
