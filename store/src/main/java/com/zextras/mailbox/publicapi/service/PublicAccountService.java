/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi.service;

import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.authz.service.RightsService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import io.vavr.control.Try;

public class PublicAccountService {

  private static final String GET_ACCOUNT_INFO = "getAccountInfo";

  private final AccountService accountService;
  private final RightsService rightsService;

  public PublicAccountService(AccountService accountService, RightsService rightsService) {
    this.accountService = accountService;
    this.rightsService = rightsService;
  }

  public Try<AuthToken> getMyAuthToken(String encodedToken) {
    return accountService.getAuthToken(encodedToken);
  }

  public Try<Account> getAccount(String encodedToken, String accountId) {
    return accountService.getAuthToken(encodedToken)
        .flatMap(authToken -> canRead(authToken, encodedToken, accountId))
        .flatMap(allowed -> allowed
            ? accountService.getAccount(accountId)
            : Try.failure(ServiceException.PERM_DENIED("Cannot read account " + accountId)));
  }

  private Try<Boolean> canRead(AuthToken authToken, String encodedToken, String accountId) {
    return accountId.equals(authToken.getAccountId())
        ? Try.success(true)
        : rightsService.tokenHasRight(encodedToken, GET_ACCOUNT_INFO, "account", accountId);
  }
}
