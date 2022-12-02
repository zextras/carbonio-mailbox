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
    public String getFrequency();
    public void setUntilInterface(DateTimeStringAttrInterface until);
    public void setCountInterface(NumAttrInterface count);
    public void setIntervalInterface(IntervalRuleInterface interval);
    public void setBySecondInterface(BySecondRuleInterface bySecond);
    public void setByMinuteInterface(ByMinuteRuleInterface byMinute);
    public void setByHourInterface(ByHourRuleInterface byHour);
    public void setByDayInterface(ByDayRuleInterface byDay);
    public void setByMonthDayInterface(ByMonthDayRuleInterface byMonthDay);
    public void setByYearDayInterface(ByYearDayRuleInterface byYearDay);
    public void setByWeekNoInterface(ByWeekNoRuleInterface byWeekNo);
    public void setByMonthInterface(ByMonthRuleInterface byMonth);
    public void setBySetPosInterface(BySetPosRuleInterface bySetPos);
    public void setWeekStartInterface(WkstRuleInterface weekStart);
    public void setXNameInterfaces(Iterable<XNameRuleInterface> xNames);
    public void addXNameInterface(XNameRuleInterface xName);
    public DateTimeStringAttrInterface getUntilInterface();
    public NumAttrInterface getCountInterface();
    public IntervalRuleInterface getIntervalInterface();
    public BySecondRuleInterface getBySecondInterface();
    public ByMinuteRuleInterface getByMinuteInterface();
    public ByHourRuleInterface getByHourInterface();
    public ByDayRuleInterface getByDayInterface();
    public ByMonthDayRuleInterface getByMonthDayInterface();
    public ByYearDayRuleInterface getByYearDayInterface();
    public ByWeekNoRuleInterface getByWeekNoInterface();
    public ByMonthRuleInterface getByMonthInterface();
    public BySetPosRuleInterface getBySetPosInterface();
    public WkstRuleInterface getWeekStartInterface();
    public List<XNameRuleInterface> getXNamesInterface();
}
