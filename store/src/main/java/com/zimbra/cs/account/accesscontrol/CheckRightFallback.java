// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Entry;

public abstract class CheckRightFallback {
    protected Right mRight;

    void setRight(Right right) {
        mRight = right;
    }
    
    public Boolean checkRight(Account authedAcct, Entry target, boolean asAdmin) {
        try {
            return doCheckRight(authedAcct, target, asAdmin);
        } catch (ServiceException e) {
            ZimbraLog.acl.warn("caught exception in checkRight fallback" +
                    ", checkRight fallback for right [" + mRight.getName() +"] skipped", e);
            return null;
        }
    }
    
    protected abstract Boolean doCheckRight(Account grantee, Entry target, boolean asAdmin) throws ServiceException;
    
}
