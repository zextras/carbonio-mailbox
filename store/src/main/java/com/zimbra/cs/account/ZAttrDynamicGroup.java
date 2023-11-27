// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.account.ZAttr;
import com.zimbra.common.account.ZAttrProvisioning;
import java.util.Map;

/**
 * AUTO-GENERATED. DO NOT EDIT.
 *
 */
public abstract class ZAttrDynamicGroup extends Group {

    protected ZAttrDynamicGroup(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, prov);
    }

    ///// BEGIN-AUTO-GEN-REPLACE

    /**
     * RFC2798: preferred name to be used when displaying entries
     *
     * @return displayName, or null if unset
     */
    @ZAttr(id=-1)
    public String getDisplayName() {
        return getAttr(ZAttrProvisioning.A_displayName, null, true);
    }

    /**
     * RFC1274: RFC822 Mailbox
     *
     * @return mail, or null if unset
     */
    @ZAttr(id=-1)
    public String getMail() {
        return getAttr(ZAttrProvisioning.A_mail, null, true);
    }

    /**
     * Identifies an URL associated with each member of a group
     *
     * @return memberURL, or null if unset
     */
    @ZAttr(id=-1)
    public String getMemberURL() {
        return getAttr(ZAttrProvisioning.A_memberURL, null, true);
    }

    /**
     * Zimbra Systems Unique ID
     *
     * @return zimbraId, or null if unset
     */
    @ZAttr(id=1)
    public String getId() {
        return getAttr(ZAttrProvisioning.A_zimbraId, null, true);
    }

    /**
     * if the dynamic group can be a legitimate grantee for folder grantees;
     * and a legitimate grantee or target for delegated admin grants
     *
     * @return zimbraIsACLGroup, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1242)
    public boolean isIsACLGroup() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsACLGroup, false, true);
    }

    /**
     * RFC822 email address of this recipient for accepting mail
     *
     * @return zimbraMailAlias, or empty array if unset
     */
    @ZAttr(id=20)
    public String[] getMailAlias() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraMailAlias, true, true);
    }

    /**
     * address to put in reply-to header
     *
     * @return zimbraPrefReplyToAddress, or null if unset
     */
    @ZAttr(id=60)
    public String getPrefReplyToAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefReplyToAddress, null, true);
    }

    /**
     * personal part of email address put in reply-to header
     *
     * @return zimbraPrefReplyToDisplay, or null if unset
     */
    @ZAttr(id=404)
    public String getPrefReplyToDisplay() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefReplyToDisplay, null, true);
    }

    /**
     * TRUE if we should set a reply-to header
     *
     * @return zimbraPrefReplyToEnabled, or false if unset
     */
    @ZAttr(id=405)
    public boolean isPrefReplyToEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefReplyToEnabled, false, true);
    }

    ///// END-AUTO-GEN-REPLACE

}
