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

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.EffectiveAttrInfo;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class EffectiveAttrsInfo {

    /**
     * @zm-api-field-tag all-attrs-accessible
     * @zm-api-field-description Flags whether all attributes on the target entry are accessible.
     * <br />
     * if set, no <b>&lt;a></b> elements will appear under the <b>&lt;setAttrs>/&lt;getAttrs></b>
     */
    @XmlAttribute(name=AdminConstants.A_ALL /* all */, required=false)
    private final ZmBoolean all;

    /**
     * @zm-api-field-description Attributes
     */
    @XmlElement(name=AdminConstants.E_A /* a */, required=false)
    private List <EffectiveAttrInfo> attrs = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private EffectiveAttrsInfo() {
        this((Boolean) null);
    }

    private EffectiveAttrsInfo(Boolean all) {
        this.all = ZmBoolean.fromBool(all);
    }

    public EffectiveAttrsInfo setAttrs(Collection <EffectiveAttrInfo> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            this.attrs.addAll(attrs);
        }
        return this;
    }

    public EffectiveAttrsInfo addAttr(EffectiveAttrInfo attr) {
        attrs.add(attr);
        return this;
    }

    public List <EffectiveAttrInfo> getAttrs() {
        return Collections.unmodifiableList(attrs);
    }

    public Boolean getAll() { return ZmBoolean.toBool(all); }
}
