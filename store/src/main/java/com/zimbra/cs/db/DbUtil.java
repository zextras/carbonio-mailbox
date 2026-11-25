// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

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

public final class DbUtil {

    public static final int IN_CLAUSE_BATCH_SIZE = 400;

    private DbUtil() {
    }

    public static String limit(int limit) {
        return "LIMIT 0," + limit;
    }

    public static void enableStreaming(Statement stmt) throws SQLException {
        stmt.setFetchSize(Integer.MIN_VALUE);
    }

    public static String upsert(String table, List<String> columns, List<String> keyColumns, List<String> updateColumns) {
        String update = updateColumns.isEmpty()
            ? keyColumns.get(0) + " = " + keyColumns.get(0)
            : updateColumns.stream().map(c -> c + " = VALUES(" + c + ")").collect(Collectors.joining(", "));
        return "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES ("
            + String.join(", ", Collections.nCopies(columns.size(), "?")) + ") ON DUPLICATE KEY UPDATE " + update;
    }

    static String updateInnerJoin(String target, String joined, String on, String set, String where) {
        return "UPDATE " + target + " INNER JOIN " + joined + " ON " + on + " SET " + set + " WHERE " + where;
    }

    static String decrementConversationSizes(String table, String sourceWhere, String targetWhere, byte conversationType) {
        return "UPDATE " + table + ", (SELECT parent_id pid, COUNT(*) count FROM " + table
            + " WHERE " + sourceWhere + " AND parent_id IS NOT NULL GROUP BY parent_id) AS x"
            + " SET size = size - count, metadata = NULL, mod_metadata = ?, change_date = ?"
            + " WHERE " + targetWhere + "id = pid AND type = " + conversationType;
    }

    static boolean databaseExists(DbConnection conn, String dbname) throws ServiceException {
        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE schema_name = ?")) {
            stmt.setString(1, dbname);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to determine whether database exists", e);
        }
    }

    public static synchronized void flushToDisk() {
        DbConnection conn = null;
        PreparedStatement createStmt = null;
        PreparedStatement dropStmt = null;
        boolean success = false;
        try {
            try {
                conn = DbPool.getMaintenanceConnection();
                createStmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS zimbra.flush_enforcer (dummy_column INTEGER) ENGINE = InnoDB");
                dropStmt = conn.prepareStatement("DROP TABLE IF EXISTS zimbra.flush_enforcer");
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
            ZimbraLog.dbconn.warn("ignoring error while forcing mysql to flush innodb log to disk", e);
        } finally {
            if (!success) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {}
            }
        }
    }

    public static String suitableNumberOfVariables(int size) {
        StringBuilder sb = new StringBuilder(" (");
        for (int i = 0; i < size; i++)
            sb.append(i == 0 ? "?" : ", ?");
        return sb.append(")").toString();
    }

    public static String whereIn(String column, boolean in, int size) {
        return column + (in ? " IN" : " NOT IN") + suitableNumberOfVariables(size);
    }

    public static String whereIn(String column, int size) {
        return whereIn(column, true, size);
    }

    public static String whereNotIn(String column, int size) {
        return whereIn(column, false, size);
    }
}
