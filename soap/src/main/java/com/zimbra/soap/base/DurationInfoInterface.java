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
    DurationInfoInterface create(ParsedDuration parsedDuration);
    void setDurationNegative(Boolean durationNegative);
    void setWeeks(Integer weeks);
    void setDays(Integer days);
    void setHours(Integer hours);
    void setMinutes(Integer minutes);
    void setSeconds(Integer seconds);
    void setRelated(String related);
    void setRepeatCount(Integer repeatCount);

    Boolean getDurationNegative();
    Integer getWeeks();
    Integer getDays();
    Integer getHours();
    Integer getMinutes();
    Integer getSeconds();
    String getRelated();
    Integer getRepeatCount();
}
