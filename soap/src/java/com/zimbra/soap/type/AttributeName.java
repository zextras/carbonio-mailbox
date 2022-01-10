// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_ATTRIBUTE_NAME, description="Attribute name")
public class AttributeName {

    /**
     * @zm-api-field-tag attribute-name
     * @zm-api-field-description Attribute name
     */
    @XmlAttribute(name=MailConstants.A_ATTRIBUTE_NAME, required=true)
    @GraphQLQuery(name=GqlConstants.NAME, description="Attribute name")
    private final String name;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    protected AttributeName() {
        this((String) null);
    }

    public AttributeName(@GraphQLNonNull @GraphQLInputField String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
