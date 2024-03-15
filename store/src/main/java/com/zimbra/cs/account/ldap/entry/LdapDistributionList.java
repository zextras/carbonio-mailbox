// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import java.util.Set;

import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * @author pshao
 */
public class LdapDistributionList extends DistributionList implements LdapEntry {
    private String mDn;
    private boolean mIsBasic; // contains only basic attrs in Ldap

    public LdapDistributionList(String dn, String email, ZAttributes attrs,
            boolean isBasic, Provisioning prov) throws LdapException {
        super(email, attrs.getAttrString(Provisioning.A_zimbraId),
                attrs.getAttrs(), prov);
        mDn = dn;
        mIsBasic = isBasic;
    }

    public String getDN() {
        return mDn;
    }

    @Override
    public String[] getAllMembers() throws ServiceException {
        // need to re-get the DistributionList in full if this object was
        // created from getDLBasic, which does not bring in members
        if (mIsBasic) {
            DistributionList dl = getProvisioning().get(DistributionListBy.id, getId());
            return dl.getMultiAttr(MEMBER_ATTR);
        } else {
            return super.getAllMembers();
        }
    }

    @Override
    public Set<String> getAllMembersSet() throws ServiceException {
        // need to re-get the DistributionList if this object was
        // created from getDLBasic, which does not bring in members
        if (mIsBasic) {
            DistributionList dl = getProvisioning().get(DistributionListBy.id, getId());
            return dl.getMultiAttrSet(MEMBER_ATTR);
        } else {
            return super.getAllMembersSet();
        }
    }

}
