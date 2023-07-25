// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.calendar.ZCalendar.ICalTok;
import com.zimbra.common.calendar.ZCalendar.ZComponent;
import com.zimbra.common.calendar.ZCalendar.ZParameter;
import com.zimbra.common.calendar.ZCalendar.ZProperty;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.mime.ParsedMessage.CalendarPartInfo;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class ZAttendeeTest {

     public String testName;

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  MailboxTestUtil.initServer();
  MailboxTestUtil.clearData();
  System.out.println( testName);
  Provisioning prov = Provisioning.getInstance();
  prov.createAccount("test@testdomain.com", "secret", Maps.<String, Object>newHashMap());
 }


 @Test
 void resourceRsvpTest() throws ServiceException {
  ZAttendee attendee = new ZAttendee("test-resource@testdomain.com", "testResource", null, null, null, "RES", "NON", "AC", Boolean.TRUE,
    null, null, null, null);
  ZProperty prop = new ZProperty("ATTENDEE");
  attendee.setProperty(prop);
  ZParameter rsvpParam = prop.getParameter(ICalTok.RSVP);
  assertTrue(Boolean.parseBoolean(rsvpParam.getValue()));
 }

 @Test
 void resourceRsvpTest2() throws ServiceException, IOException {
  InputStream is = getClass().getResourceAsStream("Calendar_NewMeetingRequest_RsvpTrue.txt");
  ParsedMessage pm = new ParsedMessage(ByteUtil.getContent(is, -1), false);
  CalendarPartInfo cpi = pm.getCalendarPartInfo();
  Iterator<ZComponent> compIter = cpi.cal.getComponentIterator();
  while (compIter.hasNext()) {
   ZComponent comp = compIter.next();
   List<ZProperty> properties = Lists.newArrayList(comp.getPropertyIterator());
   for (ZProperty prop : properties) {
    if (prop.getName().equalsIgnoreCase("ATTENDEE")) {
     ZAttendee attendee = new ZAttendee(prop);
     assertTrue(attendee.getRsvp());
    }
   }
  }
 }

 @Test
 void resourceRsvpTest3() throws Exception {
  InputStream is = getClass().getResourceAsStream("Calendar_NewMeetingRequest_RsvpFalse.txt");
  ParsedMessage pm = new ParsedMessage(ByteUtil.getContent(is, -1), false);
  CalendarPartInfo cpi = pm.getCalendarPartInfo();
  Iterator<ZComponent> compIter = cpi.cal.getComponentIterator();
  while (compIter.hasNext()) {
   ZComponent comp = compIter.next();
   List<ZProperty> properties = Lists.newArrayList(comp.getPropertyIterator());
   for (ZProperty prop : properties) {
    if (prop.getName().equalsIgnoreCase("ATTENDEE")) {
     ZAttendee attendee = new ZAttendee(prop);
     assertFalse(attendee.getRsvp());
    }
   }
  }
 }

 @Test
 void resourceRsvpTest4() throws Exception {
  InputStream is = getClass().getResourceAsStream("Calendar_ChangeMeetingRequest_WO_Rsvp.txt");
  ParsedMessage pm = new ParsedMessage(ByteUtil.getContent(is, -1), false);
  CalendarPartInfo cpi = pm.getCalendarPartInfo();
  Iterator<ZComponent> compIter = cpi.cal.getComponentIterator();
  while (compIter.hasNext()) {
   ZComponent comp = compIter.next();
   List<ZProperty> properties = Lists.newArrayList(comp.getPropertyIterator());
   for (ZProperty prop : properties) {
    if (prop.getName().equalsIgnoreCase("ATTENDEE")) {
     ZAttendee attendee = new ZAttendee(prop);
     assertNull(attendee.getRsvp());
    }
   }
  }
 }

 @Test
 void nullRsvp() throws Exception {
  TimeZoneMap tzMap = new TimeZoneMap(WellKnownTimeZones.getTimeZoneById("JST"));
  Invite inv = new Invite(MailItem.Type.APPOINTMENT, ICalTok.ACCEPTED.toString(),
    tzMap, false);
  assertThat(inv.toString(), StringContains.containsString("rsvp: (not specified)"));
 }
}
