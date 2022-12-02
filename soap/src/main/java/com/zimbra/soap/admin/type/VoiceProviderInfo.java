// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.UCServiceAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceProviderInfo {

    /**
     * @zm-api-field-description UC Service attributes
     */
    @XmlElementWrapper(name=AdminConstants.E_ATTRS /* attrs */, required=true)
    @XmlElement(name=AdminConstants.E_A /* a */, required=false)
    private List<UCServiceAttribute> attrs = Lists.newArrayList();

    public VoiceProviderInfo() {
    }

    public void setAttrs(Iterable <UCServiceAttribute> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs,attrs);
        }
    }

    public void addAttr(UCServiceAttribute attr) {
        this.attrs.add(attr);
    }

    public List<UCServiceAttribute> getAttrs() {
        return attrs;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("attrs", attrs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
