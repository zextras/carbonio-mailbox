// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.account.cos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link CosAccountRepository}.
 *
 * <p>Used as the safe default when no Postgres pool is wired (tests, and deployments with an empty
 * {@code postgres_jdbc_url}). Counting works within the process but is not persisted.
 */
public class InMemoryCosAccountRepository implements CosAccountRepository {

  private final Map<String, String> accountToCos = new ConcurrentHashMap<>();

  @Override
  public void assign(String accountId, String cosId) {
    accountToCos.put(accountId, cosId);
  }

  @Override
  public void remove(String accountId) {
    accountToCos.remove(accountId);
  }

  @Override
  public long countByCos(String cosId) {
    return accountToCos.values().stream().filter(cosId::equals).count();
  }

  @Override
  public Map<String, Long> countAllByCos() {
    Map<String, Long> result = new HashMap<>();
    for (String cosId : accountToCos.values()) {
      result.merge(cosId, 1L, Long::sum);
    }
    return result;
  }

  @Override
  public void replaceAll(Map<String, String> accountIdToCosId) {
    accountToCos.clear();
    accountToCos.putAll(accountIdToCosId);
  }
}
