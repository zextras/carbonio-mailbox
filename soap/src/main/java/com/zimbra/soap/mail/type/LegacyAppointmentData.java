// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class LegacyAppointmentData extends LegacyCalendaringData {
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private LegacyAppointmentData() {
        this((String) null, (String) null);
    }

    public LegacyAppointmentData(String xUid, String uid) {
        super(xUid, uid);
    }
}
