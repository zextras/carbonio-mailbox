// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ZimbraACE.ExternalGroupInfo;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;

public enum GranteeType {

    // Need to keep JAXB class com.zimbra.soap.type.GranteeType in sync with this class
    GT_USER("usr",      com.zimbra.soap.type.GranteeType.usr, (short)(GranteeFlag.F_ADMIN | GranteeFlag.F_INDIVIDUAL | GranteeFlag.F_IS_ZIMBRA_ENTRY)),
    GT_GROUP("grp",     com.zimbra.soap.type.GranteeType.grp, (short)(GranteeFlag.F_ADMIN | GranteeFlag.F_GROUP      | GranteeFlag.F_IS_ZIMBRA_ENTRY)),
    GT_EXT_GROUP("egp", com.zimbra.soap.type.GranteeType.egp, (short)(GranteeFlag.F_ADMIN | GranteeFlag.F_GROUP)),
    GT_AUTHUSER("all",  com.zimbra.soap.type.GranteeType.all, (                      GranteeFlag.F_AUTHUSER)), // all authenticated users
    GT_DOMAIN("dom",    com.zimbra.soap.type.GranteeType.dom, (short)(GranteeFlag.F_ADMIN | GranteeFlag.F_DOMAIN     | GranteeFlag.F_IS_ZIMBRA_ENTRY)),  // only for the admin crossDomainAdmin right and user rights
    // "edom" - Used for grantee type in conjunction with sendToDistList for non-Zimbra domains
    GT_EXT_DOMAIN("edom",com.zimbra.soap.type.GranteeType.edom,(                            GranteeFlag.F_INDIVIDUAL)),
    GT_GUEST("gst",     com.zimbra.soap.type.GranteeType.gst, (short)(                      GranteeFlag.F_INDIVIDUAL                                  | GranteeFlag.F_HAS_SECRET)),
    GT_KEY("key",       com.zimbra.soap.type.GranteeType.key, (short)(                      GranteeFlag.F_INDIVIDUAL                                  | GranteeFlag.F_HAS_SECRET)),
    GT_PUBLIC("pub",    com.zimbra.soap.type.GranteeType.pub, (                      GranteeFlag.F_PUBLIC)),

    /*
     * pseudo grantee type that can be specified in granting requests.
     *
     * The granting code will try to figure out the actual grantee type in the following order:
     * - see if it is an user on the system, if yes, the actual grantee type will be usr
     * - if not, see if it is a group on the system, if yes, the actual grantee type will be grp
     * - if not, see if it is an external group, if yes, the actual grantee type will be egp
     * - if not, treat it as a gst
     *
     * This grantee type CAN be persisted in ACL (that used not to be possible).
     */
    GT_EMAIL("email", com.zimbra.soap.type.GranteeType.email, GranteeFlag.F_INDIVIDUAL);


    private static class GT {
        static Map<String, GranteeType> sCodeMap = new HashMap<String, GranteeType>();
    }

    private String mCode;
    private short mFlags;
    private com.zimbra.soap.type.GranteeType jaxbGranteeType;

    GranteeType(String code, com.zimbra.soap.type.GranteeType jaxbGT, short flags) {
        mCode = code;
        GT.sCodeMap.put(code, this);
        mFlags = flags;
        jaxbGranteeType = jaxbGT;
    }

    public static GranteeType fromCode(String granteeType) throws ServiceException {
        GranteeType gt = GT.sCodeMap.get(granteeType);
        if (gt == null) {
            throw ServiceException.PARSE_ERROR("invalid grantee type: " + granteeType, null);
        }
        return gt;
    }

    /* return equivalent JAXB enum */
    public com.zimbra.soap.type.GranteeType toJaxb() {
        return jaxbGranteeType;
    }

    public static GranteeType fromJaxb(com.zimbra.soap.type.GranteeType jaxbGT) {
        for (GranteeType gt :GranteeType.values()) {
            if (gt.toJaxb() == jaxbGT) {
                return gt;
            }
        }
        throw new IllegalArgumentException("Unrecognised GranteeType:" + jaxbGT);
    }


    /**
     * - code stored in the ACE.
     * - code appear in XML
     * - code displayed by CLI
     *
     * @return
     */
    public String getCode() {
        return mCode;
    }

    /*
     * whether the grantee type is allowed for amin rights
     */
    public boolean allowedForAdminRights() {
        return hasFlags(GranteeFlag.F_ADMIN);
    }

    /*
     * whether this grantee type can take a secret
     */
    public boolean allowSecret() {
        return hasFlags(GranteeFlag.F_HAS_SECRET);
    }

    public boolean isZimbraEntry() {
        return hasFlags(GranteeFlag.F_IS_ZIMBRA_ENTRY);
    }

    public boolean needsGranteeIdentity() {
        return !(GT_AUTHUSER == this || GT_PUBLIC == this);
    }

    public boolean hasFlags(short flags) {
        return ((flags & mFlags) == flags);
    }

    /**
     * central place where a grantee should be loaded
     *
     * @param prov
     * @param granteeType
     * @param granteeBy
     * @param grantee
     * @return
     * @throws ServiceException
     */
    public static NamedEntry lookupGrantee(Provisioning prov, GranteeType granteeType,
            GranteeBy granteeBy, String grantee, boolean mustFind)
    throws ServiceException {
        NamedEntry granteeEntry = null;

        switch (granteeType) {
        case GT_USER:
            granteeEntry = prov.get(AccountBy.fromString(granteeBy.name()), grantee);
            if (mustFind && granteeEntry == null) {
                throw AccountServiceException.NO_SUCH_ACCOUNT(grantee);
            }
            break;
        case GT_GROUP:
            granteeEntry = prov.getGroupBasic(Key.DistributionListBy.fromString(granteeBy.name()), grantee);
            if (mustFind && granteeEntry == null) {
                throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(grantee);
            }
            break;
        case GT_DOMAIN:
            granteeEntry = prov.get(Key.DomainBy.fromString(granteeBy.name()), grantee);
            if (mustFind && granteeEntry == null) {
                throw AccountServiceException.NO_SUCH_DOMAIN(grantee);
            }
            break;
        case GT_GUEST:
            granteeEntry = new GuestAccount(grantee, null);
            break;
        case GT_EMAIL:
            // see if it is an internal account
            granteeEntry = prov.get(AccountBy.fromString(granteeBy.name()), grantee);
            if (granteeEntry == null) {
                // see if it is an internal group
                granteeEntry = prov.getGroupBasic(Key.DistributionListBy.fromString(granteeBy.name()), grantee);
            }
            if (granteeEntry == null) {
                // see if it is an external group
                try {
                    ExternalGroup.get(DomainBy.name, grantee, false /* asAdmin */);
                } catch (ServiceException e) {
                    // ignore, external group as grantee is probably not configured on the domain
                }
            }

            if (granteeEntry == null) {
                // still not found, must be a guest
                granteeEntry = new GuestAccount(grantee, null);
            }
            break;
        default:
            throw ServiceException.INVALID_REQUEST("invalid grantee type for lookupGrantee:" +
                    granteeType.getCode(), null);
        }

        return granteeEntry;
    }

    public static NamedEntry lookupGrantee(Provisioning prov, GranteeType granteeType,
            GranteeBy granteeBy, String grantee)
    throws ServiceException {
        return lookupGrantee(prov, granteeType, granteeBy,  grantee, true);
    }

    public static NamedEntry lookupGrantee(Provisioning prov, GranteeSelector selector, boolean mustFind)
    throws ServiceException {
        return lookupGrantee(prov, GranteeType.fromJaxb(selector.getType()), selector.getBy(),  selector.getKey(),
                mustFind);
    }

    public static NamedEntry lookupGrantee(Provisioning prov, GranteeSelector selector)
    throws ServiceException {
        return lookupGrantee(prov, selector, true);
    }

    public static GranteeType determineGranteeType(GranteeType granteeType,
            GranteeBy granteeBy, String grantee, String domainName)
    throws ServiceException {

        if (granteeType != GranteeType.GT_EMAIL) {
            return granteeType;
        }

        // grantee type is an email address, figure out the real grantee type
        Provisioning prov = Provisioning.getInstance();

        // see if it is an internal account
        if (GranteeType.lookupGrantee(prov, GranteeType.GT_USER, granteeBy, grantee, false) != null) {
            return GranteeType.GT_USER;
        }

        // see if it is an internal group
        if (GranteeType.lookupGrantee(prov, GranteeType.GT_GROUP, granteeBy, grantee, false) != null) {
            return GranteeType.GT_GROUP;
        }

        // see if it is an external group
        try {
            boolean asAdmin = false; // TODO, asAdmin is not used for now
            if (ExternalGroup.get(DomainBy.name,
                    ExternalGroupInfo.encodeIfExtGroupNameMissingDomain(domainName, grantee), asAdmin) != null) {
                return GranteeType.GT_EXT_GROUP;
            }
        } catch (ServiceException e) {
            // ignore, external group as grantee is probably not configured on the domain
        }

        // still not found, must be a guest
        return GranteeType.GT_GUEST;
    }

}
