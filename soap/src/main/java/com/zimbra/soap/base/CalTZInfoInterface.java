// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.soap.type.TzOnsetInfo;

@XmlAccessorType(XmlAccessType.NONE)
public interface CalTZInfoInterface {
    CalTZInfoInterface createFromIdStdOffsetDayOffset(String id,
        Integer tzStdOffset, Integer tzDayOffset);
    void setStandardTzOnset(TzOnsetInfo standardTzOnset);
    void setDaylightTzOnset(TzOnsetInfo daylightTzOnset);
    void setStandardTZName(String standardTZName);
    void setDaylightTZName(String daylightTZName);
    String getId();
    Integer getTzStdOffset();
    Integer getTzDayOffset();
    TzOnsetInfo getStandardTzOnset();
    TzOnsetInfo getDaylightTzOnset();
    String getStandardTZName();
    String getDaylightTZName();
}
