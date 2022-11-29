// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.IntervalRuleInterface;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_INTERVAL_RULE, description="Interval rule")
public class IntervalRule
implements IntervalRuleInterface {

    /**
     * @zm-api-field-tag rule-interval
     * @zm-api-field-description Rule interval count - a positive integer
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_INTERVAL_IVAL /* ival */, required=true)
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.VALUE, description="Rule interval count - a positive integer")
    private final int ival;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private IntervalRule() {
        this(-1);
    }

    public IntervalRule(@GraphQLNonNull @GraphQLInputField int ival) {
        this.ival = ival;
    }

    public static IntervalRule create(int ival) {
        return new IntervalRule(ival);
    }

    @Override
    public int getIval() { return ival; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ival", ival)
            .toString();
    }
}
