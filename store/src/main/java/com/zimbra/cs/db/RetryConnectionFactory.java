// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.DriverManagerConnectionFactory;

/**
 * ConnectionFactory implementation which allows for retry on exception
 *
 */
public class RetryConnectionFactory extends DriverManagerConnectionFactory {

    public RetryConnectionFactory(String connectUri, Properties props) {
        super(connectUri, props);
    }

    @Override
    public Connection createConnection() throws SQLException {
        AbstractRetry<Connection> exec = new AbstractRetry<Connection>() {
            @Override
            public ExecuteResult<Connection> execute() throws SQLException {
                Connection conn = superCreateConnection();
                return new ExecuteResult<Connection>(new RetryConnection(conn));
            }

            @Override
            protected boolean retryException(SQLException sqle) {
                return (super.retryException(sqle) || Db.errorMatches(sqle, Db.Error.CANTOPEN));
            }

        };
        return exec.doRetry().getResult();
    }

    private Connection superCreateConnection() throws SQLException {
        return super.createConnection();
    }
}
