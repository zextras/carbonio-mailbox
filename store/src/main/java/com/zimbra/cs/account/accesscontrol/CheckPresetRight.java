// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccessManager.ViaGrant;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.GroupMembership;
import com.zimbra.cs.account.accesscontrol.PermissionCache.CachedPermission;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;

/**
 * Check if grantee is allowed for rightNeeded on target entry.
 */

public class CheckPresetRight extends CheckRight {
    private static final Log sLog = ZimbraLog.acl;

    private final MailTarget mGranteeMailTarget;
    private final ViaGrant mVia;

    // derived from input or aux vars
    private GroupMembership mGranteeGroups;
    private final SeenRight mSeenRight;

    private static class SeenRight {
        private boolean mSeen;

        void setSeenRight() {
            mSeen = true;
        }

        boolean seenRight() {
            return mSeen;
        }
    }

    /**
     * Check if grantee is allowed for rightNeeded on target entry.
     *
     * @param grantee
     * @param target
     * @param rightNeeded
     * @param canDelegateNeeded if we are checking for "can delegate" the right
     * @param via if not null, will be populated with the grant info via which the result was determined.
     *        NOTE: cache will NOT be used if via needs to be populated.
     * @return Boolean.TRUE if allowed,
     *         Boolean.FALSE if denied,
     *         null if there is no grant applicable to the rightNeeded.
     * @throws ServiceException
     */
    public static Boolean check(MailTarget grantee, Entry target,
            Right rightNeeded, boolean canDelegateNeeded, ViaGrant via)
    throws ServiceException {

        CachedPermission cached = null;
        /* Via information is not stored in the cache, so if we need it, don't use the cache */
        if (via == null) {
            cached = PermissionCache.cacheGet(grantee, target, rightNeeded, canDelegateNeeded);
        }

        Boolean allowed;

        if ((cached == null) || (cached == CachedPermission.NOT_CACHED)) {
            CheckPresetRight checker = new CheckPresetRight(grantee, target, rightNeeded, canDelegateNeeded, via);
            allowed = checker.checkRight();
            PermissionCache.cachePut(grantee, target, rightNeeded, canDelegateNeeded, allowed);
        } else {
            allowed = cached.getResult();
        }

        if (sLog.isDebugEnabled()) {
            sLog.debug("check ACL: %s (target=%s, grantee=%s, right=%s, canDelegateNeeded=%s, wasCached=%s)",
                        (allowed==null ? "no matching ACL" : allowed), target.getLabel(), grantee.getName(),
                        rightNeeded.getName(), canDelegateNeeded, cached);
        }

        return allowed;
    }

    private CheckPresetRight(MailTarget grantee, Entry target,
            Right rightNeeded, boolean canDelegateNeeded, ViaGrant via)
    throws ServiceException {
        super(target, rightNeeded, canDelegateNeeded);

        mGranteeMailTarget = grantee;
        mVia = via;
        mTargetType = TargetType.getTargetType(mTarget);
        mSeenRight = new SeenRight();
    }

    /**
     * Includes the grantee target itself if it is a group
     */
    private GroupMembership getGranteeGroups() throws ServiceException {
        if (mGranteeGroups == null) {
            // get all groups(static and dynamic) the grantee belongs
            // get only admin groups if the right is an admin right
            boolean adminGroupsOnly = !mRightNeeded.isUserRight();
            mGranteeGroups = mProv.getGroupMembership(mGranteeMailTarget, adminGroupsOnly);
            if (mGranteeMailTarget instanceof DistributionList) {
                DistributionList dlmt = (DistributionList) mGranteeMailTarget;
                if (!adminGroupsOnly || dlmt.isIsAdminGroup()) {
                    mGranteeGroups.append(new Provisioning.MemberOf(
                            dlmt.getId(), dlmt.isIsAdminGroup(), /* is dynamic group */ false), dlmt.getId());
                }
            }
        }
        return mGranteeGroups;
    }

    private boolean matchesGroupGrantee(ZimbraACE ace) throws ServiceException {
        if (getGranteeGroups().groupIds().contains(ace.getGrantee())) {
            return true;
        } else if (ace.getGranteeType() == GranteeType.GT_EXT_GROUP) {
            return ace.matchesGrantee(mGranteeMailTarget, !mRightNeeded.isUserRight());
        } else {
            return false;
        }
    }

    private Boolean checkRight() throws ServiceException {

        if (!mRightNeeded.isPresetRight()) {
            throw ServiceException.INVALID_REQUEST("RightChecker.canDo can only check preset right, right " +
                    mRightNeeded.getName() + " is a " + mRightNeeded.getRightType() + " right",  null);
        }

        boolean adminRight = !mRightNeeded.isUserRight();

        Domain granteeDomain = null;

        if (adminRight) {
            // if the grantee is no longer legitimate, e.g. not an admin any more, ignore all his grants
            if (!RightBearer.isValidGranteeForAdminRights(GranteeType.GT_USER, mGranteeMailTarget)) {
                return null;
            }

            granteeDomain = mProv.getDomain(mGranteeMailTarget);
            // if we ever get here, the grantee must have a domain
            if (granteeDomain == null) {
                throw ServiceException.FAILURE("internal error, cannot find domain for " +
                        mGranteeMailTarget.getName(), null);
            }

            // should only come from granting/revoking check
            if (mRightNeeded == Admin.R_crossDomainAdmin) {
                return CrossDomain.checkCrossDomainAdminRight(
                        mProv, granteeDomain, mTarget, mCanDelegateNeeded);
            }
        }

        Boolean result = null;

        // check grants explicitly granted on the target entry
        // we don't return the target entry itself in TargetIterator because if
        // target is a dl, we need to know if the dl returned from TargetIterator
        // is the target itself or one of the groups the target is in.  So we check
        // the actual target separately
        List<ZimbraACE> acl = ACLUtil.getAllACEs(mTarget);
        if (acl != null) {
            result = checkTarget(acl, false);
            if (result != null) {
                return result;
            }
        }

        //
        // if the target is a domain-ed entry, get the domain of the target.
        // It is needed for checking the cross domain right.
        //
        Domain targetDomain = TargetType.getTargetDomain(mProv, mTarget);

        // group target is only supported for admin rights
        boolean expandTargetGroups = CheckRight.allowGroupTarget(mRightNeeded);

        // check grants granted on entries from which the target entry can inherit from
        TargetIterator iter = TargetIterator.getTargetIeterator(mProv, mTarget, expandTargetGroups);
        Entry grantedOn;

        GroupACLs groupACLs = null;

        while ((grantedOn = iter.next()) != null) {
            acl = ACLUtil.getAllACEs(grantedOn);

            if (grantedOn instanceof Group) {
                if (acl == null) {
                    continue;
                }

                boolean skipPositiveGrants = false;
                if (adminRight) {
                    skipPositiveGrants = !CrossDomain.crossDomainOK(mProv, mGranteeMailTarget, granteeDomain,
                        targetDomain, (Group)grantedOn);
                }

                // don't check yet, collect all acls on all target groups
                if (groupACLs == null) {
                    groupACLs = new GroupACLs(mTarget);
                }
                groupACLs.collectACL((Group)grantedOn, skipPositiveGrants);

            } else {
                // end of group targets, put all collected denied and allowed grants into one
                // list, as if they are granted on the same entry, then check.
                // We put denied in the front, so it is consistent with ZimbraACL.getAllACEs
                if (groupACLs != null) {
                    List<ZimbraACE> aclsOnGroupTargets = groupACLs.getAllACLs();
                    if (aclsOnGroupTargets != null) {
                        result = checkTarget(aclsOnGroupTargets, false);
                    }
                    if (result != null) {
                        return result;
                    }

                    // set groupACLs to null, we are done with group targets
                    groupACLs = null;
                }

                // didn't encounter any group grantedOn, or none of them matches, just check this grantedOn entry
                if (acl == null) {
                    continue;
                }

                boolean subDomain = (mTargetType == TargetType.domain && (grantedOn instanceof Domain));
                result = checkTarget(acl, subDomain);
                if (result != null) {
                    return result;
                }
            }
        }

        if (mSeenRight.seenRight()) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    private Boolean checkTarget(List<ZimbraACE> acl, boolean subDomain)
    throws ServiceException {
        Boolean result = null;

        // if the right is user right, checking for individual match will
        // only check for user grantees, if there are any guest or key grantees
        // (there should *not* be any), they are ignored.
        short adminFlag = (mRightNeeded.isUserRight()? 0 : GranteeFlag.F_ADMIN);

        // as an individual: user, guest, key
        result = checkPresetRight(acl, (short)(GranteeFlag.F_INDIVIDUAL | adminFlag), subDomain);
        if (result != null) {
            return result;
        }

        // as a group member
        result = checkGroupPresetRight(acl, (GranteeFlag.F_GROUP), subDomain);
        if (result != null) {
            return result;
        }

        // if right is an user right, check domain, authed users and public grantees
        if (mRightNeeded.isUserRight()) {
            // as an zimbra user in the same domain
            result = checkPresetRight(acl, (GranteeFlag.F_DOMAIN), subDomain);
            if (result != null) {
                return result;
            }

            // all authed zimbra user
            result = checkPresetRight(acl, (GranteeFlag.F_AUTHUSER), subDomain);
            if (result != null) {
                return result;
            }

            // public
            result = checkPresetRight(acl, (GranteeFlag.F_PUBLIC), subDomain);
            if (result != null) {
                return result;
            }
        }

        return null;
    }


    /*
     * checks
     *     - if the grant matches required granteeFlags
     *     - if the granted right is applicable to the entry on which it is granted
     *     - if the grant is for sub domains, if subDomain is true(if the grant is not a negative grant)
     *     - if the granted right matches the requested right
     *
     *     Note: if canDelegateNeeded is true but the granted right is not delegable,
     *           we skip the grant (i.e. by returning false) and let the flow continue
     *           to check other grants, instead of denying the granting attempt.
     *
     *           That is, a more relevant executable only grant will *not* take away
     *           the grantable property of a less relevant grantable grant.
     *
     * subDomain: whether we want the grant to be for sub domains only
     */
    private boolean matchesPresetRight(ZimbraACE ace, short granteeFlags, boolean subDomain)
    throws ServiceException {
        GranteeType granteeType = ace.getGranteeType();
        if (!granteeType.hasFlags(granteeFlags)) {
            return false;
        }

        if (!CheckRight.rightApplicableOnTargetType(mTargetType, mRightNeeded, mCanDelegateNeeded)) {
            return false;
        }

        if (mCanDelegateNeeded && ace.canExecuteOnly()) {
            return false;
        }

        // negative grants are always effective on sub domains
        if (!ace.deny()) {
            if (subDomain != ace.subDomain()) {
                return false;
            }
        }

        Right rightGranted = ace.getRight();
        if ((rightGranted.isPresetRight() && rightGranted == mRightNeeded) ||
             rightGranted.isComboRight() && ((ComboRight)rightGranted).containsPresetRight(mRightNeeded)) {
            return true;
        }

        return false;
    }


    /**
     * go through each grant in the ACL
     *     - checks if the right/target of the grant matches the right/target of the grant
     *       and
     *       if the grantee type(specified by granteeFlags) is one of the grantee type
     *       we are interested in this call.
     *
     *     - if so marks the right "seen" (so callsite default(only used by user right callsites)
     *       won't be honored)
     *     - check if the Account (the grantee parameter) matches the grantee of the grant
     *
     * @param acl
     * @param granteeFlags For admin rights, because of negative grants and the more "specific"
     *                     grantee takes precedence over the less "specific" grantee, we can't
     *                     just do a single ZimbraACE.match to see if a grantee matches the grant.
     *                     Instead, we need to check more specific grantee types first, then
     *                     go on the the less specific ones.  granteeFlags specifies the
     *                     grantee type(s) we are checking for this call.
     *                     e.g. an ACL has:
     *                              adminA deny  rightR  - grant1
     *                              groupG allow rightR  - grant2
     *                              and adminA is in groupG, we want to check grant1 before grant2.
     *
     * @return
     * @throws ServiceException
     */
    private Boolean checkPresetRight(List<ZimbraACE> acl, short granteeFlags, boolean subDomain)
    throws ServiceException {
        Boolean result = null;
        for (ZimbraACE ace : acl) {
            if (!matchesPresetRight(ace, granteeFlags, subDomain)) {
                continue;
            }

            // if we get here, the right matched, mark it in seenRight.
            // This is so callsite default will not be honored.
            mSeenRight.setSeenRight();

            if (ace.matchesGrantee(mGranteeMailTarget, !mRightNeeded.isUserRight())) {
                return gotResult(ace);
            }
        }

        return result;
    }

    /*
     * Like checkPresetRight, but checks group grantees.  Instead of calling ZimbraACE.match,
     * which checks group grants using inDistributionList, we do it the other way around
     * by obtaining a GroupMembership object that contains all the groups the account is
     * in that are "eligible"(for adming rights, the group's admin flag must be on) for
     * the grant.   We check if the grantee of the grant is one of the eligible groups the
     * account is in.  If the grantee on the zimbraACE is an external group, check if
     * the account is in the external group.
     *
     * Eligible:
     *   - for admin rights granted to a group, the grant is effective only if the group
     *     has zimbraIsAdminGroup=TRUE.  If the group's zimbraIsAdminGroup is set to false
     *     after a grant is made, the grant is still there on the target entry, but
     *     becomes useless.
     */
    private Boolean checkGroupPresetRight(List<ZimbraACE> acl, short granteeFlags, boolean subDomain)
    throws ServiceException {
        Boolean result = null;

        for (ZimbraACE ace : acl) {
            if (!matchesPresetRight(ace, granteeFlags, subDomain)) {
                continue;
            }

            // if we get here, the right matched, mark it in seenRight.
            // This is so callsite default will not be honored.
            mSeenRight.setSeenRight();

            if (matchesGroupGrantee(ace)) {
                return gotResult(ace);
            }
        }
        return result;
    }

    private Boolean gotResult(ZimbraACE ace) throws ServiceException {
        if (ace.deny()) {
            if (sLog.isDebugEnabled()) {
                sLog.debug("Right [%s] DENIED to %s via grant: %s on: %s=%s", mRightNeeded.getName(),
                        mGranteeMailTarget.getName(), ace.dump(false),
                        ace.getTargetType().getCode(), ace.getTargetName());
            }

            if (mVia != null) {
                mVia.setImpl(new ViaGrantImpl(ace.getTargetType(),
                                              ace.getTargetName(),
                                              ace.getGranteeType(),
                                              ace.getGranteeDisplayName(),
                                              ace.getRight(),
                                              ace.deny()));
            }

            return Boolean.FALSE;
        } else {
            if (sLog.isDebugEnabled()) {
                sLog.debug("Right [%s] ALLOWED to %s via grant: %s on: %s=%s", mRightNeeded.getName(),
                        mGranteeMailTarget.getName(), ace.dump(false),
                        ace.getTargetType().getCode(), ace.getTargetName());
            }

            if (mVia != null) {
                mVia.setImpl(new ViaGrantImpl(ace.getTargetType(),
                                              ace.getTargetName(),
                                              ace.getGranteeType(),
                                              ace.getGranteeDisplayName(),
                                              ace.getRight(),
                                              ace.deny()));
            }

            return Boolean.TRUE;
        }
    }
}
