// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.SQLException;
import java.sql.Statement;

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
        TABLE_FULL;
    }

    public enum Capability {
        BITWISE_OPERATIONS,
        BOOLEAN_DATATYPE,
        CASE_SENSITIVE_COMPARISON,
        CAST_AS_BIGINT,
        CLOB_COMPARISON,
        DISABLE_CONSTRAINT_CHECK,
        FILE_PER_DATABASE,
        LIMIT_CLAUSE,
        MULTITABLE_UPDATE,
        NON_BMP_CHARACTERS,
        ON_DUPLICATE_KEY,
        ON_UPDATE_CASCADE,
        READ_COMMITTED_ISOLATION,
        REPLACE_INTO,
        ROW_LEVEL_LOCKING,
        UNIQUE_NAME_INDEX,
        AVOID_OR_IN_WHERE_CLAUSE, // if set, then try to avoid ORs in WHERE clauses, run them as separate queries and mergesort in memory
        REQUEST_UTF8_UNICODE_COLLATION, // for mysql
        FORCE_INDEX_EVEN_IF_NO_SORT, // for derby
        SQL_PARAM_LIMIT,
        DUMPSTER_TABLES;
    }

    private static Db sDatabase;

    private static String ESCAPE_SEQUENCE = "\\";

    public static synchronized Db getInstance() {
        if (sDatabase == null) {
            String className = LC.zimbra_class_database.value();
            if (className != null && !className.equals("")) {
                try {
                    sDatabase = (Db) Class.forName(className).newInstance();
                } catch (Exception e) {
                    ZimbraLog.system.error("could not instantiate database configuration '" + className + "'; defaulting to MySQL", e);
                }
            }
            if (sDatabase == null)
                sDatabase = new MariaDB();
            ESCAPE_SEQUENCE = sDatabase.escapeSequence();
        }
        return sDatabase;
    }

    /** Returns whether the currently-configured database supports the given
     *  {@link Db.Capability}. */
    public static boolean supports(Db.Capability capability) {
        return getInstance().supportsCapability(capability);
    }

    abstract boolean supportsCapability(Db.Capability capability);

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

    /** optimize and optionally compact a database
     * level 0: analysis tuning only
     * level 1: quick file optimization and analysis
     * level 2: full file optimization and analysis
     */
    @SuppressWarnings("unused")
    public void optimize(DbConnection conn, String name, int level) throws ServiceException {}


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
        if (supports(Capability.CASE_SENSITIVE_COMPARISON)) {
            return "UPPER(" + column + ") = UPPER(?)";
        } else {
            return column + " = ?";
        }
    }

    /**
     * Generates a bitwise AND on two values.
     */
    public abstract String bitAND(String expr1, String expr2);

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
     * Give DB implementations a chance to preemptively check if param limit will be exceeded
     * If exceeded, a ServiceException which wraps a SQLException corresponding to Error.TOO_MANY_SQL_PARAMS must be thrown
     * @param numParams
     */
    public void checkParamLimit(int numParams) throws ServiceException {
    }

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

    public static String getEscapeSequence() {
        return ESCAPE_SEQUENCE;
    }
}
