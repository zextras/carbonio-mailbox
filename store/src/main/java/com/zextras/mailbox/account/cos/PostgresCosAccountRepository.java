// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.account.cos;

import com.zextras.mailbox.db.PostgresConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Postgres-backed {@link CosAccountRepository}. */
public class PostgresCosAccountRepository implements CosAccountRepository {

  private final PostgresConnectionPool pool;

  public PostgresCosAccountRepository(PostgresConnectionPool pool) {
    this.pool = pool;
  }

  @Override
  public void assign(String accountId, String cosId) {
    final String sql =
        "INSERT INTO account_cos (account_id, cos_id) VALUES (?, ?) "
            + "ON CONFLICT (account_id) DO UPDATE SET cos_id = EXCLUDED.cos_id";
    try (Connection c = pool.dataSource().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, accountId);
      ps.setString(2, cosId);
      ps.executeUpdate();
      c.commit();
    } catch (SQLException e) {
      throw new CosCountPersistenceException("assign failed for account " + accountId, e);
    }
  }

  @Override
  public void remove(String accountId) {
    final String sql = "DELETE FROM account_cos WHERE account_id = ?";
    try (Connection c = pool.dataSource().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, accountId);
      ps.executeUpdate();
      c.commit();
    } catch (SQLException e) {
      throw new CosCountPersistenceException("remove failed for account " + accountId, e);
    }
  }

  @Override
  public long countByCos(String cosId) {
    final String sql = "SELECT COUNT(*) FROM account_cos WHERE cos_id = ?";
    try (Connection c = pool.dataSource().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, cosId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getLong(1) : 0L;
      }
    } catch (SQLException e) {
      throw new CosCountPersistenceException("countByCos failed for cos " + cosId, e);
    }
  }

  @Override
  public Map<String, Long> countAllByCos() {
    final String sql = "SELECT cos_id, COUNT(*) FROM account_cos GROUP BY cos_id";
    Map<String, Long> result = new HashMap<>();
    try (Connection c = pool.dataSource().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        result.put(rs.getString(1), rs.getLong(2));
      }
      return result;
    } catch (SQLException e) {
      throw new CosCountPersistenceException("countAllByCos failed", e);
    }
  }

  @Override
  public void replaceAll(Map<String, String> accountIdToCosId) {
    try (Connection c = pool.dataSource().getConnection()) {
      try (PreparedStatement truncate = c.prepareStatement("TRUNCATE TABLE account_cos")) {
        truncate.executeUpdate();
      }
      final String insert = "INSERT INTO account_cos (account_id, cos_id) VALUES (?, ?)";
      try (PreparedStatement ps = c.prepareStatement(insert)) {
        for (Map.Entry<String, String> e : accountIdToCosId.entrySet()) {
          ps.setString(1, e.getKey());
          ps.setString(2, e.getValue());
          ps.addBatch();
        }
        ps.executeBatch();
      }
      c.commit();
    } catch (SQLException e) {
      throw new CosCountPersistenceException("replaceAll failed", e);
    }
  }
}
