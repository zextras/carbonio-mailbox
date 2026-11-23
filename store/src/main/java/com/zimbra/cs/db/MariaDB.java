// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.zimbra.common.localconfig.LC;
import java.util.Properties;


public class MariaDB extends MySQL {

    @Override
    DbPool.PoolConfig getPoolConfig() {
        return new MariaDBConfig();
    }

    protected static class MariaDBConfig extends MySQLConfig {

        @Override
        protected String getDriverClassName() {
            return "org.mariadb.jdbc.Driver";
        }

        @Override
        protected String getRootUrl() {
            String bindAddress = LC.mysql_bind_address.value();
            if (bindAddress.indexOf(':') > -1) {
                bindAddress = "[" + bindAddress + "]";
            }

            return "jdbc:mysql://" + bindAddress + ":" + LC.mysql_port.value() + "/";
        }

        @Override
        protected Properties getDBProperties() {
            Properties props = super.getDBProperties();
            props.putIfAbsent("sessionVariables", "tx_isolation='READ-COMMITTED'");
            return props;
        }

    }
}
