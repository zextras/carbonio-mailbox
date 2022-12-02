// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.util;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class DateUtil {
    private static final String[] MONTH_NAME = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
        "Oct", "Nov", "Dec"
    };

    public static String toImapDateTime(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        int tzoffset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000;
        char tzsign = tzoffset > 0 ? '+' : '-';
        tzoffset = Math.abs(tzoffset);

        return String.format("%02d-%s-%d %02d:%02d:%02d %c%02d%02d",
            cal.get(Calendar.DAY_OF_MONTH), MONTH_NAME[cal.get(Calendar.MONTH)],
            cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
            tzsign, tzoffset / 60, tzoffset % 60);
    }
}
