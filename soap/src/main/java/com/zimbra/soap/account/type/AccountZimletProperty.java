// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.ZimletConstants;
import com.zimbra.soap.base.ZimletProperty;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_ACCOUNT_ZIMLET_PROPERTY, description="Account zimlet property")
public class AccountZimletProperty
implements ZimletProperty {

    /**
     * @zm-api-field-description zimlet-property-name
     * @zm-api-field-description Property name
     */
    @XmlAttribute(name=ZimletConstants.ZIMLET_ATTR_NAME /* name */, required=false)
    private String name;

    /**
     * @zm-api-field-description zimlet-property-value
     * @zm-api-field-description Property value
     */
    @XmlValue
    private String value;

    private AccountZimletProperty() {
    }

    private AccountZimletProperty(String name, String value) {
        setName(name);
        setValue(value);
    }

    public static AccountZimletProperty createForNameAndValue(String name, String value) {
        return new AccountZimletProperty(name, value);
    }

    @Override
    public void setName(String name) { this.name = name; }
    @Override
    public void setValue(String value) { this.value = value; }
    @GraphQLQuery(name=GqlConstants.NAME, description="Name")
    @Override
    public String getName() { return name; }
    @GraphQLQuery(name=GqlConstants.VALUE, description="Value")
    @Override
    public String getValue() { return value; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("value", value);
    }

    public static Iterable <AccountZimletProperty> fromInterfaces(Iterable <ZimletProperty> ifs) {
        if (ifs == null)
            return null;
        List <AccountZimletProperty> newList = Lists.newArrayList();
        for (ZimletProperty listEnt : ifs) {
            newList.add((AccountZimletProperty) listEnt);
        }
        return newList;
    }

    public static List <ZimletProperty> toInterfaces(Iterable <AccountZimletProperty> params) {
        if (params == null)
            return null;
        List <ZimletProperty> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
