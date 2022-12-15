// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;

/**
 * dump site for all the cross domain special casing, so
 * it's easier to spot them all
 *
 */

public class CrossDomain {
    private static final Log sLog = ZimbraLog.acl;

    static boolean crossDomainOK(Provisioning prov, MailTarget grantee, Domain granteeDomain,
            Domain targetDomain, Group grantedOn) throws ServiceException {
       if (!CrossDomain.checkCrossDomain(prov, granteeDomain, targetDomain, grantedOn)) {
            sLog.info("No cross domain right for %s on domain %s, skipping positive grants on dl %s",
                    grantee.getName(), targetDomain.getName(), grantedOn.getName());
            return false;
        }
        return true;
    }

    static Boolean checkCrossDomainAdminRight(Provisioning prov,
            Domain granteeDomain, Entry target,
            boolean canDelegateNeeded) throws ServiceException {
        if (!(target instanceof Domain))
            throw ServiceException.FAILURE("internal error", null);

        // see if there is a cross domain right on the target domain
        List<ZimbraACE> acl = ACLUtil.getAllACEs(target);
        if (acl == null)
            return Boolean.FALSE;

        for (ZimbraACE ace : acl) {
            /*
             * about the crossDomainAdmin right:
             *   - is a domain right
             *   - not inheritable from the global grant entry,
             *     i.e., can only be granted on domain, not the
             *     global grant.
             *   - cannot be bundled in a combo right
             *   - is the only right for that the grantee has
             *     to be a domain.
             */
            if (ace.getRight() == Admin.R_crossDomainAdmin) {

                if (ace.getGranteeType() == GranteeType.GT_DOMAIN &&
                    ace.getGrantee().equals(granteeDomain.getId())) {
                    if (ace.deny())
                        return Boolean.FALSE;
                    else if (canDelegateNeeded && ace.canExecuteOnly())
                        return false;
                    else
                        return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE; // nope, no crossDomainAdmin
    }

    static boolean checkCrossDomain(Provisioning prov,
            Domain granteeDomain, Domain targetDomain,
            Group grantedOn) throws ServiceException {

        // sanity check, should not happen
        // if we get here, the target can inherit rights from a group,
        // and it must be a domain-ed entry and have a domain
        if (targetDomain == null)
            return true;  // let it through, or throw?

        Domain grantedOnTargetInDomain = grantedOn.getDomain();
        if (grantedOnTargetInDomain == null) {
            // really an error, can't find the domain for the DL
            // return false so ACL granted on this inherited DL target
            // will be ignored
            ZimbraLog.acl.warn("cannot get domain for dl " + grantedOn.getName() +
                    " for checking cross doamin right");
            return false;
        }

        // check if the authed admin is in the same domain of the target.
        // If it is, no issue
        if (targetDomain.getId().equals(granteeDomain.getId()))
            return true;

        // check if this inherited target is in the same domain as the
        // doamin fo the actual target entry.  If it is, no issue.
        if (targetDomain.getId().equals(grantedOnTargetInDomain.getId()))
            return true;

        return checkCrossDomainAdminRight(prov, granteeDomain, targetDomain, false);
    }

    static boolean validateCrossDomainAdminGrant(Right right, GranteeType granteeType)
    throws ServiceException {
        if (right == Admin.R_crossDomainAdmin && granteeType != GranteeType.GT_DOMAIN) {
            throw ServiceException.INVALID_REQUEST("grantee for right " +
                Admin.R_crossDomainAdmin.getName() + " must be a domain.", null);
        }

        if (right != Admin.R_crossDomainAdmin && granteeType == GranteeType.GT_DOMAIN) {
            throw ServiceException.INVALID_REQUEST("grantee for right " +
                    right.getName() + " cannot be a domain.", null);
        }

        return right == Admin.R_crossDomainAdmin;
    }
}
