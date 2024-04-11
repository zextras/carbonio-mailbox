// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

public interface CreateCalendarItemRecorder {
    void setCalendarItemAttrs(int id, int folderId);
    void setCalendarItemPartStat(String partStat);
}
