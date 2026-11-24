// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.db;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class DbTestUtil {

  private DbTestUtil() {}

  public static DbResults executeQuery(DbConnection conn, String query, Object... params)
      throws ServiceException {
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      bind(stmt, params);
      return new DbResults(stmt.executeQuery());
    } catch (SQLException e) {
      throw ServiceException.FAILURE("SQL: '" + query + "'", e);
    }
  }

  public static DbResults executeQuery(String query, Object... params) throws ServiceException {
    DbConnection conn = DbPool.getConnection();
    try {
      return executeQuery(conn, query, params);
    } finally {
      conn.closeQuietly();
    }
  }

  public static int executeUpdate(DbConnection conn, String sql, Object... params)
      throws ServiceException {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      bind(stmt, params);
      return stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE("SQL: '" + sql + "'", e);
    }
  }

  public static int executeUpdate(String sql, Object... params) throws ServiceException {
    DbConnection conn = DbPool.getConnection();
    try {
      int numRows = executeUpdate(conn, sql, params);
      conn.commit();
      return numRows;
    } finally {
      DbPool.quietClose(conn);
    }
  }

  private static void bind(PreparedStatement stmt, Object... params) throws SQLException {
    if (params == null) {
      return;
    }
    for (int i = 0; i < params.length; i++) {
      stmt.setObject(i + 1, params[i]);
    }
  }
}
