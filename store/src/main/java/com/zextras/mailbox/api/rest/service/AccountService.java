/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CountAccountResult.CountAccountByCos;
import com.zimbra.cs.account.ShareInfoData;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.mailbox.Mailbox;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;



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

  public Try<AuthToken> getAuthToken(String encodedAuthToken) {
    return Try.of(() -> {
      final AuthToken authToken = ZimbraAuthToken.getAuthToken(encodedAuthToken);
      if (authToken.isExpired()) {
        throw ServiceException.AUTH_EXPIRED("Auth token expired");
      }
      if (!authToken.isRegistered()) {
        throw ServiceException.AUTH_EXPIRED("Auth token is no longer valid");
      }
      return authToken;
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

  /**
   * Counts the accounts in each of the given COSes, using the same per-domain tally the license
   * counts with: an account that inherits the domain or default COS is attributed to that COS
   * rather than dropped.
   *
   * @return a count for every requested COS, zero for those with no accounts. COS ids are matched
   *         case-insensitively, and each key is returned as the caller spelled it.
   */
  public Try<Map<String, Long>> countAccountsByCos(Collection<String> cosIds) {
    return Try.of(() -> {
      final Map<String, String> requestedByLowercase = new LinkedHashMap<>();
      for (final String cosId : cosIds) {
        requestedByLowercase.put(cosId.toLowerCase(Locale.ROOT), cosId);
      }

      final Map<String, Long> counts = new LinkedHashMap<>();
      cosIds.forEach(cosId -> counts.put(cosId, 0L));
      if (requestedByLowercase.isEmpty()) {
        return counts;
      }

      final Provisioning provisioning = provisioningSupplier.get();
      for (final Domain domain : provisioning.getAllDomains()) {
        for (final CountAccountByCos countByCos :
            provisioning.countAccount(domain).getCountAccountByCos()) {
          final String cosId = countByCos.getCosId();
          if (cosId == null) {
            continue;
          }
          final String requested = requestedByLowercase.get(cosId.toLowerCase(Locale.ROOT));
          if (requested != null) {
            counts.merge(requested, countByCos.getCount(), Long::sum);
          }
        }
      }
      return counts;
    });
  }

  public Try<List<Account>> getSharedAccounts(String accountId) {
    return getAccount(accountId)
        .mapTry(this::getSharedAccountIds)
        .flatMap(this::getAccounts);
  }

  private List<String> getSharedAccountIds(Account account) throws ServiceException {
		return mailboxService.getShareInfo(account).stream()
				.filter(sid -> sid.getItemId() == Mailbox.ID_FOLDER_USER_ROOT)
				.map(ShareInfoData::getOwnerAcctId)
				.filter(ownerId -> !ownerId.equals(account.getId()))
        .toList();
  }
}
