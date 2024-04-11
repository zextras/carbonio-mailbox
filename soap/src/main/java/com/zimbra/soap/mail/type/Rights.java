// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class Rights {

    /**
     * @zm-api-field-tag effective-permissions
     * @zm-api-field-description The effective permissions of the specified folder
     */
    @XmlAttribute(name=MailConstants.A_RIGHTS /* perm */, required=true)
    private final String effectivePermissions;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private Rights() {
        this(null);
    }

    public Rights(String effectivePermissions) {
        this.effectivePermissions = effectivePermissions;
    }

    public String getEffectivePermissions() { return effectivePermissions; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("effectivePermissions", effectivePermissions);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
