// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.DbPool.PoolConfig;
import com.zimbra.cs.mailbox.Mailbox;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.hsqldb.cmdline.SqlFile;

/**
 * HSQLDB is for unit test. All data is in memory, not persistent across JVM
 * restarts.
 *
 * @author ysasaki
 */
public class HSQLDB extends Db {

    /**
     * Populates ZIMBRA and MBOXGROUP1 schema.
     */
    public static void createDatabase() throws Exception {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DbConnection conn = DbPool.getConnection();
        try {
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?");
            stmt.setString(1, "ZIMBRA");
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already exists
            }
            createZimbraDatabase(conn);
            createMailboxDatabase(conn);
        } finally {
            DbPool.closeResults(rs);
            DbPool.quietCloseStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    /**
     * Deletes all records from all tables.
     */
    public static void clearDatabase() throws Exception {
        DbConnection conn = DbPool.getConnection();
        try {
            clearDatabase(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    private static void createZimbraDatabase(DbConnection conn) throws Exception {
        Map<String, String> vars = Map.of(
                "DATABASE_NAME", DbMailbox.getDatabaseName(1),
                "VOLUME_BASE_DIRECTORY", new File("build/test/").getAbsolutePath());
        execute(conn, "db.sql", vars);
    }

    private static void createMailboxDatabase(DbConnection conn) throws Exception {
        Map<String, String> vars = Collections.singletonMap("DATABASE_NAME", DbMailbox.getDatabaseName(1));
        execute(conn, "create_database.sql", vars);
    }

    private static void clearDatabase(DbConnection conn) throws Exception {
        Map<String, String> vars = Collections.singletonMap("DATABASE_NAME", DbMailbox.getDatabaseName(1));
        execute(conn, "clear.sql", vars);
    }

    private static void execute(DbConnection conn, String resource, Map<String, String> vars) throws Exception {
        final InputStream resourceAsStream = HSQLDB.class.getResourceAsStream(resource);
        URL url = null;
        SqlFile sql = new SqlFile(new InputStreamReader(resourceAsStream,
                StandardCharsets.UTF_8),
                "", System.out, StandardCharsets.UTF_8.toString(), false, url);
        sql.addUserVars(vars);
        sql.setConnection(conn.getConnection());
        sql.execute();
        conn.commit();
    }

    @Override
    PoolConfig getPoolConfig() {
        return new Config();
    }

    @Override
    boolean supportsCapability(Capability capability) {
        switch (capability) {
            case MULTITABLE_UPDATE:
            case BITWISE_OPERATIONS:
            case REPLACE_INTO:
            case DISABLE_CONSTRAINT_CHECK:
                return false;
            default:
                return true;
        }
    }

    @Override
    boolean compareError(SQLException e, Error error) {
        switch (error) {
            case DUPLICATE_ROW:
                return e.getErrorCode() == -104;
            default:
                return false;
        }
    }

    /**
     * Always returns true. All schema is pre-populated.
     */
    @Override
    public boolean databaseExists(DbConnection connection, String dbname) {
        return true;
    }

    /**
     * TODO
     */
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
        return "BITAND(" + expr1 + ", " + expr2 + ")";
    }

    @Override
    public String bitANDNOT(String expr1, String expr2) {
        return "BITANDNOT(" + expr1 + ", " + expr2 + ")";
    }

    @Override
    public void flushToDisk() {
    }

    private static final class Config extends PoolConfig {
        Config() {
            mDriverClassName = "org.hsqldb.jdbcDriver";
            mPoolSize = 10;
            mRootUrl = "jdbc:hsqldb:mem:";
            mConnectionUrl = "jdbc:hsqldb:mem:zimbra";
            mSupportsStatsCallback = false;
            mDatabaseProperties = new Properties();
        }
    }

    @Override
    public String concat(String... fieldsToConcat) {
        Joiner joiner = Joiner.on(", ").skipNulls();
        return "CONCAT(" + joiner.join(fieldsToConcat) + ")";
    }

    @Override
    public String sign(String field) {
        return "SIGN(" + field + ")";
    }

    @Override
    public String lpad(String field, int padSize, String padString) {
        return "LPAD(" + field + ", " + padSize + ", '" + padString + "')";
    }

    @Override
    public String limit(int offset, int limit) {
        return "LIMIT " + limit + " OFFSET " + offset;
    }

    public void useMVCC(Mailbox mbox) throws ServiceException, SQLException {
        // tell HSQLDB to use multiversion so our asserts can read while write is open
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DbConnection conn = DbPool.getConnection(mbox);
        try {
            stmt = conn.prepareStatement("SET DATABASE TRANSACTION CONTROL MVCC");
            stmt.executeUpdate();
        } finally {
            DbPool.closeResults(rs);
            DbPool.quietCloseStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

}
