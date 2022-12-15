// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.List;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class RightsAttrs {

    /**
     * @zm-api-field-tag all-flag
     * @zm-api-field-description All flag
     */
    @XmlAttribute(name=AdminConstants.A_ALL, required=false)
    private ZmBoolean all;

    //  Need to support 2 forms:
    //  1.    <a n="attributeName"/>
    //  2.    <attributeName/>
    //  Should all be one or the other.
    /**
     * @zm-api-field-description Attrs
     */
    @XmlElement(name=AdminConstants.E_A, required=false)
    private List <Attr> attrs = Lists.newArrayList();

    /**
     * @zm-api-field-description elements of form <b>&lt;attributeName/></b>
     */
    @XmlAnyElement
    private List <Element> elements = Lists.newArrayList();

    public RightsAttrs () {
    }

    public RightsAttrs (Collection <Attr> attrs) {
        this.setAttrs(attrs);
    }

    public RightsAttrs setAttrs(Collection<Attr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            this.attrs.addAll(attrs);
        }
        return this;
    }

    public RightsAttrs addAttr(Attr attr) {
        attrs.add(attr);
        return this;
    }

    public List <Attr> getAttrs() {
        return Collections.unmodifiableList(attrs);
    }
    public RightsAttrs setElements(Collection <Element> elements) {
        this.elements.clear();
        if (elements != null) {
            this.elements.addAll(elements);
        }
        return this;
    }

    public RightsAttrs addElement(Element element) {
        elements.add(element);
        return this;
    }

    public List <Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public void setAll(Boolean all) { this.all = ZmBoolean.fromBool(all); }

    public Boolean getAll() { return ZmBoolean.toBool(all); }
}
