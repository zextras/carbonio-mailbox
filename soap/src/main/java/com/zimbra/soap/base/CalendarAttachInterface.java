// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.common.calendar.Attach;

@XmlAccessorType(XmlAccessType.NONE)
public interface CalendarAttachInterface {
    CalendarAttachInterface createFromAttach(Attach att);
    void setUri(String uri);
    void setContentType(String contentType);
    void setBinaryB64Data(String binaryB64Data);
    String getUri();
    String getContentType();
    String getBinaryB64Data();
}
