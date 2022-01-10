// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByDayRuleInterface;
import com.zimbra.soap.base.WkDayInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class ByDayRule implements ByDayRuleInterface {

    /**
     * @zm-api-field-description By day weekday rule specification
     */
    @XmlElement(name=MailConstants.E_CAL_RULE_BYDAY_WKDAY /* wkday */, required=false)
    private List<WkDay> days = Lists.newArrayList();

    public ByDayRule() {
    }

    public void setDays(Iterable <WkDay> days) {
        this.days.clear();
        if (days != null) {
            Iterables.addAll(this.days,days);
        }
    }

    public ByDayRule addDay(WkDay day) {
        this.days.add(day);
        return this;
    }

    public List<WkDay> getDays() {
        return Collections.unmodifiableList(days);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("days", days)
            .toString();
    }

    @Override
    public void setDayInterfaces(Iterable<WkDayInterface> days) {
        setDays(WkDay.fromInterfaces(days));
    }

    @Override
    public void addDayInterface(WkDayInterface day) {
        addDay((WkDay) day);
    }

    @Override
    public List<WkDayInterface> getDayInterfaces() {
        return WkDay.toInterfaces(days);
    }
}
