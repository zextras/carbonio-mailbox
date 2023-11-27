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
public class ZAttrCalendarResource extends Account {

    protected ZAttrCalendarResource(String name, String id, Map<String, Object> attrs, Map<String, Object> defaults, Provisioning prov) {
        super(name, id, attrs, defaults, prov);
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
     * Zimbra Systems Unique ID
     *
     * @return zimbraId, or null if unset
     */
    @ZAttr(id=1)
    public String getId() {
        return getAttr(ZAttrProvisioning.A_zimbraId, null, true);
    }

    ///// END-AUTO-GEN-REPLACE
}
