// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar;

import java.text.ParseException;
import java.util.Date;

import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.calendar.ZCalendar.ICalTok;
import com.zimbra.common.calendar.ZCalendar.ZParameter;
import com.zimbra.common.calendar.ZCalendar.ZProperty;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.Metadata;

public class RecurId implements Cloneable
{
    static public int RANGE_NONE                 = 1;
    public static final int RANGE_THISANDFUTURE = 2;
    public static final int RANGE_THISANDPRIOR  = 3;
    
    private int mRange;
    private ParsedDateTime mDateTime;
    
//    public RecurrenceId getRecurrenceId(TimeZone localTz) throws ServiceException {
//        RecurrenceId toRet = new RecurrenceId();
//        try {
//            toRet.setValue(mDateTime.getDateTimePartString());
//            if (mDateTime.isUTC()) {
//                toRet.setUtc(true);
//            } else {
//                String tzName = mDateTime.getTZName();
//                if (tzName == null) {
//                    toRet.getParameters().add(new TzId(localTz.getID()));
//                } else {
//                    toRet.getParameters().add(new TzId(tzName));
//                }
//            }
//            
//            switch (mRange) {
//            case RANGE_THISANDFUTURE:
//                toRet.getParameters().add(Range.THISANDFUTURE);
//                break;
//            case RANGE_THISANDPRIOR:
//                toRet.getParameters().add(Range.THISANDPRIOR);
//                break;
//            }
//        } catch (ParseException e) {
//            throw ServiceException.FAILURE("Parsing: "+mDateTime.toString(), e);
//        }
//        
//        return toRet;
//    }
//    
//    public static RecurId parse(RecurrenceId rid, TimeZoneMap tzmap) throws ParseException
//    {
//        ParsedDateTime dt = ParsedDateTime.parse(rid, tzmap);
//        
//        Range range = (Range)rid.getParameters().getParameter(Parameter.RANGE);
//        int rangeVal = RANGE_NONE;
//        if (range != null) {
//            if (range.getValue().equals(Range.THISANDFUTURE.getValue())) {
//                rangeVal = RANGE_THISANDFUTURE;
//            } else if (range.getValue().equals(Range.THISANDPRIOR.getValue())) {
//                rangeVal = RANGE_THISANDPRIOR;
//            }
//        } 
//
//        return new RecurId(dt, rangeVal);
//    }
    
    public static RecurId createFromInstance(Appointment.Instance inst) {
        return new RecurId(ParsedDateTime.fromUTCTime(inst.getStart()), RANGE_NONE);
    }
    
    public RecurId(ParsedDateTime dt, String rangeStr) {
        int range = RANGE_NONE;
        if (rangeStr != null) {
            if (rangeStr.equals("THISANDFUTURE")) {
                range = RANGE_THISANDFUTURE;
            } else if (rangeStr.equals("THISANDPRIOR")) {
                range = RANGE_THISANDPRIOR;
            }
        }
        mRange = range;
        mDateTime = dt;
    }
    
    
    public RecurId(ParsedDateTime dt, int range) {
        mRange = range;
        mDateTime = dt;
    }

    public Object clone() {
        ParsedDateTime dt = mDateTime != null ? (ParsedDateTime) mDateTime.clone() : null;
        return new RecurId(dt, mRange);
    }

    public String toString() {
        StringBuffer toRet = new StringBuffer(mDateTime.toString());
        String range = getRangeStr();
        if (range != null)
            toRet.append(";RANGE=").append(range);
        return toRet.toString();
    }
    
    private String getRangeStr()
    {
        switch (mRange) {
        case RANGE_THISANDFUTURE:
            return "THISANDFUTURE";
        case RANGE_THISANDPRIOR:
            return "THISANDPRIOR";
        }
        return null;
    }
    
    public boolean equals(Object other) {
        if (other == null) { return false; }
        RecurId rhs = (RecurId)other;
        
        if (mRange == rhs.mRange && mDateTime.equals(rhs.mDateTime)) {
            return true;
        }
        
        return false;
    }
    
    public int getRange() { return mRange; }
    public ParsedDateTime getDt() { return mDateTime; }

    public String getDtZ() {
        return mDateTime.getUtcString();
    }

    public boolean withinRange(RecurId other) {
        if (other == null) {
            return false;
        }
        
        if (other.withinRange(mDateTime.getDate())) {
            return true;
        }

        return withinRange(other.mDateTime.getDate());
    }
    
    public boolean withinRange(Date d) {
        int comp = mDateTime.compareTo(d);
        if (comp == 0) {
            return true;
        } else if (comp < 0) { // mDt < d
            return mRange == RANGE_THISANDFUTURE;
        } else {
            return mRange == RANGE_THISANDPRIOR;
        }
    }
    
    public boolean withinRange(long d) {
        int comp = mDateTime.compareTo(d);
        if (comp == 0) {
            return true;
        } else if (comp < 0) { // mDt < d
            return mRange == RANGE_THISANDFUTURE;
        } else {
            return mRange == RANGE_THISANDPRIOR;
        }
    }
    
    
    private static final String FN_DT = "dt";
    private static final String FN_RANGE = "r";
    
    public Metadata encodeMetadata() {
        Metadata md = new Metadata();
        md.put(FN_DT, mDateTime.toString());
        md.put(FN_RANGE, mRange);
        return md;
    }
    
    public ZProperty toProperty(boolean useOutlookCompatMode) {
//        ZProperty toRet = new ZProperty(ICalTok.RECURRENCE_ID, toString());
//        return toRet;
        ZProperty toRet = mDateTime.toProperty(ICalTok.RECURRENCE_ID, useOutlookCompatMode);
        String range = getRangeStr();
        if (range != null) 
            toRet.addParameter(new ZParameter(ICalTok.RANGE, range));
        
        return toRet;
    }
    
    public Element toXml(Element parent) {
        parent.addAttribute(MailConstants.A_CAL_RECURRENCE_RANGE_TYPE, mRange);
        parent.addAttribute(MailConstants.A_CAL_RECURRENCE_ID, mDateTime.getDateTimePartString(false));
        if (mDateTime.hasTime()) {
            parent.addAttribute(MailConstants.A_CAL_TIMEZONE, mDateTime.getTZName());
            parent.addAttribute(MailConstants.A_CAL_RECURRENCE_ID_Z, getDtZ());
        }
        return parent;
    }
    
    public static RecurId fromXml(Element e, TimeZoneMap tzMap) throws ServiceException {
        assert(tzMap != null);
    	String recurrenceId = e.getAttribute(MailConstants.A_CAL_RECURRENCE_ID, null);
    	if (recurrenceId == null) return null;
    	String tzId = e.getAttribute(MailConstants.A_CAL_TIMEZONE, null);
    	ICalTimeZone tz = tzId != null ? tzMap.lookupAndAdd(tzId) : null;
    	String rangeType = e.getAttribute(MailConstants.A_CAL_RECURRENCE_RANGE_TYPE, null);
    	try {
            ParsedDateTime dt = ParsedDateTime.parse(recurrenceId, tzMap, tz, tzMap.getLocalTimeZone());
            if (rangeType != null)
                return new RecurId(dt, rangeType);
            else
                return new RecurId(dt, RANGE_NONE);
    	} catch (ParseException x) {
    		throw ServiceException.FAILURE("recurId=" + recurrenceId + ", tz=" + tzId, x);
    	}
    }
    
    public static RecurId decodeMetadata(Metadata md, TimeZoneMap tzmap) throws ServiceException {
        try {
            return new RecurId(ParsedDateTime.parse(md.get(FN_DT), tzmap), (int)md.getLong(FN_RANGE));
        } catch (ParseException e) {
            throw ServiceException.FAILURE("Parsing "+md.get(FN_DT), e);
        }
    }
}
