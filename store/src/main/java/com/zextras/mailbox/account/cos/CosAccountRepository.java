// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.account.cos;

import java.util.Map;

/**
 * Read/write projection of {@code account -> effective COS id}, used for fast counts.
 *
 * <p>LDAP remains the authoritative store; this projection is derived from it and is only used to
 * answer "how many accounts are in a COS" without scanning the directory.
 */
public interface CosAccountRepository {

  /** Insert or update the effective COS of an account (createAccount / setCOS). */
  void assign(String accountId, String cosId);

  /** Remove an account from the projection (deleteAccount). */
  void remove(String accountId);

  /** Count accounts in a single COS. */
  long countByCos(String cosId);

  /** Count accounts grouped by COS id: {@code cosId -> count}. */
  Map<String, Long> countAllByCos();

  /** Atomically replace the whole projection — used by the initial backfill / reconcile. */
  void replaceAll(Map<String, String> accountIdToCosId);
}
