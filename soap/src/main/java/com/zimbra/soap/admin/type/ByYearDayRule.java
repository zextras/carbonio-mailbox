// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByYearDayRuleInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class ByYearDayRule implements ByYearDayRuleInterface {

    /**
     * @zm-api-field-tag byyearday-yrdaylist
     * @zm-api-field-description BYYEARDAY yearday list.
     * Format : <b>[[+]|-]num[,...]"</b> where num is between 1 and 366
     * <br />
     * e.g. <b>&lt;byyearday yrdaylist="1,+2,-1"/></b> means January 1st, January 2nd, and December 31st.
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_BYYEARDAY_YRDAYLIST /* yrdaylist */, required=true)
    private final String list;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ByYearDayRule() {
        this((String) null);
    }

    public ByYearDayRule(String list) {
        this.list = list;
    }

    @Override
    public ByYearDayRuleInterface create(String list) {
        return new ByYearDayRule(list);
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
