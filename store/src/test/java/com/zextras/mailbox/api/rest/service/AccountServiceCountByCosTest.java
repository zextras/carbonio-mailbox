/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CountAccountResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AccountServiceCountByCosTest {

  private static final String MAIL_COS = "11111111-1111-1111-1111-111111111111";
  private static final String WORKSPACE_COS = "22222222-2222-2222-2222-222222222222";

  @Test
  void sumsTheSameCosAcrossEveryDomain() throws Exception {
    final AccountService accountService = serviceCounting(
        Map.of("first.example", Map.of(MAIL_COS, 10L, WORKSPACE_COS, 3L),
            "second.example", Map.of(MAIL_COS, 5L)));

    final Map<String, Long> counts =
        accountService.countAccountsByCos(List.of(MAIL_COS, WORKSPACE_COS)).get();

    assertEquals(15L, counts.get(MAIL_COS));
    assertEquals(3L, counts.get(WORKSPACE_COS));
  }

  @Test
  void reportsZeroForACosWithNoAccounts() throws Exception {
    final AccountService accountService = serviceCounting(
        Map.of("first.example", Map.of(MAIL_COS, 10L)));

    final Map<String, Long> counts =
        accountService.countAccountsByCos(List.of(MAIL_COS, WORKSPACE_COS)).get();

    assertEquals(0L, counts.get(WORKSPACE_COS));
  }

  @Test
  void ignoresCosesThatWereNotAskedAbout() throws Exception {
    final AccountService accountService = serviceCounting(
        Map.of("first.example", Map.of(MAIL_COS, 10L, WORKSPACE_COS, 3L)));

    final Map<String, Long> counts = accountService.countAccountsByCos(List.of(MAIL_COS)).get();

    assertEquals(Map.of(MAIL_COS, 10L), counts);
  }

  @Test
  void matchesCosIdsRegardlessOfCase() throws Exception {
    final AccountService accountService = serviceCounting(
        Map.of("first.example", Map.of(MAIL_COS.toUpperCase(), 7L)));

    final Map<String, Long> counts = accountService.countAccountsByCos(List.of(MAIL_COS)).get();

    assertEquals(7L, counts.get(MAIL_COS));
  }

  @Test
  void asksNothingOfProvisioningForAnEmptyRequest() throws Exception {
    final Provisioning provisioning = mock(Provisioning.class);
    final AccountService accountService =
        new AccountService(() -> provisioning, mock(MailboxService.class));

    assertTrue(accountService.countAccountsByCos(List.of()).get().isEmpty());
    // getAllDomains is the expensive part; an empty request must not reach it
    org.mockito.Mockito.verify(provisioning, org.mockito.Mockito.never()).getAllDomains();
  }

  /** @param countsByDomain domain name -> (cos id -> accounts in that COS, in that domain) */
  private static AccountService serviceCounting(Map<String, Map<String, Long>> countsByDomain)
      throws Exception {
    final Provisioning provisioning = mock(Provisioning.class);
    final List<Domain> domains = countsByDomain.keySet().stream().map(name -> {
      final Domain domain = mock(Domain.class);
      when(domain.getName()).thenReturn(name);
      return domain;
    }).toList();

    when(provisioning.getAllDomains()).thenReturn(domains);
    when(provisioning.countAccount(any(Domain.class))).thenAnswer(invocation -> {
      final Domain domain = invocation.getArgument(0);
      final CountAccountResult result = new CountAccountResult();
      countsByDomain.get(domain.getName())
          .forEach((cosId, count) -> result.addCountAccountByCosResult(cosId, cosId, count));
      return result;
    });

    return new AccountService(() -> provisioning, mock(MailboxService.class));
  }
}
