// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ConnectionFactory implementation which allows for retry on exception
 *
 */
public class RetryConnectionFactory {

    private final String connectUri;
    private final Properties props;

    public RetryConnectionFactory(String connectUri, Properties props) {
        this.connectUri = connectUri;
        this.props = props;
    }

    public Connection createConnection() throws SQLException {
        AbstractRetry<Connection> exec = new AbstractRetry<>() {
          @Override
          public ExecuteResult<Connection> execute() throws SQLException {
            Connection conn = DriverManager.getConnection(connectUri, props);
            return new ExecuteResult<>(new RetryConnection(conn));
          }

          @Override
          protected boolean retryException(SQLException sqle) {
            return (super.retryException(sqle) || Db.errorMatches(sqle, Db.Error.CANTOPEN));
          }

        };
        return exec.doRetry().getResult();
    }
}
