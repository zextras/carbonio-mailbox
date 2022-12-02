// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_NAME_ID, description="A name or id")
public class NameId {

    // Some deletions etc can specify by name or id.  Hence neither
    // are required.
    /**
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=AccountConstants.A_NAME, required=false)
    @GraphQLInputField(name=GqlConstants.NAME, description="Name")
    private final String name;

    /**
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AccountConstants.A_ID, required=false)
    @GraphQLInputField(name=GqlConstants.ID, description="ID")
    private final String id;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private NameId() {
        this((String) null, (String) null);
    }

    public NameId(@GraphQLInputField(name=GqlConstants.NAME, description="Name") String name,
        @GraphQLInputField(name=GqlConstants.ID, description="ID") String id) {
        this.name = name;
        this.id = id;
    }

    @GraphQLQuery(name=GqlConstants.NAME, description="Name")
    public String getName() { return name; }
    @GraphQLQuery(name=GqlConstants.ID, description="ID")
    public String getId() { return id; }
}
