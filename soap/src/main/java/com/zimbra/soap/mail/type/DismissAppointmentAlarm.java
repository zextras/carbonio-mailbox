// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class DismissAppointmentAlarm extends DismissAlarm {

    public DismissAppointmentAlarm() {
        this((String) null, -1L);
    }

    public DismissAppointmentAlarm(String id, long dismissedAt) {
        super(id, dismissedAt);
    }
}
