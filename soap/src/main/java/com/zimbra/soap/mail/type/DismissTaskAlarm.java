// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class DismissTaskAlarm extends DismissAlarm {

    public DismissTaskAlarm() {
        this(null, -1L);
    }

    public DismissTaskAlarm(String id, long dismissedAt) {
        super(id, dismissedAt);
    }
}
