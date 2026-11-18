// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.gal.GalGroup.GroupInfo;

public class GalGroupInfoProvider {

    private static GalGroupInfoProvider instance;
    
    public static synchronized GalGroupInfoProvider getInstance() {
        if (instance == null) {
            instance = new GalGroupInfoProvider();
        }
        return instance;
    }
    
    public GroupInfo getGroupInfo(String addr, boolean needCanExpand, Account requestedAcct, Account authedAcct) {
        return GalGroup.getGroupInfo(addr, true, requestedAcct, authedAcct);
    }

}
