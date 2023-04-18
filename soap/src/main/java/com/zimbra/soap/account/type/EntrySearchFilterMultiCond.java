// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.SearchFilterCondition;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class EntrySearchFilterMultiCond
implements SearchFilterCondition {

    /**
     * @zm-api-field-tag not
     * @zm-api-field-description Negation flag
     * <br />
     * If set to <b>1 (true)</b> then negate the compound condition
     */
    @XmlAttribute(name=AccountConstants.A_ENTRY_SEARCH_FILTER_NEGATION /* not */, required=false)
    private ZmBoolean not;

    /**
     * @zm-api-field-tag or
     * @zm-api-field-description OR flag
     * <table>
     * <tr> <td> <b>1 (true)</b> </td> <td> child conditions are OR'ed together </td> </tr>
     * <tr> <td> <b>0 (false) [default]</b> </td> <td> child conditions are AND'ed together </td> </tr>
     * </table>
     */
    @XmlAttribute(name=AccountConstants.A_ENTRY_SEARCH_FILTER_OR /* or */, required=false)
    private ZmBoolean or;

    /**
     * @zm-api-field-description Compound condition or simple condition
     */
    @XmlElements({
        @XmlElement(name=AccountConstants.E_ENTRY_SEARCH_FILTER_MULTICOND /* conds */,
            type=EntrySearchFilterMultiCond.class),
        @XmlElement(name=AccountConstants.E_ENTRY_SEARCH_FILTER_SINGLECOND /* cond */,
            type=EntrySearchFilterSingleCond.class)
    })

    private List<SearchFilterCondition> conditions = Lists.newArrayList();

    public EntrySearchFilterMultiCond() {
    }

    @Override
    public void setNot(Boolean not) { this.not = ZmBoolean.fromBool(not); }
    public void setOr(Boolean or) { this.or = ZmBoolean.fromBool(or); }

    public void setConditions(Iterable <SearchFilterCondition> conditions) {
        this.conditions.clear();
        if (conditions != null) {
            Iterables.addAll(this.conditions,conditions);
        }
    }

    public void setSingleConditions(List<EntrySearchFilterSingleCond> conditions) {
        if (conditions != null) {
            Iterables.addAll(this.conditions, conditions);
        }
    }

    public void setMultipleConditions(List<EntrySearchFilterMultiCond> conditions) {
        if (conditions != null) {
            Iterables.addAll(this.conditions, conditions);
        }
    }

    public void addCondition(SearchFilterCondition condition) {
        this.conditions.add(condition);
    }

    @Override
    public Boolean isNot() { return ZmBoolean.toBool(not); }
    public Boolean isOr() { return ZmBoolean.toBool(or); }
    public List<SearchFilterCondition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("not", not)
            .add("or", or)
            .add("conditions", conditions);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
