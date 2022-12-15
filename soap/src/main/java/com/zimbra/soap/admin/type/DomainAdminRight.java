// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.RightWithName;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
public class DomainAdminRight {

    /**
     * @zm-api-field-tag dom-admin-right-name
     * @zm-api-field-description Domain admin right name
     */
    @XmlAttribute(name=AdminConstants.E_NAME, required=true)
    private final String name;
    /**
     * @zm-api-field-tag right-type
     * @zm-api-field-description Right type
     */
    @XmlAttribute(name=AdminConstants.A_TYPE, required=true)
    private final RightInfo.RightType type;

    /**
     * @zm-api-field-tag dom-admin-right-desc
     * @zm-api-field-description Description
     */
    @XmlElement(name=AdminConstants.E_DESC, required=true)
    private final String desc;

    /**
     * @zm-api-field-description Rights
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_RIGHTS, required=true)
    @XmlElement(name=AdminConstants.E_R, required=false)
    private List <RightWithName> rights = Lists.newArrayList();

    public DomainAdminRight() {
        this(null, null, null);
    }

    public DomainAdminRight(String name, RightInfo.RightType type, String desc) {
        this.name = name;
        this.type = type;
        this.desc = desc;
    }

    public DomainAdminRight setRights(Collection <RightWithName> rights) {
        this.rights.clear();
        if (rights != null) {
            this.rights.addAll(rights);
        }
        return this;
    }

    public DomainAdminRight addRight(RightWithName right) {
        rights.add(right);
        return this;
    }

    public List <RightWithName> getRights() {
        return Collections.unmodifiableList(rights);
    }

    public String getName() { return name; }
    public RightInfo.RightType getType() { return type; }
    public String getDesc() { return desc; }
}
