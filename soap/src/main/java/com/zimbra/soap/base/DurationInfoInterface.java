// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.common.calendar.ParsedDuration;

@XmlAccessorType(XmlAccessType.NONE)
public interface DurationInfoInterface {
    public DurationInfoInterface create(ParsedDuration parsedDuration);
    public void setDurationNegative(Boolean durationNegative);
    public void setWeeks(Integer weeks);
    public void setDays(Integer days);
    public void setHours(Integer hours);
    public void setMinutes(Integer minutes);
    public void setSeconds(Integer seconds);
    public void setRelated(String related);
    public void setRepeatCount(Integer repeatCount);

    public Boolean getDurationNegative();
    public Integer getWeeks();
    public Integer getDays();
    public Integer getHours();
    public Integer getMinutes();
    public Integer getSeconds();
    public String getRelated();
    public Integer getRepeatCount();
}
