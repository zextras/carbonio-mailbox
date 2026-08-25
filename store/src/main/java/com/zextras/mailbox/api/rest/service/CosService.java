/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchAccountsOptions;
import com.zimbra.cs.account.SearchAccountsOptions.IncludeType;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;
import io.vavr.control.Try;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CosService {

  private static final String COUNTABLE_ACCOUNTS =
      "(&"
          + isNot(ZAttrProvisioning.A_zimbraIsSystemAccount, LdapConstants.LDAP_TRUE)
          + isNot(ZAttrProvisioning.A_zimbraIsSystemResource, LdapConstants.LDAP_TRUE)
          + isNot(ZAttrProvisioning.A_zimbraIsExternalVirtualAccount, LdapConstants.LDAP_TRUE)
          + isNot(ZAttrProvisioning.A_zimbraCalResType, "*")
          + isNot(ZAttrProvisioning.A_zimbraAccountStatus, Provisioning.ACCOUNT_STATUS_CLOSED)
          + isNot(ZAttrProvisioning.A_zimbraAccountStatus, Provisioning.ACCOUNT_STATUS_MAINTENANCE)
          + ")";

  private static final String[] COUNT_ATTRS = {ZAttrProvisioning.A_zimbraCOSId};

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

  public Try<Map<String, Long>> countCos(Collection<String> cosIds) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final Map<String, Long> counts = new LinkedHashMap<>();
      for (final String cosId :
          cosIds == null || cosIds.isEmpty() ? allCosIds(provisioning) : cosIds) {
        counts.put(cosId, 0L);
      }

      for (final Domain domain : provisioning.getAllDomains()) {
        countAccountsIn(domain, provisioning, counts);
      }
      return counts;
    });
  }

  private static void countAccountsIn(
      Domain domain, Provisioning provisioning, Map<String, Long> counts)
      throws ServiceException {
    provisioning.searchDirectory(countableAccountsIn(domain), entry -> {
      if (entry instanceof Account account) {
        counts.computeIfPresent(provisioning.getCOS(account).getId(), (id, count) -> count + 1);
      }
    });
  }

  private static SearchAccountsOptions countableAccountsIn(Domain domain) {
    final SearchAccountsOptions options = new SearchAccountsOptions(domain, COUNT_ATTRS);
    options.setIncludeType(IncludeType.ACCOUNTS_ONLY);
    options.setFilterString(FilterId.ALL_NON_SYSTEM_INTERNAL_ACCOUNTS, COUNTABLE_ACCOUNTS);
    return options;
  }

  private static String isNot(String attribute, String value) {
    return "(!(" + attribute + "=" + value + "))";
  }

  private static List<String> allCosIds(Provisioning provisioning) throws ServiceException {
    return provisioning.getAllCos().stream().map(Cos::getId).toList();
  }
}
