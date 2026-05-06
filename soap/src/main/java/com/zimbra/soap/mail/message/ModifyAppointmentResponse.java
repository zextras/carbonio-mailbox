// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.soap.mail.type.CreateCalendarItemResponse;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="ModifyAppointmentResponse")
public class ModifyAppointmentResponse
        extends CreateCalendarItemResponse {
}
