// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.google.common.base.Joiner;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * PostgreSQL backend.
 *
 * <p>Layout: one database; the {@code zimbra} database and every {@code mboxgroupN} database of
 * the MariaDB layout become schemas, so {@code mboxgroupN.table} references and cross-group
 * foreign keys keep working unchanged. Connections default to {@code search_path=zimbra} for the
 * unqualified {@code zimbra} tables.
 *
 * <p>MariaDB keeps a transaction usable after a failed statement; the mailbox code relies on that
 * (insert, catch duplicate, update). PostgreSQL aborts the whole transaction instead, so the
 * driver runs with {@code autosave=conservative}, which wraps each statement in a savepoint.
 */
public class PostgreSQL extends Db {

  /** PostgreSQL caps bind parameters per statement at 2^15 - 1. */
  private static final int MAX_BIND_PARAMETERS = 32767;

  private static final Map<Db.Error, String> SQL_STATES =
      Map.of(
          Db.Error.DEADLOCK_DETECTED, "40P01",
          Db.Error.DUPLICATE_ROW, "23505",
          Db.Error.FOREIGN_KEY_NO_PARENT, "23503",
          Db.Error.FOREIGN_KEY_CHILD_EXISTS, "23503",
          Db.Error.NO_SUCH_DATABASE, "3F000",
          Db.Error.NO_SUCH_TABLE, "42P01",
          Db.Error.TABLE_FULL, "53100");

  @Override
  boolean supportsCapability(Db.Capability capability) {
    switch (capability) {
      case BITWISE_OPERATIONS:
      case BOOLEAN_DATATYPE:
      case CASE_SENSITIVE_COMPARISON:
      case CAST_AS_BIGINT:
      case CLOB_COMPARISON:
      case DUMPSTER_TABLES:
      case LIMIT_CLAUSE:
      case NON_BMP_CHARACTERS:
      case ON_UPDATE_CASCADE:
      case READ_COMMITTED_ISOLATION:
      case ROW_LEVEL_LOCKING:
      case SQL_PARAM_LIMIT:
      case UNIQUE_NAME_INDEX:
        return true;
      case AVOID_OR_IN_WHERE_CLAUSE:
      case DISABLE_CONSTRAINT_CHECK:
      case FILE_PER_DATABASE:
      case FORCE_INDEX_EVEN_IF_NO_SORT:
      case MULTITABLE_UPDATE:
      case ON_DUPLICATE_KEY:
      case REPLACE_INTO:
      case REQUEST_UTF8_UNICODE_COLLATION:
        return false;
    }
    return false;
  }

  @Override
  boolean compareError(SQLException e, Db.Error error) {
    String expected = SQL_STATES.get(error);
    if (expected == null) {
      return false;
    }
    for (Throwable t = e; t != null; t = t.getCause()) {
      if (t instanceof SQLException sqle && expected.equals(sqle.getSQLState())) {
        return true;
      }
    }
    return false;
  }

  @Override
  DbPool.PoolConfig getPoolConfig() {
    return new PostgreSQLConfig();
  }

  @Override
  public boolean databaseExists(DbConnection conn, String dbname) throws ServiceException {
    try (PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?")) {
      stmt.setString(1, dbname.toLowerCase(Locale.ROOT));
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw ServiceException.FAILURE("Unable to determine whether schema exists", e);
    }
  }

  /** The planner takes no index hints. */
  @Override
  String forceIndexClause(String index) {
    return "";
  }

  @Override
  String getIFNULLClause(String expr1, String expr2) {
    return "COALESCE(" + expr1 + ", " + expr2 + ")";
  }

  @Override
  public String bitAND(String expr1, String expr2) {
    return "(" + expr1 + " & " + expr2 + ")";
  }

  @Override
  public String bitANDNOT(String expr1, String expr2) {
    return "(" + expr1 + " & ~" + expr2 + ")";
  }

  /** Commits are durable once acknowledged (synchronous_commit=on); nothing to force. */
  @Override
  public void flushToDisk() {}

  @Override
  public void enableStreaming(Statement stmt) throws SQLException {
    // pgjdbc streams with a positive fetch size on a non-autocommit connection.
    if (LC.jdbc_results_streaming_enabled.booleanValue()) {
      stmt.setFetchSize(1000);
    }
  }

  @Override
  public int getParamLimit() {
    return MAX_BIND_PARAMETERS;
  }

  @Override
  public String concat(String... fieldsToConcat) {
    // CONCAT is variadic "any": an untyped bind parameter has no type to resolve to.
    String[] fields = new String[fieldsToConcat.length];
    for (int i = 0; i < fields.length; i++) {
      fields[i] = "?".equals(fieldsToConcat[i]) ? "CAST(? AS TEXT)" : fieldsToConcat[i];
    }
    return "CONCAT(" + Joiner.on(", ").skipNulls().join(fields) + ")";
  }

  @Override
  public String sign(String field) {
    return "SIGN(" + field + ")";
  }

  @Override
  public String lpad(String field, int padSize, String padString) {
    return "LPAD(CAST(" + field + " AS TEXT), " + padSize + ", '" + padString + "')";
  }

  @Override
  public String limit(int offset, int limit) {
    return "LIMIT " + limit + " OFFSET " + offset;
  }

  /** PostgreSQL text cannot hold NUL. Migrated rows must have their TAG_NAMES rewritten. */
  @Override
  public String tagSeparator() {
    return "\u0001";
  }

  @Override
  public String toString() {
    return "PostgreSQL";
  }

  static final class PostgreSQLConfig extends DbPool.PoolConfig {
    PostgreSQLConfig() {
      mDriverClassName = "org.postgresql.Driver";
      mPoolSize = 100;
      mRootUrl =
          "jdbc:postgresql://"
              + hostForUrl(LC.postgres_host.value())
              + ":"
              + LC.postgres_port.value()
              + "/"
              + LC.postgres_database.value();
      mConnectionUrl = mRootUrl;
      mLoggerUrl = null;
      mSupportsStatsCallback = true;
      mDatabaseProperties = new Properties();
      mDatabaseProperties.put("user", LC.postgres_user.value());
      mDatabaseProperties.put("password", LC.postgres_password.value());
      mDatabaseProperties.put("currentSchema", "zimbra");
      mDatabaseProperties.put("autosave", "always");
      // MariaDB coerces string parameters to the column type; let the server infer it too.
      mDatabaseProperties.put("stringtype", "unspecified");
      mDatabaseProperties.put("ApplicationName", "carbonio-mailbox");
    }

    private static String hostForUrl(String host) {
      return host.indexOf(':') > -1 ? "[" + host + "]" : host;
    }
  }
}
