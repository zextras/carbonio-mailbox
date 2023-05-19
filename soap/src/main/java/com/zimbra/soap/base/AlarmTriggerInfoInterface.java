// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface AlarmTriggerInfoInterface {

    void setAbsoluteInterface(DateAttrInterface absolute);
    void setRelativeInterface(DurationInfoInterface relative);
    DateAttrInterface getAbsoluteInterface();
    DurationInfoInterface getRelativeInterface();
}
