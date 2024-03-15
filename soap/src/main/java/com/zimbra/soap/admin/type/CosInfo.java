// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class CosInfo implements AdminObjectInterface {

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AdminConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=AdminConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag is-default-cos
     * @zm-api-field-description Flag whether is the default Class Of Service (COS)
     */
    @XmlAttribute(name=AdminConstants.A_IS_DEFAULT_COS /* isDefaultCos */, required=false)
    private final ZmBoolean isDefaultCos;

    /**
     * @zm-api-field-description Attributes
     */
    @XmlElement(name=AdminConstants.E_A /* a */, required=false)
    private final List <CosInfoAttr> attrs;

    /** no-argument constructor wanted by JAXB */
    protected CosInfo() {
        this(null, null, false, null);
    }

    protected CosInfo(String id, String name, boolean isDefaultCos, Collection <CosInfoAttr> attrs) {
        this.name = name;
        this.id = id;
        this.attrs = new ArrayList<CosInfoAttr>();
        if (attrs != null) {
            this.attrs.addAll(attrs);
        }
        this.isDefaultCos = ZmBoolean.fromBool(isDefaultCos);
    }

    public static CosInfo createForIdAndName(String id, String name) {
        return new CosInfo(id, name, false, null);
    }

    public static CosInfo createForIdNameAndAttrs(String id, String name, Collection <CosInfoAttr> attrs) {
        return new CosInfo(id, name, false, attrs);
    }

    public static CosInfo createDefaultCosForIdNameAndAttrs(String id, String name, Collection <CosInfoAttr> attrs) {
        return new CosInfo(id, name, true, attrs);
    }

    public static CosInfo createNonDefaultCosForIdNameAndAttrs(String id, String name, Collection <CosInfoAttr> attrs) {
        return new CosInfo(id, name, false, attrs);
    }

    @Override
    public String getId() { return id; }
    @Override
    public String getName() { return name; }
    public Boolean getIsDefaultCos() { return ZmBoolean.toBool(isDefaultCos); }
    public List<CosInfoAttr> getAttrs() { return Collections.unmodifiableList(attrs); }

    @Override
    public List<Attr> getAttrList() {
        return toAttrsList(attrs);
    }
    public static List <Attr> toAttrsList(Iterable <CosInfoAttr> params) {
        if (params == null)
            return null;
        List <Attr> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return Collections.unmodifiableList(newList);
    }
}
