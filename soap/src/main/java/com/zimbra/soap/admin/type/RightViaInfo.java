// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class RightViaInfo {

    /**
     * @zm-api-field-description Target
     */
    @XmlElement(name=AdminConstants.E_TARGET /* target */, required=true)
    private final TargetWithType target;

    /**
     * @zm-api-field-description Grantee
     */
    @XmlElement(name=AdminConstants.E_GRANTEE /* grantee */, required=true)
    private final GranteeWithType grantee;

    /**
     * @zm-api-field-description Checked right
     */
    @XmlElement(name=AdminConstants.E_RIGHT /* right */, required=true)
    private final CheckedRight right;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RightViaInfo() {
        this(null, null,
            null);
    }

    public RightViaInfo(TargetWithType target, GranteeWithType grantee,
                CheckedRight right) {
        this.target = target;
        this.grantee = grantee;
        this.right = right;
    }

    public TargetWithType getTarget() { return target; }
    public GranteeWithType getGrantee() { return grantee; }
    public CheckedRight getRight() { return right; }
}
