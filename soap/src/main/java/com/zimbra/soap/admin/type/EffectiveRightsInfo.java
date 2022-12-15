// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.RightWithName;

@XmlAccessorType(XmlAccessType.NONE)
public class EffectiveRightsInfo {
    /**
     * @zm-api-field-description Rights
     */
    @XmlElement(name=AdminConstants.E_RIGHT /* right */, required=false)
    private List <RightWithName> rights = Lists.newArrayList();

    /**
     * @zm-api-field-description All attributes that can be set
     */
    @XmlElement(name=AdminConstants.E_SET_ATTRS /* setAttrs */, required=true)
    private final EffectiveAttrsInfo setAttrs;

    /**
     * @zm-api-field-description All attributes that can be got
     */
    @XmlElement(name=AdminConstants.E_GET_ATTRS /* getAttrs */, required=true)
    private final EffectiveAttrsInfo getAttrs;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private EffectiveRightsInfo() {
        this(null, null, null);
    }

    public EffectiveRightsInfo(EffectiveAttrsInfo setAttrs,
            EffectiveAttrsInfo getAttrs) {
        this(null, setAttrs, getAttrs);
    }

    public EffectiveRightsInfo(Iterable <RightWithName> rights,
            EffectiveAttrsInfo setAttrs, EffectiveAttrsInfo getAttrs) {
        setRights(rights);
        this.setAttrs = setAttrs;
        this.getAttrs = getAttrs;
    }

    public EffectiveRightsInfo setRights(Iterable <RightWithName> rights) {
        this.rights.clear();
        if (rights != null) {
            Iterables.addAll(this.rights,rights);
        }
        return this;
    }

    public EffectiveRightsInfo addRight(RightWithName right) {
        rights.add(right);
        return this;
    }

    public List <RightWithName> getRights() {
        return Collections.unmodifiableList(rights);
    }

    public EffectiveAttrsInfo getSetAttrs() { return setAttrs; }
    public EffectiveAttrsInfo getGetAttrs() { return getAttrs; }
}
