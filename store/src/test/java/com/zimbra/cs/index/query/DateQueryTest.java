// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.lucene.document.DateTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link DateQuery}.
 *
 * @author ysasaki
 */
public final class DateQueryTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MockProvisioning prov = new MockProvisioning();
        prov.createAccount("zero@zimbra.com", "secret", new HashMap<String, Object>());
        Provisioning.setInstance(prov);
    }

 @Test
 void parseAbsoluteDate() throws Exception {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  DateQuery query = new DateQuery(DateQuery.Type.DATE);
  TimeZone tz = TimeZone.getTimeZone("UTC");
  String expected = "Q(DATE:DATE,201001230000-201001240000)";

  query.parseDate("1/23/2010", tz, Locale.ENGLISH);
  assertEquals(expected, query.toString());

  query.parseDate("23/1/2010", tz, Locale.FRENCH);
  assertEquals(expected, query.toString());

  query.parseDate("23.1.2010", tz, Locale.GERMAN);
  assertEquals(expected, query.toString());

  query.parseDate("23/1/2010", tz, Locale.ITALIAN);
  assertEquals(expected, query.toString());

  query.parseDate("2010/1/23", tz, Locale.JAPANESE);
  assertEquals(expected, query.toString());

  //Korean date ends with dot
  query.parseDate("2010. 1. 23.", tz, Locale.KOREAN);
  assertEquals(expected, query.toString());
 }

 @Test
 void parseAbsoluteMDate() throws Exception {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  DateQuery query = new DateQuery(DateQuery.Type.MDATE);
  TimeZone tz = TimeZone.getTimeZone("UTC");
  String expected = "Q(DATE:MDATE,201001230000-201001240000)";

  query.parseDate("1/23/2010", tz, Locale.ENGLISH);
  assertEquals(expected, query.toString());

  query.parseDate("23/1/2010", tz, Locale.FRENCH);
  assertEquals(expected, query.toString());

  query.parseDate("23.1.2010", tz, Locale.GERMAN);
  assertEquals(expected, query.toString());

  query.parseDate("23/1/2010", tz, Locale.ITALIAN);
  assertEquals(expected, query.toString());

  query.parseDate("2010/1/23", tz, Locale.JAPANESE);
  assertEquals(expected, query.toString());

  // Korean date ends with dot
  query.parseDate("2010. 1. 23.", tz, Locale.KOREAN);
  assertEquals(expected, query.toString());
 }

 @Test
 void parseRelativeDate() throws Exception {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  DateQuery query = new DateQuery(DateQuery.Type.DATE);
  TimeZone tz = TimeZone.getTimeZone("UTC");

  query.parseDate("+2mi", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getMinute(2) + "-" + getMinute(3) + ")", query.toString());

  query.parseDate("+2minute", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getMinute(2) + "-" + getMinute(3) + ")", query.toString());

  query.parseDate("+2minutes", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getMinute(2) + "-" + getMinute(3) + ")", query.toString());

  query.parseDate("+2h", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getHour(2) + "-" + getHour(3) + ")", query.toString());

  query.parseDate("+2hour", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getHour(2) + "-" + getHour(3) + ")", query.toString());

  query.parseDate("+2hours", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getHour(2) + "-" + getHour(3) + ")", query.toString());

  query.parseDate("+2d", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getDate(2) + "-" + getDate(3) + ")", query.toString());

  query.parseDate("+2day", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getDate(2) + "-" + getDate(3) + ")", query.toString());

  query.parseDate("+2days", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getDate(2) + "-" + getDate(3) + ")", query.toString());

  query.parseDate("+2w", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getWeek(2) + "-" + getWeek(3) + ")", query.toString());

  query.parseDate("+2week", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getWeek(2) + "-" + getWeek(3) + ")", query.toString());

  query.parseDate("+2weeks", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getWeek(2) + "-" + getWeek(3) + ")", query.toString());

  query.parseDate("+2m", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getMonth(2) + "-" + getMonth(3) + ")", query.toString());

  query.parseDate("+2month", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getMonth(2) + "-" + getMonth(3) + ")", query.toString());

  query.parseDate("+2months", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getMonth(2) + "-" + getMonth(3) + ")", query.toString());

  query.parseDate("+2y", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getYear(2) + "-" + getYear(3) + ")", query.toString());

  query.parseDate("+2year", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getYear(2) + "-" + getYear(3) + ")", query.toString());

  query.parseDate("+2years", tz, Locale.ENGLISH);
  assertEquals("Q(DATE:DATE," + getYear(2) + "-" + getYear(3) + ")", query.toString());
 }

    private String getMinute(int minute) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.MINUTE, minute);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

    private String getHour(int hour) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

    private String getDate(int day) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

    private String getWeek(int week) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

    private String getMonth(int month) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

    private String getYear(int year) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

 @Test
 void parseInvalidDate() throws Exception {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  DateQuery query = new DateQuery(DateQuery.Type.DATE);
  TimeZone tz = TimeZone.getTimeZone("UTC");

  try {
   query.parseDate("-1/1/2010", tz, Locale.ENGLISH);
   fail();
  } catch (ParseException expected) {
  }

  try {
   query.parseDate("1/-1/2010", tz, Locale.ENGLISH);
   fail();
  } catch (ParseException expected) {
  }

  try {
   query.parseDate("1/1/-2010", tz, Locale.ENGLISH);
   fail();
  } catch (ParseException expected) {
  }

  try {
   query.parseDate("111/1/2010", tz, Locale.ENGLISH);
   fail();
  } catch (ParseException expected) {
  }

  try {
   query.parseDate("1/111/2010", tz, Locale.ENGLISH);
   fail();
  } catch (ParseException expected) {
  }
 }

 @Test
 void parseDateFallback() throws Exception {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  DateQuery query = new DateQuery(DateQuery.Type.DATE);
  query.parseDate("1/23/2010", TimeZone.getTimeZone("UTC"), Locale.GERMAN);
  assertEquals("Q(DATE:DATE,201001230000-201001240000)", query.toString());
 }

}
