/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;



public class AccountService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final MailboxService mailboxService;

  public AccountService(Supplier<Provisioning> provisioningSupplier, MailboxService mailboxService) {
    this.provisioningSupplier = provisioningSupplier;
    this.mailboxService = mailboxService;
  }

  public Try<Account> getAccount(String accountId) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      Account accountById = provisioning.getAccountById(accountId);
      if (accountById == null) {
        throw ServiceException.NOT_FOUND("No such account with ID: " + accountId);
      }
      return accountById;
    });
  }

  public Try<Account> getAccountByAuthToken(String encodedAuthToken) {
    return Try.of(() -> {
      final AuthToken authToken = ZimbraAuthToken.getAuthToken(encodedAuthToken);
      if (authToken.isExpired()) {
        throw ServiceException.AUTH_EXPIRED("Auth token expired");
      }
      return authToken.getAccount();
    });
  }

  public Try<Tuple2<Account, AuthToken>> getAccountAndAuthToken(String encodedAuthToken) {
    return Try.of(() -> {
      final AuthToken authToken = ZimbraAuthToken.getAuthToken(encodedAuthToken);
      if (authToken.isExpired()) {
        throw ServiceException.AUTH_EXPIRED("Auth token expired");
      }
      return Tuple.of(authToken.getAccount(), authToken);
    });
  }

  public Try<Account> getAccountByEmail(String email) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final Account account = provisioning.get(AccountBy.name, email);
      if (account == null) {
        throw ServiceException.NOT_FOUND("No such account with email: " + email);
      }
      return account;
    });
  }

  public Try<List<Account>> getAccounts(List<String> ids) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final List<Account> result = new ArrayList<>();
      for (final String id : ids) {
        final Account account = provisioning.get(AccountBy.id, id);
        if (account != null) {
          result.add(account);
        }
      }
      return result;
    });
  }

  public Try<List<Account>> getAccountsByEmails(List<String> emails) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final List<Account> result = new ArrayList<>();
      for (final String email : emails) {
        final Account account = provisioning.get(AccountBy.name, email);
        if (account != null) {
          result.add(account);
        }
      }
      return result;
    });
  }

  public Try<List<Account>> getSharedAccounts(String accountId) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final Account account = provisioning.getAccountById(accountId);
      if (account == null) {
        throw ServiceException.NOT_FOUND("No such account with ID: " + accountId);
      }
      final Set<String> ownerIds = mailboxService.getShareInfo(account).stream()
          .map(sid -> sid.getOwnerAcctId())
          .filter(ownerId -> !ownerId.equals(account.getId()))
          .collect(Collectors.toSet());
      final List<Account> result = new ArrayList<>();
      for (final String ownerId : ownerIds) {
        final Account owner = provisioning.getAccountById(ownerId);
        if (owner != null) {
          result.add(owner);
        }
      }
      return result;
    });
  }
}
