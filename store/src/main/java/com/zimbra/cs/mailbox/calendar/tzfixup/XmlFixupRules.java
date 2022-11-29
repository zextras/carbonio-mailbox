// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.tzfixup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.calendar.tzfixup.TimeZoneFixupRules.Matcher;
import com.zimbra.cs.service.mail.CalendarUtils;

/*

<?xml version="1.0" encoding="utf-8"?>
<tzfixup xmlns="url:zimbraTZFixup">
  <!-- specify multiple fixupRule's -->
  <fixupRule>
    <!-- if timezone matches any of the criteria, replace with the timezone in <replace> -->
    <!-- specify one or many criteria; each type can be specified multiple times -->
    <match>
      <!-- match any/all timezones -->
      <any/>

      <!-- match the timezone's TZID string -->
      <tzid id="[TZID]"/>

      <!-- match the GMT offset of a timezone that doesn't use daylight savings time -->
      <nonDst offset="[GMT offset in minutes]"/>

      <!-- match DST timezone based on transition rules specified as month and week number + week day -->
      <rules stdoff="[GMT offset in minutes during standard time]"
             dayoff="[GMT offset in minutes during daylight savings time]">
        <standard mon="[1..12]" week="[-1, 1..4]" wkday="[1..7]"/>
        <daylight mon="[1..12]" week="[-1, 1..4]" wkday="[1..7]"/>
        <!-- mon=1 means January, mon=12 means December -->
        <!-- week=-1 means last week of the month -->
        <!-- wkday=1 means Sunday, wkday = 7 means Saturday -->
      </rules>

      <!-- match DST timezone based on transition rules specified as month and day of month -->
      <!-- This case is rare and is typically required only for timezones introduced by buggy code. -->
      <dates stdoff="[GMT offset in minutes during standard time]"
             dayoff="[GMT offset in minutes during daylight savings time]">
        <standard mon="[1..12]" mday="[1..31]"/>
        <daylight mon="[1..12]" mday="[1..31]"/>
        <!-- mon=1 means January, mon=12 means December -->
      </dates>
    </match>

    <!-- "touch" the timezone without any data change to force sync clients to refetch -->
    <touch/>
    OR
    <!-- timezone matching any of the above criteria is replaced with this timezone -->
    <replace>
      <!-- lookup a well-known timezone from /opt/zextras/conf/timezones.ics file -->
      <wellKnownTz id="[well-known TZID]">
      OR
      <!-- full timezone definition as documented in soap-calendar.txt -->
      <tz id="[custom TZID]" ... />
    </replace>
  </fixupRule>
</tzfixup>

Note the TZID value of the replacement timezone is not used.  The replaced timezone will retain the
original TZID because the appointment/task containing the timezone has other properties/parameters
that refer to the existing TZID.  Only the definition of the timezone is replaced.

 */
public class XmlFixupRules {

    private static void parseMatchers(Element matchElem, ICalTimeZone replacementTZ,
                                      List<Matcher> matchers)
    throws ServiceException {
        for (Iterator<Element> elemIter = matchElem.elementIterator(); elemIter.hasNext(); ) {
            Element elem = elemIter.next();
            String elemName = elem.getName();
            if (elemName.equals(AdminConstants.E_ANY)) {
                matchers.add(new Matcher(replacementTZ));
            } else if (elemName.equals(AdminConstants.E_TZID)) {
                String tzid = elem.getAttribute(AdminConstants.A_ID);
                matchers.add(new Matcher(tzid, replacementTZ));
            } else if (elemName.equals(AdminConstants.E_NON_DST)) {
                long offset = elem.getAttributeLong(AdminConstants.A_OFFSET);
                matchers.add(new Matcher(offset, replacementTZ));
            } else if (elemName.equals(AdminConstants.E_RULES)) {
                long stdOffset = elem.getAttributeLong(AdminConstants.A_STDOFF);
                long dstOffset = elem.getAttributeLong(AdminConstants.A_DAYOFF);
                Element stdElem = elem.getElement(AdminConstants.E_STANDARD);
                int stdMon = (int) stdElem.getAttributeLong(AdminConstants.A_MON);
                int stdWeek = (int) stdElem.getAttributeLong(AdminConstants.A_WEEK);
                int stdWkday = (int) stdElem.getAttributeLong(AdminConstants.A_WKDAY);
                Element dstElem = elem.getElement(AdminConstants.E_DAYLIGHT);
                int dstMon = (int) dstElem.getAttributeLong(AdminConstants.A_MON);
                int dstWeek = (int) dstElem.getAttributeLong(AdminConstants.A_WEEK);
                int dstWkday = (int) dstElem.getAttributeLong(AdminConstants.A_WKDAY);
                Matcher m = new Matcher(stdOffset, stdMon, stdWeek, stdWkday,
                                        dstOffset, dstMon, dstWeek, dstWkday,
                                        replacementTZ);
                matchers.add(m);
            } else if (elemName.equals(AdminConstants.E_DATES)) {
                long stdOffset = elem.getAttributeLong(AdminConstants.A_STDOFF);
                long dstOffset = elem.getAttributeLong(AdminConstants.A_DAYOFF);
                Element stdElem = elem.getElement(AdminConstants.E_STANDARD);
                int stdMon = (int) stdElem.getAttributeLong(AdminConstants.A_MON);
                int stdMday = (int) stdElem.getAttributeLong(AdminConstants.A_MDAY);
                Element dstElem = elem.getElement(AdminConstants.E_DAYLIGHT);
                int dstMon = (int) dstElem.getAttributeLong(AdminConstants.A_MON);
                int dstMday = (int) dstElem.getAttributeLong(AdminConstants.A_MDAY);
                Matcher m = new Matcher(stdOffset, stdMon, stdMday,
                                        dstOffset, dstMon, dstMday,
                                        replacementTZ);
                matchers.add(m);
            }
        }
    }

    private static void parseFixupRule(Element fixupRuleElem, List<Matcher> matchers)
    throws ServiceException {
        Element matchElem = fixupRuleElem.getElement(AdminConstants.E_MATCH);
        Element touchElem = fixupRuleElem.getOptionalElement(AdminConstants.E_TOUCH);
        Element replaceElem = fixupRuleElem.getOptionalElement(AdminConstants.E_REPLACE);
        if (touchElem == null && replaceElem == null)
            throw ServiceException.FAILURE("Neither <touch> nor <replace> found in <fixupRule>", null);
        else if (touchElem != null && replaceElem != null)
            throw ServiceException.FAILURE("<fixupRule> must not have both <touch> and <replace>", null);

        ICalTimeZone replacementTZ;
        if (touchElem != null) {
            replacementTZ = null;  // null replacement means touch-only
        } else {
            Element wellKnownTzElem = replaceElem.getOptionalElement(AdminConstants.E_WELL_KNOWN_TZ);
            if (wellKnownTzElem != null) {
                String tzid = wellKnownTzElem.getAttribute(AdminConstants.A_ID);
                replacementTZ = WellKnownTimeZones.getTimeZoneById(tzid);
                if (replacementTZ == null)
                    throw ServiceException.FAILURE("Unknown TZID \"" + tzid + "\"", null);
            } else {
                Element tzElem = replaceElem.getOptionalElement(MailConstants.E_CAL_TZ);
                if (tzElem == null)
                    throw ServiceException.FAILURE("Neither <tz> nor <wellKnownTz> found in <replace>", null);
                replacementTZ = CalendarUtils.parseTzElement(tzElem);
            }
        }
        parseMatchers(matchElem, replacementTZ, matchers);
    }

    public static List<Matcher> parseTzFixup(Element tzFixupElem)
    throws ServiceException {
        List<Matcher> matchers = new ArrayList<Matcher>();
        for (Iterator<Element> elemIter = tzFixupElem.elementIterator(AdminConstants.E_FIXUP_RULE); elemIter.hasNext(); ) {
            Element fixupRuleElem = elemIter.next();
            parseFixupRule(fixupRuleElem, matchers);
        }
        return matchers;
    }
}
