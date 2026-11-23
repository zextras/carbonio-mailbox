// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool.DbConnection;

/**
 * <code>DbUtil</code> contains some database utility methods and
 * automates the task of running SQL statements.  It
 * wraps the JDBC <code>Connection</code>and <code>PreparedStatement</code>
 * classes and frees the caller from having to handle <code>SQLException</code>
 * and allocate and deallocate database resources.<p>
 *
 * Query results are read entirely into memory and returned in the form of a
 * {@link com.zimbra.cs.db.DbResults} object.  This improves concurrency,
 * but potentially increases memory consumption.  Code that deals with
 * large result sets should use the JDBC API's directly.
 *
 * @author bburtin
 */
public final class DbUtil {

    private DbUtil() {
    }

    /**
     * Converts a <tt>java.util.Date</tt> to a <tt>java.sql.Timestamp</tt>.
     * If the date is <tt>null</tt>, returns <tt>null</tt>.
     */
    public static Timestamp dateToTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }

    /**
     * Converts a  <tt>java.sql.Timestamp</tt> to a <tt>java.util.Date</tt>.
     * If the timestamp is <tt>null</tt>, returns <tt>null</tt>.
     */
    public static Date timestampToDate(Timestamp timestamp) {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    /**
     * Executes the specified query using the specified connection.
     *
     * @throws ServiceException if the query cannot be executed or an error occurs while retrieving results
     */
    public static DbResults executeQuery(DbConnection conn, String query, Object... params) throws ServiceException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);

            if (params != null) {
                for (int i = 0; i < params.length; i++)
                    stmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();
            return new DbResults(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("SQL: '" + query + "'", e);
        } finally {
            conn.closeQuietly(stmt);
        }
    }

    /**
     * Executes the specified query using the specified connection.
     *
     * @throws ServiceException if the query cannot be executed or an error occurs while retrieving results
     */
    public static DbResults executeQuery(DbConnection conn, String query) throws ServiceException {
        return executeQuery(conn, query);
    }

    /**
     * Executes the specified query using a connection from the connection pool.
     *
     * @throws ServiceException if the query cannot be executed or an error occurs while retrieving results
     */
    public static DbResults executeQuery(String query, Object... params) throws ServiceException {
        DbConnection conn = DbPool.getConnection();
        try {
            return executeQuery(conn, query, params);
        } finally {
            conn.closeQuietly();
        }
    }

    /**
     * Executes the specified update using the specified connection.
     *
     * @return the number of rows affected
     * @throws ServiceException if the update cannot be executed
     */
    public static int executeUpdate(DbConnection conn, String sql, Object... params) throws ServiceException {
        PreparedStatement stmt = null;
        int numRows = 0;

        try {
            stmt = conn.prepareStatement(sql);

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            numRows = stmt.executeUpdate();
        } catch (SQLException e) {
            String message = "SQL: '" + sql + "'";
            throw ServiceException.FAILURE(message, e);
        } finally {
            DbPool.closeStatement(stmt);
        }

        return numRows;
    }

    /**
     * Executes the specified update using the specified connection.
     *
     * @return the number of rows affected
     * @throws ServiceException if the update cannot be executed
     */
    public static int executeUpdate(DbConnection conn, String sql) throws ServiceException {
        return executeUpdate(conn, sql, (Object[]) null);
    }

    /**
     * Executes the specified update using a connection from the connection pool.
     *
     * @return the number of rows affected
     * @throws ServiceException if the update cannot be executed
     */
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

    /**
     * Returns a string with the form <code>(?, ?, ?, ...)</code>.
     * Used for generating SQL IN clauses.
     * @param size the number of question marks
     */
    public static String suitableNumberOfVariables(int size) {
        StringBuilder sb = new StringBuilder(" (");
        for (int i = 0; i < size; i++)
            sb.append(i == 0 ? "?" : ", ?");
        return sb.append(")").toString();
    }

    /**
     * Executes all SQL statements in the specified SQL script.  Statements are
     * separated by semicolons.
     *
     * @param conn the database connection to use for executing the SQL statements
     * @param scriptReader the source of the SQL script file. The reader is closed
     * when this method returns.
     */
    public static void executeScript(DbPool.DbConnection conn, Reader scriptReader)
    throws IOException, ServiceException, SQLException {
        PreparedStatement stmt = null;
        String[] statements = parseScript(scriptReader);

        try {
            for (String sql : statements) {
                if (sql.length() == 0)
                    continue;
                stmt = conn.prepareStatement(sql);
                stmt.execute();
                stmt.close();
            }
            conn.commit();
        } catch (SQLException e) {
            DbPool.quietRollback(conn);
            throw e;
        }
    }

    private static String[] addScript(String[] s1, String[] s2) {
        if (s1 == null)
            return s2;

        String[] temp = new String[s1.length + s2.length];

        System.arraycopy(s1, 0, temp, 0, s1.length);
        System.arraycopy(s2, 0, temp, s1.length, s2.length);
        return temp;
    }

    /**
     * Parses a SQL script into separate SQL statements, separated by semicolons.
     * Removes comments that begin with <code>--</code> or <code>#</code>.
     */
    public static String[] parseScript(Reader scriptReader) throws IOException {
        return parseScript(scriptReader, Db.getInstance());
    }

    public static String[] parseScript(Reader scriptReader, Db db) throws IOException {
        StringBuilder buf = new StringBuilder();
        BufferedReader br = new BufferedReader(scriptReader);
        String line;
        String[] ret = null;

        while ((line = br.readLine()) != null) {
            line = removeComments(line);
            if (line.startsWith(".")) {
                // SQLite does not suport read statements so emulate them
                if (line.endsWith(";"))
                    line = line.substring(0, line.length() - 1);
                if (line.startsWith(".read ")) {
                    String sql;

                    line = line.substring(".read".length());
                    line = line.trim();
                    line = line.replace("\"", "");
                    sql = new String(Files.readAllBytes(new File(line).toPath()));
                    ret = addScript(ret, parseScript(new StringReader(sql)));
                }
                continue;
            }
            buf.append(line);
            line = line.trim();
            if (line.length() == 0) {
                // ignore comments or blank lines
                continue;
            }
            buf.append('\n');
        }
        br.close();
        if (buf.length() != 0 || ret == null)
            ret = addScript(ret, buf.toString().split("\\s*" +
                db.scriptCommandDelimiter() + "\\s*"));
        for (int i = 0; i < ret.length; i++) {
            if (ret[i].startsWith("END")) {
                ret[i - 1] += ";\n" + ret[i];
                ret[i] = "";
            }
        }
        return ret;
    }

    /**
     * Removes trailing comments.  Comments begin with <code>--</code> or <code>#</code>.
     * @return <code>sqlLine</code> without trailing comments
     */
    public static String removeComments(String sqlLine) {
        // remove comments that start with "#" or "--"
        int commentIndex = sqlLine.indexOf("--");
        int hashIndex = sqlLine.indexOf('#');
        if (hashIndex >= 0) {
            if (commentIndex >= 0) {
                commentIndex = Math.min(commentIndex, hashIndex);
            } else {
                commentIndex = hashIndex;
            }
        }
        if (commentIndex != -1) {
            sqlLine = sqlLine.substring(0, commentIndex);
        }
        return sqlLine;
    }

    /**
     * Returns a string that optimizes SQL WHERE IN clauses to an operator when
     * the parm list is small. A single IN can prevent some DBs like sqlite from
     * choosing proper indexes and a small # uses more CPU resources
     */
    public static String whereIn(String column, boolean in, int size) {
        if (size == 1) {
            return column + (in ? " = ?" : " <> ?");
        } else if (size <= 3) {
            StringBuffer sb = new StringBuffer("(");

            do {
                sb.append(column).append((in ? " = ?" : " <> ?"));
                if (size > 1) {
                    if (in)
                        // col IN(A,B,C) --> (col=A OR col=B OR col=C)
                        sb.append(" OR ");
                    else
                        // col NOT IN(A,B,C) --> (col<>A AND col<>B AND col<>C)
                        sb.append(" AND ");
                }
            } while (--size > 0);
            sb.append(')');
            return sb.toString();
        }
        return column + (in ? " IN" : " NOT IN") + suitableNumberOfVariables(size);
    }

    public static String whereIn(String column, int size) {
        return whereIn(column, true, size);
    }

    public static String whereNotIn(String column, int size) {
        return whereIn(column, false, size);
    }
}
