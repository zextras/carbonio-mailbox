// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.soap.type.TzOnsetInfo;

@XmlAccessorType(XmlAccessType.NONE)
public interface CalTZInfoInterface {
    public CalTZInfoInterface createFromIdStdOffsetDayOffset(String id,
            Integer tzStdOffset, Integer tzDayOffset);
    public void setStandardTzOnset(TzOnsetInfo standardTzOnset);
    public void setDaylightTzOnset(TzOnsetInfo daylightTzOnset);
    public void setStandardTZName(String standardTZName);
    public void setDaylightTZName(String daylightTZName);
    public String getId();
    public Integer getTzStdOffset();
    public Integer getTzDayOffset();
    public TzOnsetInfo getStandardTzOnset();
    public TzOnsetInfo getDaylightTzOnset();
    public String getStandardTZName();
    public String getDaylightTZName();
}
