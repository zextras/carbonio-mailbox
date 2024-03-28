// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface SimpleRepeatingRuleInterface {
    String getFrequency();
    void setUntilInterface(DateTimeStringAttrInterface until);
    void setCountInterface(NumAttrInterface count);
    void setIntervalInterface(IntervalRuleInterface interval);
    void setBySecondInterface(BySecondRuleInterface bySecond);
    void setByMinuteInterface(ByMinuteRuleInterface byMinute);
    void setByHourInterface(ByHourRuleInterface byHour);
    void setByDayInterface(ByDayRuleInterface byDay);
    void setByMonthDayInterface(ByMonthDayRuleInterface byMonthDay);
    void setByYearDayInterface(ByYearDayRuleInterface byYearDay);
    void setByWeekNoInterface(ByWeekNoRuleInterface byWeekNo);
    void setByMonthInterface(ByMonthRuleInterface byMonth);
    void setBySetPosInterface(BySetPosRuleInterface bySetPos);
    void setWeekStartInterface(WkstRuleInterface weekStart);
    void setXNameInterfaces(Iterable<XNameRuleInterface> xNames);
    void addXNameInterface(XNameRuleInterface xName);
    DateTimeStringAttrInterface getUntilInterface();
    NumAttrInterface getCountInterface();
    IntervalRuleInterface getIntervalInterface();
    BySecondRuleInterface getBySecondInterface();
    ByMinuteRuleInterface getByMinuteInterface();
    ByHourRuleInterface getByHourInterface();
    ByDayRuleInterface getByDayInterface();
    ByMonthDayRuleInterface getByMonthDayInterface();
    ByYearDayRuleInterface getByYearDayInterface();
    ByWeekNoRuleInterface getByWeekNoInterface();
    ByMonthRuleInterface getByMonthInterface();
    BySetPosRuleInterface getBySetPosInterface();
    WkstRuleInterface getWeekStartInterface();
    List<XNameRuleInterface> getXNamesInterface();
}
