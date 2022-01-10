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
import com.zimbra.soap.base.ByMonthRuleInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class ByMonthRule implements ByMonthRuleInterface {

    /**
     * @zm-api-field-tag month-list
     * @zm-api-field-description Comma separated list of months where month is a number between 1 and 12
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_BYMONTH_MOLIST /* molist */, required=true)
    private final String list;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ByMonthRule() {
        this((String) null);
    }

    public ByMonthRule(String list) {
        this.list = list;
    }

    @Override
    public ByMonthRuleInterface create(String list) {
        return new ByMonthRule(list);
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
