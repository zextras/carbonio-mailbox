// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class AppointmentData extends CalendaringData {
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AppointmentData() {
        this((String) null, (String) null);
    }

    public AppointmentData(String xUid, String uid) {
        super(xUid, uid);
    }
}
