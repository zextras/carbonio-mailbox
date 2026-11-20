// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;

import com.zimbra.common.mailbox.ACLGrant;
import com.zimbra.common.mailbox.GrantGranteeType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.soap.mail.type.Grant;

public class ZGrant implements ACLGrant, ToZJSONObject {

    private String mArgs;
    private final String mGranteeName;
    private final String mGranteeId;
    private final GranteeType mGranteeType;
    private final String mPermissions;

    public enum GranteeType {
        // Need to keep JAXB class com.zimbra.soap.type.GrantGranteeType in sync with this class
        /**
         * access is granted to an authenticated user
         */
        usr(com.zimbra.soap.type.GrantGranteeType.usr, com.zimbra.common.mailbox.GrantGranteeType.usr),
        /**
         * access is granted to a group of users
         */
        grp(com.zimbra.soap.type.GrantGranteeType.grp, com.zimbra.common.mailbox.GrantGranteeType.grp),
        /**
         * access is granted to users on a cos
         */
        cos(com.zimbra.soap.type.GrantGranteeType.cos, com.zimbra.common.mailbox.GrantGranteeType.cos),
        /**
         * access is granted to public. no authentication needed.
         */
        pub(com.zimbra.soap.type.GrantGranteeType.pub, com.zimbra.common.mailbox.GrantGranteeType.pub),
        /**
         * access is granted to all authenticated users
         */
        all(com.zimbra.soap.type.GrantGranteeType.all, com.zimbra.common.mailbox.GrantGranteeType.all),
        /**
         * access is granted to all users in a domain
         */
        dom(com.zimbra.soap.type.GrantGranteeType.dom, com.zimbra.common.mailbox.GrantGranteeType.dom),
        /**
         * access is granted to a non-Zimbra email address and a password
         */
        guest(com.zimbra.soap.type.GrantGranteeType.guest, com.zimbra.common.mailbox.GrantGranteeType.guest),
        /**
         * access is granted to a non-Zimbra email address and an accesskey
         */
        key(com.zimbra.soap.type.GrantGranteeType.key, com.zimbra.common.mailbox.GrantGranteeType.key);

        private com.zimbra.soap.type.GrantGranteeType jaxbGranteeType;
        private com.zimbra.common.mailbox.GrantGranteeType commonGranteeType;

        GranteeType(com.zimbra.soap.type.GrantGranteeType jaxbGT, com.zimbra.common.mailbox.GrantGranteeType commGT) {
            jaxbGranteeType = jaxbGT;
            commonGranteeType = commGT;
        }

        public static GranteeType fromString(String s) throws ServiceException {
            try {
                return GranteeType.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ZClientException.CLIENT_ERROR(
                        "invalid grantee: " + s + ", valid values: " + Arrays.asList(GranteeType.values()), e);
            }
        }

        /* return equivalent zm-common enum */
        public com.zimbra.common.mailbox.GrantGranteeType toCommon() {
            return commonGranteeType;
        }

        public static GranteeType fromCommon(com.zimbra.common.mailbox.GrantGranteeType commGT) {
            for (GranteeType gt :GranteeType.values()) {
                if (gt.toCommon() == commGT) {
                    return gt;
                }
            }
            throw new IllegalArgumentException("Unrecognised GranteeType:" + commGT);
        }
    }

    public ZGrant(Element e) throws ServiceException {
        mPermissions = e.getAttribute(MailConstants.A_RIGHTS);
        mGranteeName = e.getAttribute(MailConstants.A_DISPLAY, null);
        mGranteeId = e.getAttribute(MailConstants.A_ZIMBRA_ID, null);
        mGranteeType = GranteeType.fromString(e.getAttribute(MailConstants.A_GRANT_TYPE));

        if (mGranteeType == GranteeType.key)
            mArgs = e.getAttribute(MailConstants.A_ACCESSKEY, null);
        else
            mArgs = e.getAttribute(MailConstants.A_PASSWORD, null);
    }

    public ZGrant(Grant grant) throws ServiceException {
        mPermissions = grant.getPerm();
        mGranteeName = grant.getGranteeName();
        mGranteeId = grant.getGranteeId();
        mGranteeType = GranteeType.fromString(grant.getGranteeType().toString());
        if (mGranteeType == GranteeType.key)
            mArgs = grant.getAccessKey();
        else
            mArgs = grant.getGuestPassword();
    }

    public void toElement(Element parent) {
        Element grant = parent.addElement(MailConstants.E_GRANT);
        if (mPermissions != null)
            grant.addAttribute(MailConstants.A_RIGHTS, mPermissions);

        grant.addAttribute(MailConstants.A_GRANT_TYPE, mGranteeType.name());

        if (mGranteeId != null)
            grant.addAttribute(MailConstants.A_ZIMBRA_ID, mGranteeId);

        if (mGranteeName != null)
            grant.addAttribute(MailConstants.A_DISPLAY, mGranteeName);

        if (mArgs != null && mArgs.length() > 0) {
            if (mGranteeType == GranteeType.key)
                grant.addAttribute(MailConstants.A_ACCESSKEY, mArgs);
            else
                grant.addAttribute(MailConstants.A_ARGS, mArgs);
        }
    }

    /**
     *  some combination of (r)ead, (w)rite, (i)nsert, (d)elete, (a)dminister, workflow action (x)
     */
    @Override
    public String getPermissions() {
        return mPermissions;
    }

    /**
     * the type of grantee: "usr", "grp", "dom" (domain),
     * "all" (all authenticated users), "pub" (public authenticated and unauthenticated access),
     * "guest" (non-Zimbra email address and password)
     * "key" (access key)
     */
    public GranteeType getGranteeType() {
        return mGranteeType;
    }

    @Override
    public GrantGranteeType getGrantGranteeType() {
        return (mGranteeType == null) ? null : mGranteeType.commonGranteeType;
    }

    /***
     * the display name (*not* the zimbra id) of the principal being granted rights;
     * optional if {grantee-type} is "all"
     */
    @Override
    public String getGranteeName() {
        return mGranteeName;
    }

    /***
     * the zimbraId of the granteee
     */
    @Override
    public String getGranteeId() {
        return mGranteeId;
    }

    void setAccessKey(String accesskey) {
        mArgs = accesskey;
    }

    @Override
    public ObjectNode toObjectNode() {
        ObjectNode jo = JsonNodeFactory.instance.objectNode();
        jo.put("type", mGranteeType.name());
        if (mGranteeName != null) jo.put("name", mGranteeName);
        if (mGranteeId != null) jo.put("id", mGranteeId);
        if (mPermissions != null) jo.put("permissions", mPermissions);
        if (mArgs != null) jo.put("args", mArgs);
        return jo;
    }

    @Override
    public String toString() {
        return String.format("[ZGrant %s %s %s]", mGranteeType.name(), mGranteeName, mPermissions);
    }

    public String dump() {
        return ToZJSONObject.toString(this);
    }

}
