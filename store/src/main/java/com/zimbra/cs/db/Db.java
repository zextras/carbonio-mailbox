// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @since Apr 10, 2004
 * @author schemers
 */
public abstract class Db {

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
        if (db == null) {
            throw new IllegalStateException("no database configured: create the pool with DbPool.global(Db)");
        }
        return db;
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

    /** Returns the set of configuration settings necessary to initialize the
     *  appropriate database connection pool.
     * @see DbPool#getPool() */
    abstract DbPool.PoolConfig getPoolConfig();


    /** Returns <tt>true</tt> if the database with the given name exists. */
    public abstract boolean databaseExists(DbConnection conn, String dbname)
    throws ServiceException;

    /** Generates the correct SQL to direct the current database engine to use
     *  a particular index to perform a SELECT query.  This string should come
     *  after the FROM clause and before the WHERE clause in the final SQL
     *  query.  If the database does not support this type of hinting, the
     *  function will return <tt>""</tt>. */
    public static String forceIndex(String index) {
        if (index == null || index.trim().equals(""))
            return "";
        return getInstance().forceIndexClause(index);
    }

    abstract String forceIndexClause(String index);

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

    /**
     * Generates a bitwise AND on two values.
     */
    public abstract String bitAND(String expr1, String expr2);

    public abstract String bitOR(String expr1, String expr2);

    abstract String updateInnerJoin(String target, String joined, String on, String set, String where);

    abstract String decrementConversationSizes(String table, String sourceWhere, String targetWhere, byte conversationType);

    abstract String foreignKeyChecks(boolean enabled);

    public abstract String upsert(String table, List<String> columns, List<String> keyColumns, List<String> updateColumns);

    /**
     * Generates a bitwise first value AND NOT second values.
     */
    public abstract String bitANDNOT(String expr1, String expr2);

    @SuppressWarnings("unused")
    public void enableStreaming(Statement stmt) throws SQLException {}

    /** Generates a WHERE-type clause that evaluates to {@code expr1} if
     *  its value is non-<tt>NULL</tt> and {@code expr2} otherwise. */
    public static String clauseIFNULL(String expr1, String expr2) {
        return getInstance().getIFNULLClause(expr1, expr2);
    }

    abstract String getIFNULLClause(String expr1, String expr2);

    /** Force the database engine to flush committed changes to physical disk. */
    public abstract void flushToDisk();


    /**
     * @return limit for param checking
     */
    public int getParamLimit() {
        return Integer.MAX_VALUE;
    }

    /**
     * Concatenates two or more fields.
     */
    public abstract String concat(String... fieldsToConcat);

    /**
     * Generates the sign value of the field.
     */
    public abstract String sign(String field);

    /**
     * Pads to the left end of the field.
     */
    public abstract String lpad(String field, int padSize, String padString);

    /** Returns a {@code LIMIT} clause that is appended to a {@code SELECT}
     *  statement to limit the number of rows in the result set.  If the
     *  database does not support this feature, returns an empty string.
     *
     * @param limit number of rows to return */
    public String limit(int limit) {
        return limit(0, limit);
    }

    /**
     * Returns a {@code LIMIT} clause that is appended to a {@code SELECT} statement
     * to limit the number of rows in the result set.  If the database does not support
     * this feature, returns an empty string.
     *
     * @param offset number of rows at the beginning of the result set that will be skipped
     * @param limit number of rows to return
     * @return
     */
    public String limit(int offset, int limit) {
        return "";
    }

    protected String escapeSequence() {
        return "\\";
    }
}
