// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.zimbra.common.util.ZimbraLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.dbcp.DelegatingConnection;

class DebugConnection extends DelegatingConnection {
  protected final Connection mConn;

  DebugConnection(Connection conn) {
    super(conn);
    mConn = conn;
  }

  Connection getConnection() {
    return mConn;
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return new DebugPreparedStatement(this, mConn.prepareStatement(sql), sql);
  }

  public void commit() throws SQLException {
    ZimbraLog.sqltrace.debug("commit, conn=" + mConn.hashCode());
    mConn.commit();
  }

  public void rollback() throws SQLException {
    ZimbraLog.sqltrace.debug("rollback, conn=" + mConn.hashCode());
    mConn.rollback();
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return new DebugPreparedStatement(
        this, mConn.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
  }

  public PreparedStatement prepareStatement(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return new DebugPreparedStatement(
        this,
        mConn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability),
        sql);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return new DebugPreparedStatement(this, mConn.prepareStatement(sql, autoGeneratedKeys), sql);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return new DebugPreparedStatement(this, mConn.prepareStatement(sql, columnIndexes), sql);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return new DebugPreparedStatement(this, mConn.prepareStatement(sql, columnNames), sql);
  }
}
