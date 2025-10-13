// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.util;

import com.google.common.base.Strings;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;

public final class DateUtil {

  /**
   * Returns the number of milliseconds specified by the time interval value. The format of the time
   * interval value is one of the following, where <tt>NN</tt> is a number:
   *
   * <ul>
   *   <li>NNd - days
   *   <li>NNh - hours
   *   <li>NNm - minutes
   *   <li>NNs - seconds
   *   <li>NNms - milli seconds
   *   <li>NN - seconds
   * </ul>
   *
   * @param value the time interval value
   * @param defaultValue returned if the time interval is null or cannot be parsed
   */
  public static long getTimeInterval(String value, long defaultValue) {
    try {
      return getTimeInterval(value);
    } catch (DateUtilException e) {
      return defaultValue;
    }
  }

  private static final long MILLIS_PER_SECOND = 1000;
  private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
  private static final long MILLIS_PER_HOUR   = MILLIS_PER_MINUTE * 60;
  private static final long MILLIS_PER_DAY    = MILLIS_PER_HOUR * 24;

  private static class DateUtilException extends Exception {
    public DateUtilException(String message) {
      super(message);
    }
  }
  private static long getTimeInterval(String value) throws DateUtilException {
    if (value == null || value.isEmpty()) throw new DateUtilException("no value");
    else {
      Matcher matcher = StringUtil.newMatcher("(\\d+)([hmsd]|ms)?", value);
      if (!matcher.matches()) {
        throw new DateUtilException("Invalid duration: " + value);
      }
      long numberValue = Long.parseLong(matcher.group(1));
      String units = Strings.nullToEmpty(matcher.group(2));
      if (units.equals("d")) {
        return numberValue * MILLIS_PER_DAY;
      } else if (units.equals("h")) {
        return numberValue * MILLIS_PER_HOUR;
      } else if (units.equals("m")) {
        return numberValue * MILLIS_PER_MINUTE;
      } else if (units.equals("ms")) {
        return numberValue;
      }
      return numberValue * MILLIS_PER_SECOND;
    }
  }


  /**
   * from LDAP generalized time string (see bug #90820) Format: yyyyMMddHHmmss + optional millis,
   * micros and time zone
   *
   * <p>Examples: 20150527191216GMT 20150527191216.000040Z 20150610215759.659Z
   */
  public static Date parseGeneralizedTime(String time) {
    // first 14 are mandatory. the rest are optional.
    if (time.length() < 14) {
      return null;
    }
    TimeZone tz;
    boolean trailingZ = false;
    if (time.endsWith("Z")) {
      trailingZ = true;
      tz = TimeZone.getTimeZone("GMT");
    } else {
      tz = TimeZone.getDefault();
    }
    int year = Integer.parseInt(time.substring(0, 4));
    int month = Integer.parseInt(time.substring(4, 6)) - 1; // months are 0 base
    int date = Integer.parseInt(time.substring(6, 8));
    int hour = Integer.parseInt(time.substring(8, 10));
    int min = Integer.parseInt(time.substring(10, 12));
    int sec = Integer.parseInt(time.substring(12, 14));
    Calendar calendar = new GregorianCalendar(tz);
    calendar.clear();
    calendar.set(year, month, date, hour, min, sec);
    if (time.length() >= 16 + trailLen(trailingZ) && time.charAt(14) == '.') {
      int fractionLen = time.length() - 15 - trailLen(trailingZ);
      if (fractionLen > 3) {
        // java Date object is only millisecond precision; drop the micros if present
        fractionLen = 3;
      }
      assert (fractionLen > 0);
      int fractionRaw = Integer.parseInt(time.substring(15, 15 + fractionLen));
      int factor = 1;
      for (int i = fractionLen; i < 3; i++) {
        factor *= 10;
      }
      int millis = fractionRaw * factor;
      calendar.set(Calendar.MILLISECOND, millis);
    }
    return calendar.getTime();
  }

  public static int trailLen(boolean trailingChar) {
    return trailingChar ? 1 : 0;
  }
}
