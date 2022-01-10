// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public final class SnoozeTaskAlarm extends SnoozeAlarm {

    private SnoozeTaskAlarm() {
        super();
    }

    private SnoozeTaskAlarm(String id, long snoozeUntil) {
        super(id, snoozeUntil);
    }

    public static SnoozeTaskAlarm createForIdAndSnoozeUntil(String id, long snoozeUntil) {
        return new SnoozeTaskAlarm(id, snoozeUntil);
    }
}
