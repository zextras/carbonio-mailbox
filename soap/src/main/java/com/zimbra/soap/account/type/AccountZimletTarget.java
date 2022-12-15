// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.ZimletConstants;
import com.zimbra.soap.base.ZimletTarget;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * Implemented as an object rather than using String with @XmlElement because when constructing a JAXB
 * object containing this and other "Strings" there needs to be a way of differentiating them when
 * marshaling to XML.
 *
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=ZimletConstants.ZIMLET_TAG_TARGET)
@GraphQLType(name=GqlConstants.CLASS_ACCOUNT_ZIMLET_TARGET, description="Account zimlet target")
public class AccountZimletTarget
implements ZimletTarget {

    /**
     * @zm-api-field-tag zimlet-target
     * @zm-api-field-description Zimlet target
     */
    @XmlValue
    private String value;

    @SuppressWarnings("unused")
    private AccountZimletTarget() { }

    public AccountZimletTarget(String value) { setValue(value); }

    @Override
    public void setValue(String value) { this.value = value; }
    @Override
    @GraphQLQuery(name=GqlConstants.VALUE, description="Value")
    public String getValue() { return value; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("value", value);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
