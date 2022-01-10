// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Aug 31, 2005
 */
package com.zimbra.cs.session;

import java.util.ArrayList;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;

class DirectorySearchParams {

    private SearchDirectoryOptions mSearchOpts;
    private List<NamedEntry> mResult;
    private NamedEntry.CheckRight mRightChecker;
    
    DirectorySearchParams(SearchDirectoryOptions searchOpts, NamedEntry.CheckRight rightChecker) {
        mSearchOpts = searchOpts;
        mRightChecker = rightChecker;
    }
    
    List<NamedEntry> getResult() {
        return mResult;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectorySearchParams)) {
            return false;
        }
        
        if (o == this) {
            return true;
        }
        
        DirectorySearchParams other = (DirectorySearchParams) o;
        return mSearchOpts.equals(other.mSearchOpts);
    }
    
    void doSearch() throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        mResult = prov.searchDirectory(mSearchOpts);
        
        if (mRightChecker != null) {
            List<NamedEntry> allowed = new ArrayList<NamedEntry>();
            for (int i = 0; i < mResult.size(); i++) {
                NamedEntry entry = (NamedEntry)mResult.get(i);
                if (mRightChecker.allow(entry)) {
                    allowed.add(entry);
                }
            }
            mResult = allowed;
        }
    }
}
