// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.DomainAccessManager;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.Rights.User;

/*
 * - domain based access manager
 * - support user ACL only, not admin rights.
 */
public class DomainACLAccessManager extends DomainAccessManager {

    public DomainACLAccessManager() throws ServiceException {
        // initialize RightManager
        RightManager.getInstance();
    }

    @Override
    public boolean canAccessAccount(AuthToken at, Account target, boolean asAdmin) throws ServiceException {
        if (super.canAccessAccount(at, target, asAdmin))
            return true;
        else
            return canDo(at, target, User.R_loginAs, asAdmin, false);
    }

    @Override
    public boolean canAccessAccount(AuthToken at, Account target) throws ServiceException {
        return canAccessAccount(at, target, true);
    }

    @Override
    public boolean canAccessAccount(Account credentials, Account target, boolean asAdmin) throws ServiceException {
        if (super.canAccessAccount(credentials, target, asAdmin))
            return true;
        else
            return canDo(credentials, target, User.R_loginAs, asAdmin, false);
    }

    @Override
    public boolean canAccessAccount(Account credentials, Account target) throws ServiceException {
        return canAccessAccount(credentials, target, true);
    }

    public boolean canDo(MailTarget grantee, Entry target, Right rightNeeded, boolean asAdmin, boolean defaultGrant) {
        try {
            if (grantee == null)
                grantee = GuestAccount.ANONYMOUS_ACCT;

            // 1. always allow self
            if (target instanceof Account) {
                if (((Account)target).getId().equals(grantee.getId()))
                    return true;
            }

            // 2. check admin access - if the right being asked for is not loginAs
            if (rightNeeded != Rights.User.R_loginAs) {
                if (target instanceof Account) {
                    if ((grantee instanceof Account) && canAccessAccount((Account)grantee, (Account)target, asAdmin))
                        return true;
                }
            }

            // 3. check ACL
            Boolean result = CheckPresetRight.check(grantee, target, rightNeeded, false, null);
            if (result != null)
                return result.booleanValue();
            else {
                // no ACL, see if there is a configured default
                Boolean defaultValue = rightNeeded.getDefault();
                if (defaultValue != null)
                    return defaultValue.booleanValue();

                // no configured default, return default requested by the callsite
                return defaultGrant;
            }

        } catch (ServiceException e) {
            ZimbraLog.account.warn("ACL checking failed: " +
                                   "grantee=" + grantee.getName() +
                                   ", target=" + target.getLabel() +
                                   ", right=" + rightNeeded.getName() +
                                   " => denied", e);
        }
        return false;
    }

    public boolean canDo(AuthToken grantee, Entry target, Right rightNeeded, boolean asAdmin, boolean defaultGrant) {
        try {
            Account granteeAcct;
            if (grantee == null)
                granteeAcct = GuestAccount.ANONYMOUS_ACCT;
            else if (grantee.isZimbraUser())
                granteeAcct = Provisioning.getInstance().get(Key.AccountBy.id, grantee.getAccountId());
            else
                granteeAcct = new GuestAccount(grantee);

            return canDo(granteeAcct, target, rightNeeded, asAdmin, defaultGrant);
        } catch (ServiceException e) {
            ZimbraLog.account.warn("ACL checking failed: " +
                                   "grantee=" + grantee.getAccountId() +
                                   ", target=" + target.getLabel() +
                                   ", right=" + rightNeeded.getName() +
                                   " => denied", e);
        }

        return false;
    }

    public boolean canDo(String granteeEmail, Entry target, Right rightNeeded, boolean asAdmin, boolean defaultGrant) {
        try {
            Account granteeAcct = null;

            if (granteeEmail != null)
                granteeAcct = Provisioning.getInstance().get(Key.AccountBy.name, granteeEmail);
            if (granteeAcct == null)
                granteeAcct = GuestAccount.ANONYMOUS_ACCT;

            return canDo(granteeAcct, target, rightNeeded, asAdmin, defaultGrant);
        } catch (ServiceException e) {
            ZimbraLog.account.warn("ACL checking failed: " +
                                   "grantee=" + granteeEmail +
                                   ", target=" + target.getLabel() +
                                   ", right=" + rightNeeded.getName() +
                                   " => denied", e);
        }

        return false;
    }


}
