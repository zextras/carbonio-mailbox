// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;

import com.zimbra.soap.base.XPropInterface;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

import com.zimbra.soap.base.XParamInterface;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_XPROP, description="Non-standard property")
public class XProp implements XPropInterface {

    /**
     * @zm-api-field-tag xprop-name
     * @zm-api-field-description XPROP name
     */
    @XmlAttribute(name=MailConstants.A_NAME, required=true)
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.NAME, description="XPROP name")
    private final String name;

    /**
     * @zm-api-field-tag xprop-value
     * @zm-api-field-description XPROP value
     */
    @XmlAttribute(name=MailConstants.A_VALUE, required=true)
    @GraphQLQuery(name=GqlConstants.VALUE, description="XPROP value")
    private final String value;

    /**
     * @zm-api-field-description XPARAMs
     */
    @XmlElement(name=MailConstants.E_CAL_XPARAM, required=false)
    @GraphQLQuery(name=GqlConstants.XPARAMS, description="XPARAMs")
    private List<XParam> xParams = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private XProp() {
        this((String) null, (String) null);
    }

    public XProp(
        @GraphQLNonNull @GraphQLInputField(name=GqlConstants.NAME) String name,
        @GraphQLNonNull @GraphQLInputField(name=GqlConstants.VALUE) String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    @GraphQLIgnore
    public XPropInterface createFromNameAndValue(String name, String value) {
        return new XProp(name, value);
    }

    @GraphQLInputField(name=GqlConstants.XPARAMS, description="XPARAMs") 
    public void setXParams(Iterable <XParam> xParams) {
        this.xParams.clear();
        if (xParams != null) {
            Iterables.addAll(this.xParams,xParams);
        }
    }

    @GraphQLIgnore
    public XProp addXParam(XParam xParam) {
        this.xParams.add(xParam);
        return this;
    }

    @Override
    public String getName() { return name; }
    @Override
    public String getValue() { return value; }

    @GraphQLQuery(name=GqlConstants.XPARAMS, description="XPARAMs")
    public List<XParam> getXParams() {
        return Collections.unmodifiableList(xParams);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("value", value)
            .add("xParams", xParams)
            .toString();
    }

    @Override
    @GraphQLIgnore
    public void setXParamInterfaces(Iterable<XParamInterface> xParams) {
        setXParams(XParam.fromInterfaces(xParams));
    }

    @Override
    @GraphQLIgnore
    public void addXParamInterface(XParamInterface xParam) {
        addXParam((XParam) xParam);
    }

    @Override
    @GraphQLIgnore
    public List<XParamInterface> getXParamInterfaces() {
        return XParam.toInterfaces(xParams);
    }

    public static Iterable <XProp> fromInterfaces(Iterable <XPropInterface> params) {
        if (params == null)
            return null;
        List <XProp> newList = Lists.newArrayList();
        for (XPropInterface param : params) {
            newList.add((XProp) param);
        }
        return newList;
    }

    public static List <XPropInterface> toInterfaces(Iterable <XProp> params) {
        if (params == null)
            return null;
        List <XPropInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }
}
