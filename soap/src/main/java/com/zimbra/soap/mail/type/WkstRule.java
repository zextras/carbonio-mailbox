// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.WkstRuleInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class WkstRule
implements WkstRuleInterface {

    /**
     * @zm-api-field-tag weekday
     * @zm-api-field-description Weekday -  <b>SU|MO|TU|WE|TH|FR|SA</b>
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_DAY, required=true)
    private final String day;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private WkstRule() {
        this(null);
    }

    public WkstRule(String day) {
        this.day = day;
    }

    @Override
    public WkstRuleInterface create(String day) {
        return new WkstRule(day);
    }

    @Override
    public String getDay() { return day; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("day", day).toString();
    }
}
