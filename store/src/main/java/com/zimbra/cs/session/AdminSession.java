// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Aug 31, 2005
 */
package com.zimbra.cs.session;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.SearchDirectoryOptions;

import java.util.HashMap;
import java.util.List;

/** @author dkarp */
public class AdminSession extends Session {

    private static final long ADMIN_SESSION_TIMEOUT_MSEC = 10 * Constants.MILLIS_PER_MINUTE;
  
    private DirectorySearchParams mSearchParams;
    private HashMap<String,Object> mData = new HashMap<String,Object>();

    public AdminSession(String accountId) {
        super(accountId, Session.Type.ADMIN);
    }

    @Override
    protected boolean isMailboxListener() {
        return false;
    }

    @Override
    protected boolean isRegisteredInCache() {
        return true;
    }

    @Override
    protected long getSessionIdleLifetime() {
        return ADMIN_SESSION_TIMEOUT_MSEC;
    }
    
    public Object getData(String key) { return mData.get(key); }
    public void setData(String key, Object data) { mData.put(key, data); }
    public void clearData(String key) { mData.remove(key); }

    @Override public void notifyPendingChanges(PendingModifications pns, int changeId, Session source) { }

    @Override protected void cleanup() { }

    public List<NamedEntry> searchDirectory(SearchDirectoryOptions searchOpts,
            int offset, NamedEntry.CheckRight rightChecker) 
    throws ServiceException {
        
        DirectorySearchParams params = new DirectorySearchParams(searchOpts, rightChecker);
        
        boolean needToSearch =  (offset == 0) || (mSearchParams == null) || !mSearchParams.equals(params);
        //ZimbraLog.account.info("this="+this+" mSearchParams="+mSearchParams+" equal="+!params.equals(mSearchParams));
        if (needToSearch) {
            //ZimbraLog.account.info("doing new search: "+query+ " offset="+offset);
            params.doSearch();
            mSearchParams = params;
        } else {
            //ZimbraLog.account.info("cached search: "+query+ " offset="+offset);
        }
        return mSearchParams.getResult();
    }

}
