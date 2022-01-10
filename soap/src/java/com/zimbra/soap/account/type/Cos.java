// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.gql.GqlConstants;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * <cos name="cos-name" id="cos-id"/>
 */
@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_COS, description="Cos objec with id and name")
public class Cos {

    /**
     * @zm-api-field-tag cos-id
     * @zm-api-field-description Class of Service (COS) ID
     */
    @XmlAttribute private String id;

    /**
     * @zm-api-field-tag cos-name
     * @zm-api-field-description Class of Service (COS) name
     */
    @XmlAttribute private String name;

    public Cos() {
    }

    @GraphQLQuery(name=GqlConstants.NAME, description="Name of the cos")
    public String getName() { return name; }
    @GraphQLQuery(name=GqlConstants.ID, description="Id of the cos")
    public String getId() { return id; }

    public Cos setName(String name) {
        this.name = name;
        return this;
    }

    public Cos setId(String id) {
        this.id = id;
        return this;
    }
}
