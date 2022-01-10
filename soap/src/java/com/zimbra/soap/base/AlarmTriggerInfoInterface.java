// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface AlarmTriggerInfoInterface {

    public void setAbsoluteInterface(DateAttrInterface absolute);
    public void setRelativeInterface(DurationInfoInterface relative);
    public DateAttrInterface getAbsoluteInterface();
    public DurationInfoInterface getRelativeInterface();
}
