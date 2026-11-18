// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.google.common.annotations.VisibleForTesting;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;

import com.zimbra.cs.db.DbPool.PoolConfig;

/**
 * Default ConnectionFactory implementation
 *
 */
public class ZimbraConnectionFactory extends DriverManagerConnectionFactory {

    private static ConnectionFactory sConnFactory = null;

    @VisibleForTesting
    static void close() {
        sConnFactory = null;
    }
    public static ConnectionFactory getConnectionFactory(PoolConfig pconfig) {
        if (sConnFactory == null) {
            sConnFactory = new ZimbraConnectionFactory(pconfig.mConnectionUrl, pconfig.mDatabaseProperties);
        }
        return sConnFactory;
    }

    ZimbraConnectionFactory(String connectUri, Properties props) {
        super(connectUri, props);
    }

    /**
     * Wraps the JDBC connection from the pool with a <tt>DebugConnection</tt>,
     * which does  <tt>sqltrace</tt> logging.
     */
    @Override
    public Connection createConnection() throws SQLException {
        Connection conn = super.createConnection();
        return new DebugConnection(conn);
    }
}
