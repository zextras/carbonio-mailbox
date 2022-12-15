// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class DismissTaskAlarm extends DismissAlarm {

    public DismissTaskAlarm() {
        this((String) null, -1L);
    }

    public DismissTaskAlarm(String id, long dismissedAt) {
        super(id, dismissedAt);
    }
}
