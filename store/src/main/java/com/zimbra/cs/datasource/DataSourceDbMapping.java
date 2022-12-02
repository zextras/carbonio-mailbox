// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import java.util.Collection;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.db.DbDataSource;
import com.zimbra.cs.db.DbDataSource.DataSourceItem;

public class DataSourceDbMapping {

    private static DataSourceDbMapping instance;

    public static synchronized DataSourceDbMapping getInstance() {
        if (instance == null) {
            instance = new DataSourceDbMapping();
        }
        return instance;
    }

    @VisibleForTesting
    public static synchronized void setInstance(DataSourceDbMapping mapping) {
        instance = mapping;
    }

    public void updateMapping(DataSource ds, DataSourceItem item) throws ServiceException {
        DbDataSource.updateMapping(ds, item);
    }

    public void addMapping(DataSource ds, DataSourceItem item) throws ServiceException {
        DbDataSource.addMapping(ds, item);
    }

    public void deleteMapping(DataSource ds, int itemId) throws ServiceException {
        DbDataSource.deleteMapping(ds, itemId);
    }

    public DataSourceItem getReverseMapping(DataSource ds, String remoteId) throws ServiceException {
        return DbDataSource.getReverseMapping(ds, remoteId);
    }

    public DataSourceItem getMapping(DataSource ds, int itemId) throws ServiceException {
        return DbDataSource.getMapping(ds, itemId);
    }

    public Collection<DataSourceItem> getAllMappingsInFolder(DataSource ds, int folderId) throws ServiceException {
        return DbDataSource.getAllMappingsInFolder(ds, folderId);
    }
}
