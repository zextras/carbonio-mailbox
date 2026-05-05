// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.google.common.annotations.VisibleForTesting;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool.PoolConfig;

/**
 * Default ConnectionFactory implementation.
 *
 * <p>Note: this class is retained as an extension point referenced by
 * {@code LC.zimbra_class_dbconnfactory}. The primary connection pool (HikariCP)
 * no longer uses this factory directly; it manages connections internally.
 */
public class ZimbraConnectionFactory {

    private static ZimbraConnectionFactory sConnFactory = null;

    private final String connectUri;
    private final Properties props;

    ZimbraConnectionFactory(String connectUri, Properties props) {
        this.connectUri = connectUri;
        this.props = props;
    }

    /**
     * Creates a raw JDBC connection (used by extension implementations).
     */
    public Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(connectUri, props);
        return new DebugConnection(conn);
    }

    @VisibleForTesting
    static void close() {
        sConnFactory = null;
    }

    public static ZimbraConnectionFactory getConnectionFactory(PoolConfig pconfig) {
        if (sConnFactory == null) {
            String className = LC.zimbra_class_dbconnfactory.value();
            if (className != null && !className.isEmpty()) {
                try {
                    ZimbraLog.dbconn.debug("instantiating DB connection factory class " + className);
                    Class<?> clazz = Class.forName(className);
                    var constructor = clazz.getDeclaredConstructor(String.class, Properties.class);
                    sConnFactory = (ZimbraConnectionFactory) constructor.newInstance(
                            pconfig.mConnectionUrl, pconfig.mDatabaseProperties);
                } catch (Exception e) {
                    ZimbraLog.system.error(
                            "could not instantiate database connection pool '" + className
                                    + "'; defaulting to ZimbraConnectionFactory", e);
                }
            }
            if (sConnFactory == null)
                sConnFactory = new ZimbraConnectionFactory(pconfig.mConnectionUrl, pconfig.mDatabaseProperties);
        }
        return sConnFactory;
    }
}
