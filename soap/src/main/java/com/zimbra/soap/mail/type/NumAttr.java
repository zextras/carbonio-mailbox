// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.NumAttrInterface;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_NUMBER_ATTRIBUTE, description="Number attribute")
public class NumAttr
implements NumAttrInterface {

    /**
     * @zm-api-field-tag num
     * @zm-api-field-description Number
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_COUNT_NUM /* num */, required=true)
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.NUM, description="Number")
    private final int num;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private NumAttr() {
        this(-1);
    }

    public NumAttr(@GraphQLNonNull @GraphQLInputField int num) {
        this.num = num;
    }

    @Override
    public NumAttrInterface create(int num) {
        return new NumAttr(num);
    }

    @Override
    public int getNum() { return num; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("num", num)
            .toString();
    }
}
