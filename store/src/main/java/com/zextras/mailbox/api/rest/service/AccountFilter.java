/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Properties that keep an account out of a count. Empty sets exclude nothing. */
public record AccountFilter(Set<String> excludedAccountStatuses) {

  public static final AccountFilter ANY = new AccountFilter(Set.of());

  public AccountFilter {
    excludedAccountStatuses = Set.copyOf(excludedAccountStatuses);
  }

  public static AccountFilter excluding(Collection<String> accountStatuses)
      throws ServiceException {
    if (accountStatuses == null || accountStatuses.isEmpty()) {
      return ANY;
    }
    final Set<String> known = new LinkedHashSet<>();
    for (final String accountStatus : accountStatuses) {
      known.add(AccountStatus.fromString(accountStatus).toString());
    }
    return new AccountFilter(known);
  }

  boolean matches(Account account, Provisioning provisioning) {
    return excludedAccountStatuses.isEmpty()
        || !excludedAccountStatuses.contains(account.getAccountStatus(provisioning));
  }
}
