// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.collect.Lists;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
abstract public class AttributeSelectorImpl implements AttributeSelector {

    private static Joiner COMMA_JOINER = Joiner.on(",");
    private List<String> attrs = Lists.newArrayList();

    public AttributeSelectorImpl() {
    }

    public AttributeSelectorImpl(String attrs) {
        setAttrs(attrs);
    }

    public AttributeSelectorImpl(String ... attrNames) {
        addAttrs(attrNames);
    }

    public AttributeSelectorImpl(Iterable<String> attrs) {
        addAttrs(attrs);
    }

    @Override
    public AttributeSelector setAttrs(String attrs) {
        this.attrs.clear();
        if (attrs != null) {
            addAttrs(attrs.split(","));
        }
        return this;
    }

    @Override
    public AttributeSelector addAttrs(String attr) {
        if (attr != null)
            attrs.add(attr);
        return this;
    }

    @Override
    public AttributeSelector addAttrs(String ... attrNames) {
        for (String attrName : attrNames) {
            addAttrs(attrName);
        }
        return this;
    }

    @Override
    public AttributeSelector addAttrs(Iterable<String> attrs) {
        if (attrs != null) {
            for (String attr : attrs) {
                addAttrs(attr);
            }
        }
        return this;
    }

    /**
     * @zm-api-field-tag request-attrs
     * @zm-api-field-description Comma separated list of attributes
     */
    @Override
    @XmlAttribute(name=AdminConstants.A_ATTRS, required=false)
    public String getAttrs() {
        if (attrs.size() == 0)
            return null;
        return COMMA_JOINER.join(attrs);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
            MoreObjects.ToStringHelper helper) {
    return helper
        .add("attrs", getAttrs());
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
