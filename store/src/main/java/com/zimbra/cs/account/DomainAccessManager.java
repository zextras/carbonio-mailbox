// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.EmailUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.accesscontrol.Right;

public class DomainAccessManager extends AccessManager {

    @Override
    public boolean isDomainAdminOnly(AuthToken at) {
        return at.isDomainAdmin() && !at.isAdmin();
    }

    @Override
    public boolean isAdequateAdminAccount(Account acct) {
        return acct.getBooleanAttr(Provisioning.A_zimbraIsDomainAdminAccount, false) ||
               acct.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false);
    }

    @Override
    public boolean canAccessAccount(AuthToken at, Account target, boolean asAdmin) throws ServiceException {
        if (!at.isZimbraUser())
            return false;

        boolean isDomainTheSame = StringUtil.equal(at.getAccount().getDomainId(), target.getDomainId());
        if (!isDomainTheSame) {
            checkDomainStatus(target);
        }

        if (asAdmin && at.isAdmin()) return true;
        if (isParentOf(at, target)) return true;
        if (!(asAdmin && at.isDomainAdmin())) return false;
        // don't allow a domain-only admin to access a global admin's account
        if (target.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false)) return false;
        return isDomainTheSame;
    }

    @Override
    public boolean canAccessAccount(AuthToken at, Account target) throws ServiceException {
        return canAccessAccount(at, target, true);
    }

    /** Returns whether the specified account's credentials are sufficient
     *  to perform operations on the target account.  This occurs when the
     *  credentials belong to an admin or when the credentials are for an
     *  appropriate domain admin.  <i>Note: This method checks only for admin
     *  access, and passing the same account for <code>credentials</code> and
     *  <code>target</code> will not succeed for non-admin accounts.</i>
     * @param credentials  The authenticated account performing the action.
     * @param target       The target account for the proposed action.
     * @param asAdmin      If the authenticated account is acting as an admin account*/
    @Override
    public boolean canAccessAccount(Account credentials, Account target, boolean asAdmin) throws ServiceException {
        if (credentials == null)
            return false;

        boolean isDomainTheSame = StringUtil.equal(credentials.getDomainId(), target.getDomainId());
        if (!isDomainTheSame) {
            checkDomainStatus(target);
        }

        // admin auth account will always succeed
        if (asAdmin && credentials.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false))
            return true;
        // parent auth account will always succeed
        if (isParentOf(credentials, target))
            return true;
        // don't allow access if the authenticated account is not acting as an admin
        if (!asAdmin)
            return false;
        // don't allow a domain-only admin to access a global admin's account
        if (target.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false))
            return false;
        // domain admins succeed if the target is in the same domain
        if (isDomainTheSame)
            return credentials.getBooleanAttr(Provisioning.A_zimbraIsDomainAdminAccount, false);
        // everyone else is out of luck
        return false;
    }

    @Override
    public boolean canAccessAccount(Account credentials, Account target) throws ServiceException {
        return canAccessAccount(credentials, target, true);
    }

    private boolean canAccessDomainInternal(AuthToken at, String domainName) throws ServiceException {
        if (at.isAdmin()) {
            return true;
        }
        if (!at.isDomainAdmin()) {
            return false;
        }
        return getDomain(at).getName().equalsIgnoreCase(domainName);
    }

    @Override
    public boolean canAccessDomain(AuthToken at, String domainName) throws ServiceException {
        if (!at.isZimbraUser())
            return false;
        checkDomainStatus(domainName);
        return canAccessDomainInternal(at, domainName);
    }

    @Override
    public boolean canAccessDomain(AuthToken at, Domain domain) throws ServiceException {
        if (!at.isZimbraUser())
            return false;
        checkDomainStatus(domain);
        return canAccessDomainInternal(at, domain.getName());
    }

    @Override
    public boolean canAccessCos(AuthToken at, Cos cos) throws ServiceException {
        if (!at.isZimbraUser())
            return false;

        if (at.isAdmin()) return true;
        if (!at.isDomainAdmin()) return false;

        String cosId = cos.getId();

        Domain domain = getDomain(at);
        Set<String> allowedCoses = domain.getMultiAttrSet(Provisioning.A_zimbraDomainCOSMaxAccounts);
        for (String c : allowedCoses) {
            String[] parts = c.split(":");
            if (parts.length != 2)
                continue;  // bad value skip
            String id = parts[0];
            if (id.equals(cosId))
                return true;
        }
        return false;
    }


    @Override
    public boolean canCreateGroup(AuthToken at, String groupEmail)
            throws ServiceException {
        return false;
    }

    @Override
    public boolean canCreateGroup(Account credentials, String groupEmail)
            throws ServiceException {
        return false;
    }

    @Override
    public boolean canAccessGroup(AuthToken at, Group group)
            throws ServiceException {
        return false;
    }

    @Override
    public boolean canAccessGroup(Account credentials, Group group, boolean asAdmin)
            throws ServiceException {
        return false;
    }

    @Override
    public boolean canAccessEmail(AuthToken at, String email) throws ServiceException {
        String parts[] = EmailUtil.getLocalPartAndDomain(email);
        if (parts == null)
            throw ServiceException.INVALID_REQUEST("must be valid email address: "+email, null);

        // check for family mailbox
        Account targetAcct = Provisioning.getInstance().get(Key.AccountBy.name, email, at);
        if (targetAcct != null) {
            if (isParentOf(at, targetAcct))
                return true;
        }
        return canAccessDomain(at, parts[1]);
    }

    @Override
    public boolean canModifyMailQuota(AuthToken at, Account targetAccount, long mailQuota) throws ServiceException {
        if (!canAccessAccount(at,  targetAccount))
            return false;

        return canSetMailQuota(at, targetAccount, mailQuota);
    }

    // public static because of bug 42896.
    // change back to non-static protected when we support constraints on a per admin basis
    public static boolean canSetMailQuota(AuthToken at, Account targetAccount, long quota) throws ServiceException {
        if (at.isAdmin()) return true;

        Account adminAccount = Provisioning.getInstance().get(Key.AccountBy.id,  at.getAccountId(), at);
        if (adminAccount == null) return false;

        // 0 is unlimited
        long maxQuota = adminAccount.getLongAttr(Provisioning.A_zimbraDomainAdminMaxMailQuota, -1);

        // return true if they can set quotas to anything
        if (maxQuota == 0)
            return true;

        if ((maxQuota == -1) ||    // they don't permsission to change any quotas
            (quota == 0) ||        // they don't have permission to assign unlimited quota
            (quota > maxQuota)     // the quota they are tying to assign is too big
           ) {
            ZimbraLog.account.warn(String.format("invalid attempt to change quota: admin(%s) account(%s) quota(%d) max(%d)",
                    adminAccount.getName(), targetAccount.getName(), quota, maxQuota));
            return false;
        } else {
            return true;
        }
    }

    /* ===========================================================================================
     * ACL based access methods
     *
     * - not supported by DomainAccessManager
     * - DomainAccessManager will be retired after ACL based access control is fully implemented.
     *
     * ===========================================================================================
     */
    @Override
    public boolean canDo(AuthToken grantee, Entry target, Right rightNeeded, boolean asAdmin) {
        return false;
    }

    @Override
    public boolean canDo(MailTarget grantee, Entry target, Right rightNeeded, boolean asAdmin) {
        return false;
    }

    @Override
    public boolean canDo(String grantee, Entry target, Right rightNeeded, boolean asAdmin) {
        return false;
    }

    @Override
    public boolean canGetAttrs(Account grantee,   Entry target, Set<String> attrs, boolean asAdmin) throws ServiceException {
        return false;
    }

    @Override
    public boolean canGetAttrs(AuthToken grantee, Entry target, Set<String> attrs, boolean asAdmin) throws ServiceException {
        return false;
    }

    @Override
    public boolean canSetAttrs(Account grantee,   Entry target, Set<String> attrs, boolean asAdmin) throws ServiceException {
        return false;
    }

    @Override
    public boolean canSetAttrs(AuthToken grantee, Entry target, Set<String> attrs, boolean asAdmin) throws ServiceException {
        return false;
    }

    @Override
    public boolean canSetAttrs(Account grantee,   Entry target, Map<String, Object> attrs, boolean asAdmin) throws ServiceException {
        return false;
    }

    @Override
    public boolean canSetAttrs(AuthToken grantee, Entry target, Map<String, Object> attrs, boolean asAdmin) throws ServiceException {
        return false;
    }

}
