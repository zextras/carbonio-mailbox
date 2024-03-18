// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import java.util.HashSet;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;

public abstract class DataSourceListner {

    public static final String CREATE_DATASOURCE = "CreateDataSource";
    public static final String DELETE_DATASOURCE = "DeleteDataSource";

    private static final HashSet<DataSourceListner> listeners;

    static {
        listeners = new HashSet<>();
        reset();
    }

    public enum DataSourceAction {
        CREATE(CREATE_DATASOURCE), DELETE(DELETE_DATASOURCE);

        private String action;

        /**
         * @param name
         * @param ordinal
         */
        private DataSourceAction(String name) {
            this.action = name;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return action;
        }

    }

    static void reset() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    public abstract void notify(Account account, DataSource dataSource, DataSourceAction action);

    public static void register(DataSourceListner listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public static void unregister(DataSourceListner listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public static void createDataSource(Account account, DataSource dataSource) {
        for (DataSourceListner dataSourceListener : listeners) {
            dataSourceListener.notify(account, dataSource, DataSourceAction.CREATE);
        }
    }

    public static void deleteDataSource(Account account, DataSource dataSource) {
        for (DataSourceListner dataSourceListener : listeners) {
            dataSourceListener.notify(account, dataSource, DataSourceAction.DELETE);
        }
    }

}
