/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchAccountsOptions;
import com.zimbra.cs.account.SearchAccountsOptions.IncludeType;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import io.vavr.control.Try;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CosService {

  private static final String[] COUNT_ATTRS = {
    Provisioning.A_zimbraCOSId,
    Provisioning.A_zimbraAccountStatus,
    Provisioning.A_zimbraIsAdminAccount,
    Provisioning.A_zimbraIsDomainAdminAccount
  };

  private final Supplier<Provisioning> provisioningSupplier;

  public CosService(Supplier<Provisioning> provisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
  }

  public Try<Cos> getCos(String cosId) {
    return Try.of(() -> {
      final Cos cos = provisioningSupplier.get().getCosById(cosId);
      if (cos == null) {
        throw ServiceException.NOT_FOUND("No such COS with ID: " + cosId);
      }
      return cos;
    });
  }

  public Try<Map<String, Long>> countCos(Collection<String> cosIds, AccountFilter accountFilter) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final Map<String, Long> counts = new LinkedHashMap<>();
      for (final String cosId :
          cosIds == null || cosIds.isEmpty() ? allCosIds(provisioning) : cosIds) {
        counts.put(cosId, 0L);
      }

      for (final Domain domain : provisioning.getAllDomains()) {
        countAccountsIn(domain, provisioning, accountFilter, counts);
      }
      return counts;
    });
  }

  private static void countAccountsIn(
      Domain domain, Provisioning provisioning, AccountFilter accountFilter,
      Map<String, Long> counts) throws ServiceException {
    provisioning.searchDirectory(countableAccountsIn(domain), entry -> {
      if (!(entry instanceof Account account) || !accountFilter.matches(account, provisioning)) {
        return;
      }
      counts.computeIfPresent(provisioning.getCOS(account).getId(), (id, count) -> count + 1);
    });
  }

  /** The accounts a licence counts: no calendar resources, system resources or external guests. */
  private static SearchAccountsOptions countableAccountsIn(Domain domain) {
    final SearchAccountsOptions options = new SearchAccountsOptions(domain, COUNT_ATTRS);
    options.setIncludeType(IncludeType.ACCOUNTS_ONLY);
    options.setFilter(ZLdapFilterFactory.getInstance().allNonSystemInternalAccounts());
    return options;
  }

  private static List<String> allCosIds(Provisioning provisioning) throws ServiceException {
    return provisioning.getAllCos().stream().map(Cos::getId).toList();
  }
}
