// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ConstraintAttr {

    /**
     * @zm-api-field-tag constraint-name
     * @zm-api-field-description Constraint name
     */
    @XmlAttribute(name=AdminConstants.A_NAME, required=true)
    private final String name;

    /**
     * @zm-api-field-description Constraint information
     */
    @XmlElement(name=AdminConstants.E_CONSTRAINT, required=true)
    private final ConstraintInfo constraint;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ConstraintAttr() {
        this(null, null);
    }

    public ConstraintAttr(String name, ConstraintInfo constraint) {
        this.name = name;
        this.constraint = constraint;
    }

    public String getName() { return name; }
    public ConstraintInfo getConstraint() { return constraint; }
}
