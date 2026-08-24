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

    final Map<String, Long> counts =
        cosService.countCos(List.of(upperCaseId), AccountFilter.ANY).get();

    assertEquals(0L, counts.get(upperCaseId));
  }

  @Test
  void countsEveryCosWhenNoneIsRequested() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);

    final Map<String, Long> counts = cosService.countCos(List.of(), AccountFilter.ANY).get();

    assertEquals(everyCosId(), counts.keySet());
    assertEquals(1L, counts.get(cos.getId()));
  }

  @Test
  void reportsZeroForAnUnusedCosWhenNoneIsRequested() throws Exception {
    final Cos unused = createCos();

    final Map<String, Long> counts = cosService.countCos(List.of(), AccountFilter.ANY).get();

    assertEquals(0L, counts.get(unused.getId()));
  }

  @Test
  void skipsSystemResources() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createAccountWith(cos, DEFAULT_DOMAIN_NAME, Provisioning.A_zimbraIsSystemResource, "TRUE");

    final Map<String, Long> counts = countCos(cos);

    assertEquals(1L, counts.get(cos.getId()));
  }

  @Test
  void skipsExternalVirtualAccounts() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createAccountWith(
        cos, DEFAULT_DOMAIN_NAME, Provisioning.A_zimbraIsExternalVirtualAccount, "TRUE");

    final Map<String, Long> counts = countCos(cos);

    assertEquals(1L, counts.get(cos.getId()));
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
  void countsEveryStatusWhenNoneIsExcluded() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createClosedAccountOn(cos, DEFAULT_DOMAIN_NAME);

    final Map<String, Long> counts = countCos(cos);

    assertEquals(2L, counts.get(cos.getId()));
  }

  @Test
  void skipsAccountsWithAnExcludedStatus() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createClosedAccountOn(cos, DEFAULT_DOMAIN_NAME);

    final Map<String, Long> counts = countCos(cos, Provisioning.ACCOUNT_STATUS_CLOSED);

    assertEquals(1L, counts.get(cos.getId()));
  }

  @Test
  void skipsEveryExcludedStatus() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createClosedAccountOn(cos, DEFAULT_DOMAIN_NAME);
    createAccountWith(
        cos,
        DEFAULT_DOMAIN_NAME,
        Provisioning.A_zimbraAccountStatus,
        Provisioning.ACCOUNT_STATUS_MAINTENANCE);

    final Map<String, Long> counts =
        countCos(cos, Provisioning.ACCOUNT_STATUS_CLOSED, Provisioning.ACCOUNT_STATUS_MAINTENANCE);

    assertEquals(1L, counts.get(cos.getId()));
  }

  @Test
  void readsAnAccountInAClosedDomainAsClosed() throws Exception {
    final Cos cos = createCos();
    final Domain domain = createDomain();
    createAccountOn(cos, domain.getName());
    close(domain);

    assertEquals(1L, countCos(cos).get(cos.getId()));
    assertEquals(0L, countCos(cos, Provisioning.ACCOUNT_STATUS_CLOSED).get(cos.getId()));
  }

  @Test
  void keepsTheOwnStatusOfAnAdminAccountInAClosedDomain() throws Exception {
    final Cos cos = createCos();
    final Domain domain = createDomain();
    createAccountWith(cos, domain.getName(), Provisioning.A_zimbraIsAdminAccount, "TRUE");
    close(domain);

    assertEquals(1L, countCos(cos, Provisioning.ACCOUNT_STATUS_CLOSED).get(cos.getId()));
    assertEquals(0L, countCos(cos, Provisioning.ACCOUNT_STATUS_ACTIVE).get(cos.getId()));
  }

  private Map<String, Long> countCos(Cos cos, String... excludedAccountStatuses) throws Exception {
    return cosService
        .countCos(List.of(cos.getId()), AccountFilter.excluding(List.of(excludedAccountStatuses)))
        .get();
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

  private static Account createClosedAccountOn(Cos cos, String domainName)
      throws ServiceException {
    return createAccountWith(
        cos, domainName, Provisioning.A_zimbraAccountStatus, Provisioning.ACCOUNT_STATUS_CLOSED);
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
