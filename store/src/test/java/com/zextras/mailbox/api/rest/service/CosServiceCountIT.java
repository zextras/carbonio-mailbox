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
    createAccountOn(cos, createDomain());

    final Map<String, Long> counts = cosService.countCos(List.of(cos.getId())).get();

    assertEquals(2L, counts.get(cos.getId()));
  }

  @Test
  void reportsZeroForACosWithNoAccounts() throws Exception {
    final Cos cos = createCos();

    final Map<String, Long> counts = cosService.countCos(List.of(cos.getId())).get();

    assertEquals(0L, counts.get(cos.getId()));
  }

  @Test
  void ignoresCosesThatWereNotAskedAbout() throws Exception {
    final Cos requested = createCos();
    final Cos ignored = createCos();
    createAccountOn(requested, DEFAULT_DOMAIN_NAME);
    createAccountOn(ignored, DEFAULT_DOMAIN_NAME);

    final Map<String, Long> counts = cosService.countCos(List.of(requested.getId())).get();

    assertEquals(Map.of(requested.getId(), 1L), counts);
  }

  @Test
  void matchesCosIdsRegardlessOfCase() throws Exception {
    final Cos cos = createCos();
    createAccountOn(cos, DEFAULT_DOMAIN_NAME);
    final String upperCaseId = cos.getId().toUpperCase(Locale.ROOT);

    final Map<String, Long> counts = cosService.countCos(List.of(upperCaseId)).get();

    assertEquals(1L, counts.get(upperCaseId));
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

  private static Cos createCos() throws ServiceException {
    return Provisioning.getInstance().createCos("cos-" + UUID.randomUUID(), new HashMap<>());
  }

  private static String createDomain() throws ServiceException {
    return Provisioning.getInstance()
        .createDomain(UUID.randomUUID() + ".com", new HashMap<>())
        .getName();
  }

  private static Account createAccountOn(Cos cos, String domainName) throws ServiceException {
    return createAccount()
        .withDomain(domainName)
        .withAttribute(Provisioning.A_zimbraCOSId, cos.getId())
        .create();
  }

  private static Set<String> everyCosId() throws ServiceException {
    return Provisioning.getInstance().getAllCos().stream()
        .map(Cos::getId)
        .collect(Collectors.toSet());
  }
}
