// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource.imap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class SyncStateManager {
    private final Map<String, SyncState> entries;

    private static final SyncStateManager INSTANCE = new SyncStateManager();

    public static SyncStateManager getInstance() {
        return INSTANCE;
    }
    
    private SyncStateManager() {
        entries = Collections.synchronizedMap(new HashMap<String, SyncState>());
    }

    public SyncState getSyncState(DataSource ds) {
        return entries.get(ds.getId());
    }
    
    public SyncState getOrCreateSyncState(DataSource ds) throws ServiceException {
        synchronized (entries) {
            SyncState ss = entries.get(ds.getId());
            if (ss == null) {
                ss = new SyncState(ds.getMailbox());
                entries.put(ds.getId(), ss);
            }
            return ss;
        }
    }

    public void removeSyncState(String dataSourceId) {
        entries.remove(dataSourceId);
    }
}
