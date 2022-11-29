// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import java.util.Collection;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.db.DbDataSource;
import com.zimbra.cs.db.DbDataSource.DataSourceItem;

public class DataSourceFolderMapping extends DataSourceMapping {
    public DataSourceFolderMapping(DataSource ds, DataSourceItem dsi) throws ServiceException {
	super(ds, dsi);
    }
    
    public DataSourceFolderMapping(DataSource ds, int itemId) throws ServiceException {
	super(ds, itemId);
    }
    
    public DataSourceFolderMapping(DataSource ds, String remoteId) throws ServiceException {
	super(ds, remoteId);
    }
    
    public DataSourceFolderMapping(DataSource ds, int itemId, String remoteId) throws ServiceException {
        super(ds, ds.getFolderId(), itemId, remoteId);
    }

    @Override
    public void delete() throws ServiceException {
        deleteMappings(ds, dsi.itemId);
        super.delete();
    }
    
    public void deleteMappings() throws ServiceException {
        deleteMappings(ds, dsi.itemId);
    }
    
    public static void deleteMappings(DataSource ds, int itemId)
        throws ServiceException {
        DbDataSource.deleteAllMappingsInFolder(ds, itemId);
    }
    
    public Collection<DataSourceItem> getMappings() throws ServiceException {
        return getMappings(ds, dsi.itemId);
    }

    public Collection<DataSourceItem> getMappingsAndFlags() throws ServiceException {
        return getMappingsAndFlags(ds, dsi.itemId);
    }

    public static Collection<DataSourceItem> getMappings(DataSource ds, int
        folderId) throws ServiceException {
        return DbDataSource.getAllMappingsInFolder(ds, folderId);
    }

    public static Collection<DataSourceItem> getMappingsAndFlags(DataSource ds, int
        folderId) throws ServiceException {
        return DbDataSource.getAllMappingsAndFlagsInFolder(ds, folderId);
    }
    
    protected void parseMetaData() throws ServiceException {}
}
