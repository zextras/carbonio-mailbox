// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface DtValInterface {
    void setStartTimeInterface(DtTimeInfoInterface startTime);
    void setEndTimeInterface(DtTimeInfoInterface endTime);
    void setDurationInterface(DurationInfoInterface duration);
    DtTimeInfoInterface getStartTimeInterface();
    DtTimeInfoInterface getEndTimeInterface();
    DurationInfoInterface getDurationInterface();
}
