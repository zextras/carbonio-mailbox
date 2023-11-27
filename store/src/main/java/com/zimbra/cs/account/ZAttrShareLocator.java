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
public abstract class ZAttrShareLocator extends NamedEntry {

    protected ZAttrShareLocator(String id, Map<String,Object> attrs, Provisioning prov) {
        super(null, id, attrs, null, prov);
    }

    ///// BEGIN-AUTO-GEN-REPLACE

    /**
     * RFC2256: common name(s) for which the entity is known by
     *
     * @return cn, or null if unset
     */
    @ZAttr(id=-1)
    public String getCn() {
        return getAttr(ZAttrProvisioning.A_cn, null, true);
    }

    /**
     * account ID of the owner of the shared folder
     *
     * @return zimbraShareOwnerAccountId, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1375)
    public String getShareOwnerAccountId() {
        return getAttr(ZAttrProvisioning.A_zimbraShareOwnerAccountId, null, true);
    }

    ///// END-AUTO-GEN-REPLACE

}
