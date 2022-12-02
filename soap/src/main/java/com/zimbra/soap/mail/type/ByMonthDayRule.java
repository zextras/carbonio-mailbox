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
import com.zimbra.soap.base.ByMonthDayRuleInterface;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_BY_MONTH_DAY_RULE, description="By-month-day rule")
public class ByMonthDayRule implements ByMonthDayRuleInterface {

    /**
     * @zm-api-field-tag modaylist
     * @zm-api-field-description Comma separated list of day numbers from either the start (positive) or the
     * end (negative) of the month - format : <b>[[+]|-]num[,...]</b>   where num between 1 to 31
     * <br />
     * e.g. <b>modaylist="1,+2,-7"</b>
     * <br />
     * means first day of the month, plus the 2nd day of the month, plus the 7th from last day of the month.
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_BYMONTHDAY_MODAYLIST /* modaylist */, required=true)
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.LIST, description="Comma separated list of day numbers from either the start (positive) or the end (negative) of the month - format : <b>[[+]|-]num[,...]</b>   where num between 1 to 31")
    private final String list;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ByMonthDayRule() {
        this((String) null);
    }

    public ByMonthDayRule(@GraphQLNonNull @GraphQLInputField String list) {
        this.list = list;
    }

    @Override
    public ByMonthDayRuleInterface create(String list) {
        return new ByMonthDayRule(list);
    }

    @Override
    public String getList() { return list; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("list", list)
            .toString();
    }
}
