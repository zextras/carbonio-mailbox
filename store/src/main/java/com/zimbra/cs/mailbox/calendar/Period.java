// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/**
 * Represents an iCalendar PERIOD (RFC2445 Section 4.3.9)
 * 
 * period     = period-explicit / period-start
 *
 *  period-explicit = date-time "/" date-time
 *  ; [ISO 8601] complete representation basic format for a period of
 *  ; time consisting of a start and end. The start MUST be before the
 *  ; end.
 *
 *  period-start = date-time "/" dur-value
 *  ; [ISO 8601] complete representation basic format for a period of
 *  ; time consisting of a start and positive duration of time.
 */
package com.zimbra.cs.mailbox.calendar;

import java.text.ParseException;

import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.calendar.ParsedDuration;
import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Metadata;

public class Period {

    private ParsedDateTime mStart;  // DATE-TIME in UTC or local without TZ
    private ParsedDateTime mEnd;    // DATE-TIME in UTC or local without TZ
    private ParsedDuration mDuration;

    // If this is true, the period was originally specified with end time
    // rather than duration.
    private boolean mHasEnd;

    public Period(ParsedDateTime start, ParsedDateTime end) {
        mStart = start;
        mEnd = end;
        mDuration = mEnd.difference(mStart);
        mHasEnd = true;
    }

    public Period(ParsedDateTime start, ParsedDuration dur) {
        mStart = start;
        mDuration = dur;
        mEnd = mStart.add(mDuration);
        mHasEnd = false;
    }

    public ParsedDateTime getStart()    { return mStart; }
    public ParsedDateTime getEnd()      { return mEnd; }
    public ParsedDuration getDuration() { return mDuration; }
    public boolean hasEnd()             { return mHasEnd; }

    public String toString() {
        StringBuilder sb = new StringBuilder(mStart.getDateTimePartString(false));
        sb.append("/");
        if (mHasEnd) {
            if (mEnd != null)
                sb.append(mEnd.getDateTimePartString(false));
        } else {
            if (mDuration != null)
                sb.append(mDuration.toString());
        }
        return sb.toString();
    }

    public static Period parse(String value, ICalTimeZone tz, TimeZoneMap tzmap)
    throws ServiceException, ParseException {
        String parsed[] = value.split("\\/", 2);
        if (parsed.length != 2 ||
            parsed[0].length() == 0 || parsed[1].length() == 1)
            throw ServiceException.INVALID_REQUEST(
                    "Invalid PERIOD value \"" + value + "\"", null);

        ParsedDateTime startTime;
        startTime = ParsedDateTime.parse(
                parsed[0], tzmap, tz, tzmap.getLocalTimeZone());

        char ch = parsed[1].charAt(0);
        if (ch == 'P' || ch == '+' || ch == '-') {
            ParsedDuration duration = ParsedDuration.parse(parsed[1]);
            return new Period(startTime, duration);
        } else {
            ParsedDateTime endTime = ParsedDateTime.parse(
                    parsed[1], tzmap, tz, tzmap.getLocalTimeZone());
            return new Period(startTime, endTime);
        }
    }

    private static final String FN_START = "dts";
    private static final String FN_DURATION = "dur";
    private static final String FN_END = "dte";

    public Metadata encodeMetadata() {
        Metadata meta = new Metadata();
        meta.put(FN_START, mStart.getDateTimePartString(false));
        if (mHasEnd)
            meta.put(FN_END, mEnd.getDateTimePartString(false));
        else
            meta.put(FN_DURATION, mDuration);
        
        return meta;
    }

    public static Period decodeMetadata(Metadata meta,
                                        ICalTimeZone tz, TimeZoneMap tzmap)
    throws ServiceException {
        String start = meta.get(FN_START);
        ParsedDateTime startTime;
        try {
            startTime = ParsedDateTime.parse(start, tzmap, tz, tzmap.getLocalTimeZone());
        } catch (ParseException e) {
            throw ServiceException.INVALID_REQUEST(
                    "Invalid PERIOD start time in metadata: " + meta.toString(), e);
        }

        String end = meta.get(FN_END, null);
        if (end != null) {
            ParsedDateTime endTime;
            try {
                endTime = ParsedDateTime.parse(end, tzmap, tz, tzmap.getLocalTimeZone());
            } catch (ParseException e) {
                throw ServiceException.INVALID_REQUEST(
                        "Invalid PERIOD end time in metadata: " + meta.toString(), e);
            }
            return new Period(startTime, endTime);
        } else {
            String durStr = meta.get(FN_DURATION, null);
            if (durStr == null)
                throw ServiceException.INVALID_REQUEST(
                        "PERIOD in metadata missing both end time and duration: " +
                        meta.toString(), null);
            ParsedDuration duration = ParsedDuration.parse(durStr);
            return new Period(startTime, duration);
        }
    }
}
