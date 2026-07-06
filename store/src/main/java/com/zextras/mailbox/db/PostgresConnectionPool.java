// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/**
 * Owns the HikariCP-backed Postgres pool.
 *
 * <p>One instance per process, injected — never a static singleton. The underlying {@link
 * HikariDataSource} is created lazily on the first {@link #dataSource()} call, so merely
 * constructing the pool (which {@code Provisioning} does on every init) does not open any
 * connection. This keeps server start-up and tests free of Postgres unless the projection is
 * actually queried.
 */
public final class PostgresConnectionPool implements AutoCloseable {

  private final PostgresConfig config;
  private volatile HikariDataSource dataSource;

  public PostgresConnectionPool(PostgresConfig config) {
    this.config = config;
  }

  public static PostgresConnectionPool fromLocalConfig() {
    return new PostgresConnectionPool(PostgresConfig.fromLocalConfig());
  }

  /**
   * @return the live pooled {@link DataSource}
   * @throws IllegalStateException if no jdbc url is configured
   */
  public DataSource dataSource() {
    HikariDataSource local = dataSource;
    if (local == null) {
      synchronized (this) {
        local = dataSource;
        if (local == null) {
          if (!config.isEnabled()) {
            throw new IllegalStateException(
                "Postgres pool is not configured (localconfig postgres_jdbc_url is empty)");
          }
          local = build(config);
          dataSource = local;
        }
      }
    }
    return local;
  }

  public boolean isEnabled() {
    return config.isEnabled();
  }

  private static HikariDataSource build(PostgresConfig config) {
    HikariConfig hikari = new HikariConfig();
    hikari.setPoolName("carbonio-provisioning-pg");
    hikari.setJdbcUrl(config.jdbcUrl());
    hikari.setUsername(config.username());
    hikari.setPassword(config.password());
    hikari.setMaximumPoolSize(config.maxPoolSize());
    hikari.setMinimumIdle(config.minIdle());
    hikari.setAutoCommit(false);
    // Do not fail pool creation when the DB is briefly unreachable; keep retrying in background.
    hikari.setInitializationFailTimeout(-1);
    return new HikariDataSource(hikari);
  }

  @Override
  public synchronized void close() {
    if (dataSource != null) {
      dataSource.close();
      dataSource = null;
    }
  }
}
