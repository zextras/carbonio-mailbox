// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTimeUtil {

    /**
     * Determines if the two dates are in the same day in the given time zone.
     * @param t1
     * @param t2
     * @param tz
     * @return
     */
    public static boolean sameDay(Date t1, Date t2, TimeZone tz) {
        Calendar cal1 = new GregorianCalendar(tz);
        cal1.setTime(t1);
        Calendar cal2 = new GregorianCalendar(tz);
        cal2.setTime(t2);
        return
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
}
