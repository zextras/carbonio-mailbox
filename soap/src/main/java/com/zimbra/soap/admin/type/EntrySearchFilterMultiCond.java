// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.SearchFilterCondition;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class EntrySearchFilterMultiCond implements SearchFilterCondition {

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
    private List <SearchFilterCondition> conditions = Lists.newArrayList();

    public EntrySearchFilterMultiCond() {
    }

    public EntrySearchFilterMultiCond setConditions(Collection<SearchFilterCondition> conditions) {
        this.conditions.clear();
        if (conditions != null) {
            this.conditions.addAll(conditions);
        }
        return this;
    }

    public EntrySearchFilterMultiCond addCondition(SearchFilterCondition condition) {
        conditions.add(condition);
        return this;
    }


    @Override
    public void setNot(Boolean not) { this.not = ZmBoolean.fromBool(not); }
    public void setOr(Boolean or) { this.or = ZmBoolean.fromBool(or); }

    @Override
    public Boolean isNot() { return ZmBoolean.toBool(not); }
    public Boolean isOr() { return ZmBoolean.toBool(or); }

    public List<SearchFilterCondition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }
}
