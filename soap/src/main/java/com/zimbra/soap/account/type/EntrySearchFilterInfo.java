// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.base.EntrySearchFilterInterface;
import com.zimbra.soap.type.SearchFilterCondition;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_SEARCH_FILTER, description="Search Filter specification, only one of the single or multiple condition can be specified at a time")
public class EntrySearchFilterInfo
implements EntrySearchFilterInterface {

    /**
     * @zm-api-field-description Compound condition or simple condition
     */
    @XmlElements({
        @XmlElement(name=AccountConstants.E_ENTRY_SEARCH_FILTER_MULTICOND /* conds */,
            type=EntrySearchFilterMultiCond.class),
        @XmlElement(name=AccountConstants.E_ENTRY_SEARCH_FILTER_SINGLECOND /* cond */,
            type=EntrySearchFilterSingleCond.class)
    })

    @GraphQLIgnore
    private SearchFilterCondition condition;

    public EntrySearchFilterInfo() {
    }

    public EntrySearchFilterInfo(SearchFilterCondition condition) {
        this.setCondition(condition);
    }

    @Override
    @GraphQLIgnore
    public void setCondition(SearchFilterCondition condition) { this.condition = condition; }

    @GraphQLInputField(name = GqlConstants.SINGLE_CONDITION, description="search filter single condition")
    public void setSingleCondition(EntrySearchFilterSingleCond condition) { this.condition = condition; }
    @GraphQLInputField(name = GqlConstants.MULTIPLE_CONDITION, description="search filter multiple condition")
    public void setMultipleCondition(EntrySearchFilterMultiCond condition) { this.condition = condition; }
    @Override
    public SearchFilterCondition getCondition() { return condition; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("condition", condition);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
