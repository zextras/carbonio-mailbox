// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface DtValInterface {
    public void setStartTimeInterface(DtTimeInfoInterface startTime);
    public void setEndTimeInterface(DtTimeInfoInterface endTime);
    public void setDurationInterface(DurationInfoInterface duration);
    public DtTimeInfoInterface getStartTimeInterface();
    public DtTimeInfoInterface getEndTimeInterface();
    public DurationInfoInterface getDurationInterface();
}
