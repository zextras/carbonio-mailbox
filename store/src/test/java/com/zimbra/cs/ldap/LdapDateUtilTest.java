// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class LdapDateUtilTest {

 @Test
 void parseGeneralizedTime() throws Exception {
  Calendar cal = Calendar.getInstance();
  cal.clear();
  cal.setTimeZone(TimeZone.getTimeZone("GMT"));
  cal.set(2007, 2, 18, 5, 1, 24);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124Z"));

  cal.clear();
  cal.setTimeZone(TimeZone.getTimeZone("GMT"));
  cal.set(2007, 2, 18, 5, 1, 24);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124Z"));

  cal.clear();
  cal.setTimeZone(TimeZone.getTimeZone("GMT"));
  cal.set(2007, 2, 18, 5, 1, 24);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124Z"));

  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.0Z"));

  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.00Z"));
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.000Z"));

  cal.set(Calendar.MILLISECOND, 100);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.1Z"));

  cal.set(Calendar.MILLISECOND, 310);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.31Z"));

  cal.set(Calendar.MILLISECOND, 597);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.597Z"));

  cal.set(Calendar.MILLISECOND, 478);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.4782Z"));

  cal.set(Calendar.MILLISECOND, 712);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.71288Z"));

  cal.set(Calendar.MILLISECOND, 876);
  assertEquals(cal.getTime(), LdapDateUtil.parseGeneralizedTime("20070318050124.876999Z"));
 }


}
