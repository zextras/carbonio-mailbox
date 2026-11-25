// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since Apr 10, 2004
 * @author schemers
 */
public abstract class Db {

	private final String sTableName = "zimbra.flush_enforcer";
	private final String sCreateTable =
			"CREATE TABLE IF NOT EXISTS " + sTableName + " (dummy_column INTEGER) ENGINE = InnoDB";
	private final String sDropTable = "DROP TABLE IF EXISTS " + sTableName;

	public String limit(int offset, int limit) {
			return "LIMIT " + offset + "," + limit;
	}

	public enum Error {
        DEADLOCK_DETECTED,
        DUPLICATE_ROW,
        FOREIGN_KEY_CHILD_EXISTS,
        FOREIGN_KEY_NO_PARENT,
        NO_SUCH_DATABASE,
        NO_SUCH_TABLE,
        TOO_MANY_SQL_PARAMS,
        BUSY,
        LOCKED,
        CANTOPEN,
        TABLE_FULL
    }


    private static volatile Db sDatabase;

    public static Db getInstance() {
        Db db = sDatabase;
        if (db != null) {
            return db;
        }
        synchronized (Db.class) {
            if (sDatabase == null) {
                sDatabase = new MariaDB();
            }
            return sDatabase;
        }
    }

    @VisibleForTesting
    public static synchronized void setInstance(Db database) {
        sDatabase = database;
    }

    public String healthCheckQuery() {
        return "SELECT 1";
    }
    /** Returns whether the given {@link SQLException} is an instance of the
     *  specified {@link Db.Error}. */
    public static boolean errorMatches(SQLException e, Db.Error error) {
        return getInstance().compareError(e, error);
    }

    abstract boolean compareError(SQLException e, Db.Error error);


	public boolean databaseExists(DbConnection conn, String dbname) throws ServiceException {
			PreparedStatement stmt = null;
			ResultSet rs = null;
			int numSchemas = 0;

			try {
					stmt = conn.prepareStatement(
							"SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA " +
							"WHERE schema_name = ?");
					stmt.setString(1, dbname);
					rs = stmt.executeQuery();
					rs.next();
					numSchemas = rs.getInt(1);
			} catch (SQLException e) {
					throw ServiceException.FAILURE("Unable to determine whether database exists", e);
			} finally {
					DbPool.closeResults(rs);
					DbPool.closeStatement(stmt);
			}

			return (numSchemas > 0);
	}

	String forceIndexClause(String index) {
			return " FORCE INDEX (" + index + ')';
	}

    /** Returns the string used to delimit commands in multi-line scripts.
     *  This is usually '<tt>;</tt>' (in keeping with SQL conventions), but
     *  it may be an alternate character in order to permit '<tt>;</tt>'
     *  within a script. */
    public String scriptCommandDelimiter() {
        return ";";
    }

    private static final int DEFAULT_IN_CLAUSE_BATCH_SIZE = 400;

    protected int getInClauseBatchSize() { return DEFAULT_IN_CLAUSE_BATCH_SIZE; }

    /** Returns the maximum number of items to include in an "IN (?, ?, ...)"
     *  clause.  For databases with a broken or hugely nonperformant IN clause,
     *  e.g. Derby pre-10.3 (see DERBY-47 JIRA), this may be 1 */
    public static int getINClauseBatchSize() {
        return getInstance().getInClauseBatchSize();
    }

    /** Generates a WHERE-type clause that evaluates to true when the given
     *  column equals a string later specified by <tt>stmt.setString()</tt>
     *  under a case-insensitive comparison.  Note that the caller *MUST NOT*
     *  pass an upcased version of the comparison string in the subsequent
     *  call to <tt>stmt.setString()</tt>. */
    static String equalsSTRING(String column) {
        return column + " = ?";
    }

	public String bitAND(String expr1, String expr2) {
			return expr1 + " & " + expr2;
	}

	public String bitOR(String expr1, String expr2) {
			return expr1 + " | " + expr2;
	}

	String updateInnerJoin(String target, String joined, String on, String set, String where) {
			return "UPDATE " + target + " INNER JOIN " + joined + " ON " + on + " SET " + set + " WHERE " + where;
	}

	String decrementConversationSizes(String table, String sourceWhere, String targetWhere, byte conversationType) {
			return "UPDATE " + table + ", (SELECT parent_id pid, COUNT(*) count FROM " + table
					+ " WHERE " + sourceWhere + " AND parent_id IS NOT NULL GROUP BY parent_id) AS x"
					+ " SET size = size - count, metadata = NULL, mod_metadata = ?, change_date = ?"
					+ " WHERE " + targetWhere + "id = pid AND type = " + conversationType;
	}

	String foreignKeyChecks(boolean enabled) {
			return "SET FOREIGN_KEY_CHECKS=" + (enabled ? 1 : 0);
	}

	public void enableStreaming(Statement stmt)
	throws SQLException {
			stmt.setFetchSize(Integer.MIN_VALUE);
	}

	public String upsert(String table, List<String> columns, List<String> keyColumns, List<String> updateColumns) {
			String update = updateColumns.isEmpty()
					? keyColumns.get(0) + " = " + keyColumns.get(0)
					: updateColumns.stream().map(c -> c + " = VALUES(" + c + ")").collect(Collectors.joining(", "));
			return "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES ("
					+ String.join(", ", Collections.nCopies(columns.size(), "?")) + ") ON DUPLICATE KEY UPDATE " + update;
	}

	public String bitANDNOT(String expr1, String expr2) {
			return expr1 + " & ~" + expr2;
	}

    /** Generates a WHERE-type clause that evaluates to {@code expr1} if
     *  its value is non-<tt>NULL</tt> and {@code expr2} otherwise. */
    public static String clauseIFNULL(String expr1, String expr2) {
        return getInstance().getIFNULLClause(expr1, expr2);
    }

	String getIFNULLClause(String expr1, String expr2) {
			return "IFNULL(" + expr1 + ", " + expr2 + ")";
	}

	@Override
	public String toString() {
			return "MySQL";
	}

	public synchronized void flushToDisk() {
			// Create a table and then drop it.  We take advantage of the fact that innodb will call
			// log_buffer_flush_to_disk() during CREATE TABLE or DELETE TABLE.
			DbConnection conn = null;
			PreparedStatement createStmt = null;
			PreparedStatement dropStmt = null;
			boolean success = false;
			try {
					try {
							conn = DbPool.getMaintenanceConnection();
							createStmt = conn.prepareStatement(sCreateTable);
							dropStmt = conn.prepareStatement(sDropTable);
							createStmt.executeUpdate();
							dropStmt.executeUpdate();
							success = true;
					} finally {
							DbPool.quietCloseStatement(createStmt);
							DbPool.quietCloseStatement(dropStmt);
							if (conn != null)
									conn.commit();
							DbPool.quietClose(conn);
					}
			} catch (SQLException | ServiceException e) {
					// If there's an error, let's just log it but not bubble up the exception.
					ZimbraLog.dbconn.warn("ignoring error while forcing mysql to flush innodb log to disk", e);
			} finally {
					if (!success) {
							// There was an error.
							// The whole point of this method is to force innodb to flush its log.  Innodb is
							// supposed to be flushing roughly every second in its master thread, so let's simply
							// wait a few seconds to give the master thread a chance.
							try {
									Thread.sleep(3000);
							} catch (InterruptedException e1) {}
					}
			}
	}


    /**
     * @return limit for param checking
     */
    public int getParamLimit() {
        return Integer.MAX_VALUE;
    }

	public String concat(String... fieldsToConcat) {
			Joiner joiner = Joiner.on(", ").skipNulls();
			return "CONCAT(" + joiner.join(fieldsToConcat) + ")";
	}

	public String sign(String field) {
			return "SIGN(" + field + ")";
	}

	public String lpad(String field, int padSize, String padString) {
			return "LPAD(" + field + ", " + padSize + ", '" + padString + "')";
	}

    /** Returns a {@code LIMIT} clause that is appended to a {@code SELECT}
     *  statement to limit the number of rows in the result set.  If the
     *  database does not support this feature, returns an empty string.
     *
     * @param limit number of rows to return */
    public String limit(int limit) {
        return limit(0, limit);
    }

    protected String escapeSequence() {
        return "\\";
    }
}
