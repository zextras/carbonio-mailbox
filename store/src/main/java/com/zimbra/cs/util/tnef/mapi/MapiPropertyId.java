// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.zimbra.cs.util.tnef.IcalUtil;
import com.zimbra.cs.util.tnef.MSGUID;
import com.zimbra.cs.util.tnef.SchedulingViewOfTnef;

import net.fortuna.ical4j.model.DateTime;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIPropName;
import net.freeutils.tnef.MAPIProps;
import net.freeutils.tnef.MAPIValue;
import net.freeutils.tnef.RawInputStream;

public class MapiPropertyId {

    public final static MapiPropertyId PidTagImportance =
        new MapiPropertyId(MAPIProp.PR_IMPORTANCE);
    public final static MapiPropertyId PidTagSensitivity =
        new MapiPropertyId(MAPIProp.PR_SENSITIVITY);
    public final static MapiPropertyId PidTagResponseRequested =
        new MapiPropertyId(MAPIProp.PR_RESPONSE_REQUESTED);
    public final static MapiPropertyId PidTagReplyRequested =
        new MapiPropertyId(MAPIProp.PR_REPLY_REQUESTED);
    public final static MapiPropertyId PidTagOwnerAppointmentId =
        new MapiPropertyId(MAPIProp.PR_OWNER_APPT_ID);
    public final static MapiPropertyId PidTagEndDate =
        new MapiPropertyId(MAPIProp.PR_END_DATE);
    public final static MapiPropertyId PidTagStartDate =
        new MapiPropertyId(MAPIProp.PR_START_DATE);
    public final static MapiPropertyId PidTagCreationTime =
        new MapiPropertyId(MAPIProp.PR_CREATION_TIME);
    public final static MapiPropertyId PidTagLastModificationTime =
        new MapiPropertyId(MAPIProp.PR_LAST_MODIFICATION_TIME);
    public final static MapiPropertyId PidTagRtfCompressed =
        new MapiPropertyId(MAPIProp.PR_RTF_COMPRESSED);
    public final static MapiPropertyId PidLidReminderDelta =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8501);
    public final static MapiPropertyId PidLidReminderSet =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8503);
    public final static MapiPropertyId PidLidCommonStart =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8516);
    public final static MapiPropertyId PidLidCommonEnd =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8517);
    public final static MapiPropertyId PidLidTaskMode =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8518);
    public final static MapiPropertyId PidLidTaskGlobalId =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8519);
    public final static MapiPropertyId PidLidMileage =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8534);
    public final static MapiPropertyId PidLidBilling =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8535);
    public final static MapiPropertyId PidLidCompanies =
        new MapiPropertyId(MSGUID.PSETID_Common, 0x8539);
    public final static MapiPropertyId PidNameKeywords =
        new MapiPropertyId(MSGUID.PS_PUBLIC_STRINGS, "Keywords");
    public final static MapiPropertyId PidNameCalendarUid =
        new MapiPropertyId(MSGUID.PS_PUBLIC_STRINGS,
                "urn:schemas:calendar:uid");
    public final static MapiPropertyId PidLidAppointmentSequence =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8201);
    public final static MapiPropertyId PidLidAppointmentStartWhole =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x820D);
    public final static MapiPropertyId PidLidAppointmentEndWhole =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x820E);
    public final static MapiPropertyId PidLidAppointmentSubType =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8215);
    public final static MapiPropertyId PidLidAppointmentRecur =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8216);
    public final static MapiPropertyId PidLidAppointmentStateFlags =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8217);
    public final static MapiPropertyId PidLidBusyStatus =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8205);
    public final static MapiPropertyId PidLidLocation =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8208);
    public final static MapiPropertyId PidLidAppointmentReplyTime =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8220);
    public final static MapiPropertyId PidLidIntendedBusyStatus =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8224);
    public final static MapiPropertyId PidLidExceptionReplaceTime =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8228);
    public final static MapiPropertyId PidLidTimeZoneDescription =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8234);
    public final static MapiPropertyId PidLidAppointmentProposedStartWhole =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8250);
    public final static MapiPropertyId PidLidAppointmentProposedEndWhole =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8251);
    public final static MapiPropertyId PidLidAppointmentCounterProposal =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8257);
    public final static MapiPropertyId PidLidAppointmentNotAllowPropose =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x825A);
    // PidLidAppointmentTimeZoneDefinitionStartDisplay - Specifies
    // time zone information applicable to PidLidAppointmentStartWhole.
    public final static MapiPropertyId
        PidLidAppointmentTimeZoneDefinitionStartDisplay =
            new MapiPropertyId(MSGUID.PSETID_Appointment, 0x825E);
    // PidLidAppointmentTimeZoneDefinitionEndDisplay - Specifies
    // time zone information applicable to PidLidAppointmentEndWhole.
    public final static MapiPropertyId
        PidLidAppointmentTimeZoneDefinitionEndDisplay =
            new MapiPropertyId(MSGUID.PSETID_Appointment, 0x825F);
    // PidLidAppointmentTimeZoneDefinitionRecur - Specifies time zone information
    // that describes how to convert the meeting date and time on a recurring
    // series to and from UTC.
    // MS-OXOCAL says "If this property is set, but it has data that is
    // inconsistent with the data that is represented by PidLidTimeZoneStruct,
    // then the client uses PidLidTimeZoneStruct instead of this property."
    public final static MapiPropertyId
        PidLidAppointmentTimeZoneDefinitionRecur =
            new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8260);
    public final static MapiPropertyId PidLidTimeZoneStruct =
        new MapiPropertyId(MSGUID.PSETID_Appointment, 0x8233);
    public final static MapiPropertyId PidLidAttendeeCriticalChange =
        new MapiPropertyId(MSGUID.PSETID_Meeting, 0x0001);
    public final static MapiPropertyId PidLidGlobalObjectId =
        new MapiPropertyId(MSGUID.PSETID_Meeting, 0x0003);
    public final static MapiPropertyId PidLidOwnerCriticalChange =
        new MapiPropertyId(MSGUID.PSETID_Meeting, 0x001a);
    public final static MapiPropertyId PidLidCleanGlobalObjectId =
        new MapiPropertyId(MSGUID.PSETID_Meeting, 0x0023);
    public final static MapiPropertyId PidLidMeetingType =
        new MapiPropertyId(MSGUID.PSETID_Meeting, 0x0026);
    public final static MapiPropertyId PidLidStartRecurrenceTime =
        new MapiPropertyId(MSGUID.PSETID_Meeting, 0x000e);

    // PSETID_Task - MSGUID("{00062003-0000-0000-C000-000000000046}");
    public final static MapiPropertyId PidLidTaskStatus =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8101);
    /* PidLidPercentComplete - Double -value between 0 and 1! */
    public final static MapiPropertyId PidLidPercentComplete =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8102);
    // PidLidTaskStartDate - MS-OXOTASK this is start date in user's local timezone.
    // MS-OXOTASK also says PidLidCommonStart should be UTC equivalent of PidLidTaskStartDate
    public final static MapiPropertyId PidLidTaskStartDate =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8104);
    // PidLidTaskDueDate - MS-OXOTASK this is due date in user's local timezone.
    // MS-OXOTASK also says PidLidCommonEnd should be UTC equivalent of PidLidTaskDueDate
    public final static MapiPropertyId PidLidTaskDueDate =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8105);
    // PidLidTaskResetReminder 0x8107 - true if future instances need reminders
    // PidLidTaskAccepted 0x8108 - boolean
    public final static MapiPropertyId PidLidTaskDateCompleted = /* UTC value */
        new MapiPropertyId(MSGUID.PSETID_Task, 0x810f);
    /* PidLidTaskActualEffort - Number of mins.  Works on assumption 8 hrs/day 5 days/week */
    public final static MapiPropertyId PidLidTaskActualEffort =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8110);
    /* PidLidTaskEstimatedEffort - Number of mins.  Works on assumption 8 hrs/day 5 days/week */
    public final static MapiPropertyId PidLidTaskEstimatedEffort =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8111);
    public final static MapiPropertyId PidLidTaskVersion =  /* integer - suitable for iCal SEQUENCE */
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8112);
    // PidLidTaskState 0x8113 - Current assignment state of Task Object
    // PidLidTaskLastUpdate 0x8115 -  UTC value
    // DeletedInstanceCount and ModifiedInstanceCount MUST be 0 - hence no EXDATE/RDATE
    public final static MapiPropertyId PidLidTaskRecurrence =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8116);
    // PidLidTaskAssigners 0x8117 - binary info on each of past assigners
    // PidLidTaskStatusOnComplete - true if assignee asked to RSVP on completion.
    public final static MapiPropertyId PidLidTaskStatusOnComplete =
        new MapiPropertyId(MSGUID.PSETID_Task, 0x8119);
    // PidLidTaskHistory 0x811a - indicates nature of last change
    // PidLidTaskUpdates 0x811b - true if assignee asked to RSVP on changes
    //    (c.f. PidLidTaskStatusOnComplete)
    public final static MapiPropertyId PidLidTaskComplete = /* boolean */
        new MapiPropertyId(MSGUID.PSETID_Task, 0x811c);
    // PidLidTaskFCreator 0x811e - boolean - false if task assigned by another user.
    // PidLidTaskOwner 0x811f - String - name of task owner
    // PidLidTaskAssigner 0x8121 - String - name user that last assigned task
    // PidLidTaskLastUser 0x8122 - String - name of user who was last the owner
    // PidLidTaskLastDelegate 0x8125 - name of mailbox's delegate who most recently assigned the task
    // PidLidTaskOwnership 0x8129 - values not assigned(0)/assigner copy(1)/assignee copy(2)
    // PidLidTaskAcceptanceState 0x812a - not assigned(0)/unknown(1)/assignee accepted(2)/assignee rejected(3)

    private MAPIPropName mapiPropName;
    private int id;

    private MapiPropertyId(MSGUID msg, long lid) {
        this.mapiPropName = new MAPIPropName(msg.getJtnefGuid(), lid);
        this.id = 0;
    }

    private MapiPropertyId(MSGUID msg, String propNameId) {
        this.mapiPropName = new MAPIPropName(msg.getJtnefGuid(), propNameId);
        this.id = 0;
    }

    private MapiPropertyId(int id) {
        this.mapiPropName = null;
        this.id = id;
    }

    @Override
    public String toString() {
        if (mapiPropName == null) {
            return mapiPropName.toString();
        } else {
            return "ID=" + id;
        }
    }

    public String getStringValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        Object obj;
        if (mpValue.getType() == MAPIProp.PT_STRING) {
            // Assume the value is in the OEM Code Page
            // The current MAPIValue code does not take account of that.
            RawInputStream ris = mpValue.getRawData();
            return IcalUtil.readString(ris, (int)ris.getLength(),
                        schedView.getOEMCodePage());
        } else {
            // Probably PT_UNICODE_STRING but will accept anything whose value is
            // a String.
            obj = mpValue.getValue();
        }
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    public Boolean getBooleanValue(SchedulingViewOfTnef schedView, boolean defaultValue) throws IOException {
        Boolean truthValue = getBooleanValue(schedView);
        if (truthValue == null) {
            truthValue = new Boolean(defaultValue);
        }
        return truthValue;
    }

    public Boolean getBooleanValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        Object obj = mpValue.getValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return null;
    }

    /**
     * Return the Integer value corresponding to a MAPI Property with value
     * PT_INT or PT_ERROR
     * @param schedView represents a TNEF which might contain a property with this
     * object's ID.
     * @param defaultVal
     * @return
     * @throws IOException
     */
    public Integer getIntegerValue(SchedulingViewOfTnef schedView, int defaultVal) throws IOException {
        Integer retVal = this.getIntegerValue(schedView);
        if (retVal == null) {
            retVal = Integer.valueOf(defaultVal);
        }
        return retVal;
    }

    /**
     * Return the Integer value corresponding to a MAPI Property with value
     * PT_INT or PT_ERROR
     * @param schedView represents a TNEF which might contain a property with this
     * object's ID.
     * @return
     * @throws IOException
     */
    public Integer getIntegerValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        Object obj = mpValue.getValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return null;
    }

    /**
     * Return the Double value corresponding to a MAPI Property with value
     * PT_DOUBLE
     * @param schedView represents a TNEF which might contain a property with this
     * object's ID.
     * @return
     * @throws IOException
     */
    public Double getDoubleValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        Object obj = mpValue.getValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof Double) {
            return (Double) obj;
        }
        return null;
    }

    /**
     * PT_SYSTIME properties wrap FILETIME which is a count of seconds since start of 1601
     * Note that some PT_SYSTIME properties are actually stored such that if they are treated
     * as UTC times, the day/month/year/hour/min etc components are correct in localtime.
     * Calling this method for such properties will result in a DateTime which doesn't reflect
     * the underlying real date and time on its own - but this can still be useful.
     * @param schedView
     * @return DateTime object equivalent to a PT_SYSTIME property where that property
     *         represents a time in UTC
     * @throws IOException
     */
    public DateTime getDateTimeAsUTC(SchedulingViewOfTnef schedView) throws IOException {
        Date javaDate = getDateValue(schedView);
        if (javaDate == null) {
            return null;
        }
        DateTime icalDateTime = new net.fortuna.ical4j.model.DateTime(javaDate);
        icalDateTime.setUtc(true);
        return icalDateTime;
    }

    public Date getDateValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        Object obj = mpValue.getValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof Date) {
            return (Date) obj;
        }
        return null;
    }

    /**
     * PT_SYSTIME properties wrap FILETIME which is a count of seconds since start of 1601
     * @param schedView represents a TNEF which might contain a property with this
     * object's ID.
     * @return Long as seconds since start of 1601 equivalent to a PT_SYSTIME property
     * @throws IOException
     */
    public Long get100nsPeriodsSince1601(SchedulingViewOfTnef schedView) throws IOException {
        RawInputStream ris = getRawInputStreamValue(schedView);
        if (ris == null) {
            return null;
        }
        if (ris.getLength() != 8) {
            return null;
        }
        // 64-bit Windows FILETIME is 100ns since January 1, 1601
        return ris.readU64();
    }

    public RawInputStream getRawInputStreamValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        return mpValue.getRawData();
    }

    public byte[] getByteArrayValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue mpValue = getFirstValue(schedView);
        if (mpValue == null) {
            return null;
        }
        Object obj = mpValue.getValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        } else if (obj instanceof RawInputStream) {
            RawInputStream ris = (RawInputStream) obj;
            return ris.toByteArray();
        }
        return null;
    }

    public MAPIValue getFirstValue(SchedulingViewOfTnef schedView) throws IOException {
        MAPIValue[] mpValues = getValues(schedView);
        if (mpValues == null) {
            return null;
        }
        if (mpValues.length < 1) {
            return null;
        }
        return mpValues[0];
    }

    public MAPIValue[] getValues(SchedulingViewOfTnef schedView) throws IOException {
        MAPIProp mp = getProp(schedView);
        if (mp == null) {
            return null;
        }
        return mp.getValues();
    }

    public MAPIProp getProp(SchedulingViewOfTnef schedView) throws IOException {
        MAPIProp mp;
        List <?> attribs = schedView.getAttributes();
        for (Object thisObj : attribs) {
            if (! (thisObj instanceof Attr)) {
                continue;
            }
            Attr thisAtt = (Attr) thisObj;
            Object o = thisAtt.getValue();
            if (o == null) {
                continue;
            }
            if (thisAtt.getID() != (Attr.attMAPIProps)) {
                continue;
            }
            if (!(o instanceof MAPIProps)) {
                return null;
            }
            MAPIProps thisPropset = (MAPIProps) o;
            if (this.mapiPropName == null) {
                mp = thisPropset.getProp(id);
            } else {
                mp = thisPropset.getProp(this.mapiPropName);
            }
            if (mp != null) {
                return mp;
            }
        }
        return null;
    }

}
