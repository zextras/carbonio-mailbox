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
public class TZFixupRuleMatchDate {

    /**
     * @zm-api-field-taga match-month
     * @zm-api-field-descriptiona Match month.  Value between 1 (January) and 12 (December)
     */
    @XmlAttribute(name=AdminConstants.A_MON /* mon */, required=true)
    private final int month;

    /**
     * @zm-api-field-taga match-month-day
     * @zm-api-field-descriptiona Match month day.  Value between 1 and 31
     */
    @XmlAttribute(name=AdminConstants.A_MDAY /* mday */, required=true)
    private final int monthDay;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private TZFixupRuleMatchDate() {
        this(-1, -1);
    }

    public TZFixupRuleMatchDate(int month, int monthDay) {
        this.month = month;
        this.monthDay = monthDay;
    }

    public int getMonth() { return month; }
    public int getMonthDay() { return monthDay; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("month", month)
            .add("monthDay", monthDay);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
