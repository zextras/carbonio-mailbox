// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.EmailUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightCommand.AllEffectiveRights;
import com.zimbra.cs.account.accesscontrol.Rights.User;

public class GlobalAccessManager extends AccessManager implements AdminConsoleCapable {

    // embed a ACLAccessManager for handling user rights
    private ACLAccessManager mAclAccessManager;

    public GlobalAccessManager() {
        try {
            mAclAccessManager = new ACLAccessManager();
        } catch (ServiceException e) {
            ZimbraLog.acl.warn("unable to instantiate ACLAccessManager, user rights will not be honored", e);
        }
    }

    @Override
    public boolean isAdequateAdminAccount(Account acct) {
        return acct.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false);
    }

    @Override
    public boolean canAccessAccount(AuthToken at, Account target,
            boolean asAdmin) throws ServiceException {
        if (!at.isZimbraUser())
            return false;

        checkDomainStatus(target);

        if (isGlobalAdmin(at, asAdmin))
            return true;

        if (isParentOf(at, target))
            return true;

        return canDo(at, target, User.R_loginAs, asAdmin);
    }

    @Override
    public boolean canAccessAccount(AuthToken at, Account target)
            throws ServiceException {
        return canAccessAccount(at, target, true);
    }

    @Override
    public boolean canAccessAccount(Account credentials, Account target,
            boolean asAdmin) throws ServiceException {
        if (credentials == null)
            return false;

        checkDomainStatus(target);

        // admin auth account will always succeed
        if (AccessControlUtil.isGlobalAdmin(credentials, asAdmin))
            return true;

        // parent auth account will always succeed
        if (isParentOf(credentials, target))
            return true;

        return canDo(credentials, target, User.R_loginAs, asAdmin);
    }

    @Override
    public boolean canAccessAccount(Account credentials, Account target)
            throws ServiceException {
        return canAccessAccount(credentials, target, true);
    }

    @Override
    public boolean canAccessCos(AuthToken at, Cos cos) throws ServiceException {
        if (!at.isZimbraUser())
            return false;

        return isGlobalAdmin(at);
    }

    @Override
    public boolean canCreateGroup(AuthToken at, String groupEmail)
            throws ServiceException {
        Domain domain = Provisioning.getInstance().getDomainByEmailAddr(groupEmail);
        checkDomainStatus(domain);

        boolean asAdmin = true;
        Right rightNeeded = User.R_createDistList;
        Account authedAcct = AccessControlUtil.authTokenToAccount(at, rightNeeded);

        if (AccessControlUtil.isGlobalAdmin(authedAcct, asAdmin)) {
            return true;
        }

        return canDo(at, domain, rightNeeded, asAdmin);
    }

    @Override
    public boolean canCreateGroup(Account credentials, String groupEmail)
            throws ServiceException {
        Domain domain = Provisioning.getInstance().getDomainByEmailAddr(groupEmail);
        checkDomainStatus(domain);

        boolean asAdmin = true;
        Right rightNeeded = User.R_createDistList;
        Account authedAcct = credentials;

        if (AccessControlUtil.isGlobalAdmin(authedAcct, asAdmin)) {
            return true;
        }

        return canDo(credentials, domain, rightNeeded, asAdmin);
    }

    @Override
    public boolean canAccessGroup(AuthToken at, Group group) throws ServiceException {
        checkDomainStatus(group);

        boolean asAdmin = true;
        Right rightNeeded = Group.GroupOwner.GROUP_OWNER_RIGHT;
        Account authedAcct = AccessControlUtil.authTokenToAccount(at, rightNeeded);

        if (AccessControlUtil.isGlobalAdmin(authedAcct, asAdmin)) {
            return true;
        }

        return canDo(at, group, rightNeeded, asAdmin);
    }

    @Override
    public boolean canAccessGroup(Account credentials, Group group, boolean asAdmin)
    throws ServiceException {
        checkDomainStatus(group);

        Right rightNeeded = Group.GroupOwner.GROUP_OWNER_RIGHT;
        Account authedAcct = credentials;

        if (AccessControlUtil.isGlobalAdmin(authedAcct, asAdmin)) {
            return true;
        }

        return canDo(credentials, group, rightNeeded, asAdmin);
    }

    @Override
    public boolean canAccessDomain(AuthToken at, String domainName)
            throws ServiceException {
        if (!at.isZimbraUser())
            return false;
        checkDomainStatus(domainName);

        return isGlobalAdmin(at);
    }

    @Override
    public boolean canAccessDomain(AuthToken at, Domain domain)
            throws ServiceException {
        if (!at.isZimbraUser())
            return false;
        checkDomainStatus(domain);

        return isGlobalAdmin(at);
    }

    @Override
    public boolean canAccessEmail(AuthToken at, String email)
            throws ServiceException {
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
    public boolean canDo(MailTarget grantee, Entry target, Right rightNeeded, boolean asAdmin) {
        if (rightNeeded != null && rightNeeded.isUserRight()) {
            // need a user right, delegate to the ACLAccessmanager
            if (mAclAccessManager != null)
                return mAclAccessManager.canDo(grantee, target, rightNeeded, asAdmin);
            else
                return false;
        }
        return AccessControlUtil.isGlobalAdmin(grantee, asAdmin);
    }

    @Override
    public boolean canDo(AuthToken grantee, Entry target, Right rightNeeded,
            boolean asAdmin) {

        Account granteeAcct = AccessControlUtil.authTokenToAccount(grantee, rightNeeded);
        if (granteeAcct != null)
            return canDo(granteeAcct, target, rightNeeded, asAdmin);
        else
            return false;
    }

    @Override
    public boolean canDo(String granteeEmail, Entry target, Right rightNeeded,
            boolean asAdmin) {
        Account granteeAcct = AccessControlUtil.emailAddrToAccount(granteeEmail, rightNeeded);
        if (granteeAcct != null)
            return canDo(granteeAcct, target, rightNeeded, asAdmin);
        else
            return false;
    }


    @Override
    public AttrRightChecker getGetAttrsChecker(Account credentials,   Entry target, boolean asAdmin) throws ServiceException {
        if (AccessControlUtil.isGlobalAdmin(credentials, asAdmin) == Boolean.TRUE)
            return AllowedAttrs.ALLOW_ALL_ATTRS();
        else
            return AllowedAttrs.DENY_ALL_ATTRS();
    }

    @Override
    public AttrRightChecker getGetAttrsChecker(AuthToken credentials, Entry target, boolean asAdmin) throws ServiceException {
        return getGetAttrsChecker(credentials.getAccount(), target, asAdmin);
    }

    @Override
    public boolean canGetAttrs(Account credentials, Entry target,
            Set<String> attrs, boolean asAdmin) throws ServiceException {
        return AccessControlUtil.isGlobalAdmin(credentials, asAdmin);
    }

    @Override
    public boolean canGetAttrs(AuthToken credentials, Entry target,
            Set<String> attrs, boolean asAdmin) throws ServiceException {
        return isGlobalAdmin(credentials, asAdmin);
    }

    @Override
    public boolean canModifyMailQuota(AuthToken at, Account targetAccount,
            long mailQuota) throws ServiceException {
        return isGlobalAdmin(at);
    }

    @Override
    public boolean canSetAttrs(Account credentials, Entry target,
            Set<String> attrs, boolean asAdmin) throws ServiceException {
        return AccessControlUtil.isGlobalAdmin(credentials, asAdmin);
    }

    @Override
    public boolean canSetAttrs(AuthToken credentials, Entry target,
            Set<String> attrs, boolean asAdmin) throws ServiceException {
        return isGlobalAdmin(credentials, asAdmin);
    }

    @Override
    public boolean canSetAttrs(Account credentials, Entry target,
            Map<String, Object> attrs, boolean asAdmin) throws ServiceException {
        return AccessControlUtil.isGlobalAdmin(credentials, asAdmin);
    }

    @Override
    public boolean canSetAttrs(AuthToken credentials, Entry target,
            Map<String, Object> attrs, boolean asAdmin) throws ServiceException {
        return isGlobalAdmin(credentials, asAdmin);
    }

    @Override
    public boolean isDomainAdminOnly(AuthToken at) {
        return false;
    }

    private boolean isGlobalAdmin(AuthToken at) {
        return isGlobalAdmin(at, true);
    }

    private boolean isGlobalAdmin(AuthToken at, boolean asAdmin) {
        return asAdmin && at.isAdmin();
    }


    // ===========================
    // AdminConsoleCapable methods
    // ===========================

    @Override
    public void getAllEffectiveRights(RightBearer rightBearer,
            boolean expandSetAttrs, boolean expandGetAttrs,
            AllEffectiveRights result) throws ServiceException {
        CollectAllEffectiveRights.getAllEffectiveRights(rightBearer, expandSetAttrs, expandGetAttrs, result);
    }

    @Override
    public void getEffectiveRights(RightBearer rightBearer, Entry target,
            boolean expandSetAttrs, boolean expandGetAttrs,
            RightCommand.EffectiveRights result) throws ServiceException {
        CollectEffectiveRights.getEffectiveRights(rightBearer, target, expandSetAttrs, expandGetAttrs, result);

    }

    @Override
    public Set<TargetType> targetTypesForGrantSearch() {
        // we want only targets type on which user can grant rights on
        HashSet<TargetType> tts = new HashSet<TargetType>();
        tts.add(TargetType.account);
        tts.add(TargetType.calresource);
        tts.add(TargetType.dl);
        tts.add(TargetType.group);

        return tts;
    }

}
