// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import com.zimbra.common.service.ServiceException;

public final class DateUtilTest {

  @Test
  void getTimeInterval() throws Exception {
    assertEquals(5, DateUtil.getTimeInterval("5ms"));
    assertEquals(10 * Constants.MILLIS_PER_SECOND, DateUtil.getTimeInterval("10s"));
    assertEquals(321 * Constants.MILLIS_PER_SECOND, DateUtil.getTimeInterval("321"));
    assertEquals(5 * Constants.MILLIS_PER_HOUR, DateUtil.getTimeInterval("5h"));
    assertEquals(5 * Constants.MILLIS_PER_DAY, DateUtil.getTimeInterval("5d"));
  }

  @Test
  void getTimeIntervalWithDefault() throws Exception {
    assertEquals(1, DateUtil.getTimeInterval("abc", 1));
    assertEquals(2, DateUtil.getTimeInterval("1a", 2));

    assertEquals(5, DateUtil.getTimeInterval("5ms", 0));
    assertEquals(10 * Constants.MILLIS_PER_SECOND, DateUtil.getTimeInterval("10s", 0));
    assertEquals(321 * Constants.MILLIS_PER_SECOND, DateUtil.getTimeInterval("321", 0));
    assertEquals(5 * Constants.MILLIS_PER_HOUR, DateUtil.getTimeInterval("5h", 0));
    assertEquals(5 * Constants.MILLIS_PER_DAY, DateUtil.getTimeInterval("5d", 0));
  }

  @Test
  void getTimeIntervalSecs() throws Exception {
    assertEquals(1, DateUtil.getTimeIntervalSecs("abc", 1));
    assertEquals(2, DateUtil.getTimeIntervalSecs("1a", 2));

    assertEquals(1, DateUtil.getTimeIntervalSecs("1000ms", 0));
    assertEquals(1, DateUtil.getTimeIntervalSecs("1499ms", 0));
    assertEquals(2, DateUtil.getTimeIntervalSecs("1500ms", 0));
    assertEquals(10, DateUtil.getTimeIntervalSecs("10s", 0));
    assertEquals(321, DateUtil.getTimeIntervalSecs("321", 0));
    assertEquals(5 * Constants.SECONDS_PER_HOUR, DateUtil.getTimeIntervalSecs("5h", 0));
    assertEquals(5 * Constants.SECONDS_PER_DAY, DateUtil.getTimeIntervalSecs("5d", 0));
  }

  @Test
  void negativeTimeInterval() throws Exception {
    assertEquals(1, DateUtil.getTimeInterval("-5", 1));
    assertEquals(1, DateUtil.getTimeInterval("-5m", 1));
    assertEquals(1, DateUtil.getTimeInterval("-30d", 1));
    try {
      DateUtil.getTimeInterval("-5s");
      fail("Parse should have failed");
    } catch (ServiceException e) {
      assertEquals(ServiceException.INVALID_REQUEST, e.getCode());
    }
  }

    private void assertTime(long expected, long actual, long margin) {
        assertTrue(expected - margin <= actual && actual <= expected + margin);
    }

  @Test
  void parseDateSpecifider() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(1998, 11, 25);
    assertEquals(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("12/25/1998", 0L));

    cal.clear();
    cal.set(1989, 11, 25);
    assertEquals(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("1989/12/25", 0L));

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.DATE, 1);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("1day", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.DATE, 1);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("+1day", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.DATE, 10);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("+10day", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.MINUTE, 60);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("+60minute", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.WEEK_OF_YEAR, 1);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("+1week", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.MONTH, 1);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("+1month", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.YEAR, 1);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("+1year", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.DATE, -1);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("-1day", 0L), 1000L);

    cal.setTimeInMillis(System.currentTimeMillis());
    cal.add(Calendar.DATE, 10);
    assertTime(cal.getTimeInMillis(), DateUtil.parseDateSpecifier("p10day", 0L), 1000L);

    assertEquals(1132276598000L, DateUtil.parseDateSpecifier("1132276598000", 0));
  }

  @Test
  void parseISO8601Date() throws Exception {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.clear();
    cal.set(2009, 10, 20);
    assertEquals(cal.getTime(), DateUtil.parseISO8601Date("2009-11-20", new Date(0L)));

    cal.clear();
    cal.set(2009, 10, 20, 13, 55, 49);
    assertEquals(cal.getTime(),
        DateUtil.parseISO8601Date("2009-11-20T13:55:49Z", new Date(0L)),
        "Ignore Z");

    cal.clear();
    cal.set(2009, 10, 20, 13, 55, 49);
    assertEquals(cal.getTime(),
        DateUtil.parseISO8601Date("2009-11-20T13:55:49.823", new Date(0L)),
        "Ignore .823");

    cal.clear();
    cal.set(2009, 10, 20, 13, 55, 49);
    assertEquals(cal.getTime(), DateUtil.parseISO8601Date("2009-11-20t13:55:49.724z", new Date(0L)), "Ignore .724z");
  }

  @Test
  void parseRFC2822Date() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
    cal.set(2007, 4, 1, 9, 27);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("01 May 2007 09:27 -0600", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
    cal.set(2005, 3, 1, 18, 20, 24);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("01 Apr 2005 18:20:24 -0800", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-07:00"));
    cal.set(2003, 9, 10, 12, 52, 52);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Fri, 10 Oct 2003 12:52:52 -0700", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-07:00"));
    cal.set(2005, 3, 27, 11, 14, 18);
    assertEquals(cal.getTime(),
        DateUtil.parseRFC2822Date("Wed, 27 Apr 2005 11:14:18 -0700 (PDT)", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
    cal.set(2008, 1, 8, 0, 56, 2);
    assertEquals(cal.getTime(),
        DateUtil.parseRFC2822Date("Fri, 8 Feb 2008 00:56:02 -0500 (EST)", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-07:00"));
    cal.set(2005, 3, 27, 15, 37, 37);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Wed, 27 Apr 2005 15:37:37 PDT", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(1997, 10, 21, 9, 55, 6);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("21 Nov 97 09:55:06 GMT", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-07:00"));
    cal.set(2007, 4, 1, 9, 41, 26);
    assertEquals(cal.getTime(),
        DateUtil.parseRFC2822Date("Tue,  1 May 2007 09:41(() )():(() )()26 -0700", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(1997, 10, 21, 9, 0, 6);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("21 Nov 97 09::06 GMT", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
    cal.set(1997, 10, 21, 9, 55, 6);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("21 nOV 97 09:55:06 R", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(2006, 10, 21, 9, 55, 6);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("21 11 06 09:55:06 GMT", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getDefault());
    cal.set(2009, 7, 3, 22, 35, 13);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Mon Aug  3 22:35:13 2009", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
    cal.set(2009, 5, 5, 12, 6, 22);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Fri Jun 05 12:06:22 CDT 2009", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
    cal.set(2009, 3, 2, 23, 28, 18);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Thu Apr 02 23:28:18 CDT 2009", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
    cal.set(2009, 2, 7, 2, 53, 31);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Sat Mar 07 02:53:31 CST 2009", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
    cal.set(2009, 1, 27, 11, 5, 49);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Fri Feb 27 11:05:49 CST 2009", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
    cal.set(2009, 1, 6, 15, 56, 40);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Fri Feb 06 15:56:40 CST 2009", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
    cal.set(2008, 11, 11, 22, 10, 4);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Thu Dec 11 22:10:04 CST 2008", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
    cal.set(2008, 2, 8, 20, 33, 2);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Sat Mar 08 20:33:02 PST 2008", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
    cal.set(2007, 11, 19, 22, 11, 4);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Wed Dec 19 22:11:04 CST 2007", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getDefault());
    cal.set(2001, 8, 26, 16, 30, 37);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Wed, 26 Sep 2001 16:30:37", new Date(0L)));

    cal.clear();
    cal.setTimeZone(TimeZone.getDefault());
    cal.set(2001, 8, 25, 13, 20, 59);
    assertEquals(cal.getTime(), DateUtil.parseRFC2822Date("Tue, 25 Sep 2001 13:20:59", new Date(0L)));
  }

  @Test
  void toImapDateTime() throws Exception {
    assertEquals("31-Dec-1969 16:00:00 -0800", DateUtil.toImapDateTime(new Date(0L), TimeZone.getTimeZone("US/Pacific")));
  }

    public void toRFC1123Date() throws Exception {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(1398412488000L);
        assertEquals(DateUtil.toRFC1123Date(cal), "Fri, 25 Apr 2014 07:54:48 GMT");
    }

  @Test
  void parseRFC2822DateAsCalendar() throws Exception {
    Calendar cal = DateUtil.parseRFC2822DateAsCalendar("21 Nov 97 09:55:06 GMT");
    if (cal != null) {
      java.text.DateFormat format = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, java.util.Locale.UK);
      System.out.println(format.format(cal.getTime()));
      format.setTimeZone(TimeZone.getTimeZone("GMT" + DateUtil.getTimezoneString(cal)));
      System.out.println(format.format(cal.getTime()));
    }
  }

}
