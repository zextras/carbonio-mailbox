// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class TaskData extends CalendaringData {
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private TaskData() {
        this((String) null, (String) null);
    }

    public TaskData(String xUid, String uid) {
        super(xUid, uid);
    }
}
