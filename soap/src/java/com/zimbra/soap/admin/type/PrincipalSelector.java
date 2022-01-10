// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AutoProvPrincipalBy;

@XmlAccessorType(XmlAccessType.NONE)
public class PrincipalSelector {

    /**
     * @zm-api-field-tag principal-selector-by
     * @zm-api-field-description Select the meaning of <b>{principal-selector-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY /* by */, required=true)
    private AutoProvPrincipalBy by;

    /**
     * @zm-api-field-tag principal-selector-key
     * @zm-api-field-description The key used to identify the principal.
     * Meaning determined by <b>{principal-selector-by}</b>
     */
    @XmlValue
    private String key;

    private PrincipalSelector() {
    }

    private PrincipalSelector(AutoProvPrincipalBy by, String key) {
        setBy(by);
        setKey(key);
    }

    public static PrincipalSelector create(AutoProvPrincipalBy by, String key) {
        return new PrincipalSelector(by, key);
    }

    public void setBy(AutoProvPrincipalBy by) { this.by = by; }
    public void setKey(String key) { this.key = key; }
    public AutoProvPrincipalBy getBy() { return by; }
    public String getKey() { return key; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("by", by)
            .add("key", key);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
