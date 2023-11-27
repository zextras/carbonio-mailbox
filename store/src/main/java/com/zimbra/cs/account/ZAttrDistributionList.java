// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.account.ZAttr;
import com.zimbra.common.account.ZAttrProvisioning;
import java.util.Map;

/** AUTO-GENERATED. DO NOT EDIT. */
public abstract class ZAttrDistributionList extends Group {

  protected ZAttrDistributionList(
      String name, String id, Map<String, Object> attrs, Provisioning prov) {
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
     * Zimbra Systems Unique ID
     *
     * @return zimbraId, or null if unset
     */
    @ZAttr(id=1)
    public String getId() {
        return getAttr(ZAttrProvisioning.A_zimbraId, null, true);
    }

  /**
     * set to true for admin groups
     *
     * @return zimbraIsAdminGroup, or false if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=802)
    public boolean isIsAdminGroup() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsAdminGroup, false, true);
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
