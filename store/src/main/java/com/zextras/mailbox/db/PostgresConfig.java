// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.db;

import com.zimbra.common.localconfig.LC;

/** Connection settings for the provisioning-side Postgres pool. */
public record PostgresConfig(
    String jdbcUrl, String username, String password, int maxPoolSize, int minIdle) {

  /** Reads the settings from LocalConfig. A blank {@link #jdbcUrl()} means the pool is disabled. */
  public static PostgresConfig fromLocalConfig() {
    return new PostgresConfig(
        LC.postgres_jdbc_url.value(),
        LC.postgres_user.value(),
        LC.postgres_password.value(),
        LC.postgres_pool_max.intValue(),
        LC.postgres_pool_min_idle.intValue());
  }

  /** True when a jdbc url is configured; otherwise the pool must not attempt to connect. */
  public boolean isEnabled() {
    return jdbcUrl != null && !jdbcUrl.isBlank();
  }
}
