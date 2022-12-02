// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class TZFixupRuleMatchRule {

    /**
     * @zm-api-field-tag match-month
     * @zm-api-field-description Match month.  Value between 1 (January) and 12 (December)
     */
    @XmlAttribute(name=AdminConstants.A_MON /* mon */, required=true)
    private final int month;

    /**
     * @zm-api-field-tag match-week
     * @zm-api-field-description Match week.  -1 means last week of month else between 1 and 4
     */
    @XmlAttribute(name=AdminConstants.A_WEEK /* week */, required=true)
    private final int week;

    /**
     * @zm-api-field-tag match-week-day
     * @zm-api-field-description Match week day.  Value between 1 (Sunday) and 7 (Saturday)
     */
    @XmlAttribute(name=AdminConstants.A_WKDAY /* wkday */, required=true)
    private final int weekDay;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private TZFixupRuleMatchRule() {
        this(-1, -1, -1);
    }

    public TZFixupRuleMatchRule(int month, int week, int weekDay) {
        this.month = month;
        this.week = week;
        this.weekDay = weekDay;
    }

    public int getMonth() { return month; }
    public int getWeek() { return week; }
    public int getWeekDay() { return weekDay; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("month", month)
            .add("week", week)
            .add("weekDay", weekDay);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
