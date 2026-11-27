// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateTimeUtil;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.cs.fb.FreeBusy;
import com.zimbra.cs.fb.FreeBusy.FBInstance;
import com.zimbra.cs.fb.FreeBusy.Interval;
import com.zimbra.cs.mailbox.Appointment.Instance;
import com.zimbra.cs.mailbox.calendar.CalendarMailSender;
import com.zimbra.cs.mailbox.calendar.IcalXmlStrMap;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.ZOrganizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

final class AppointmentAvailability {

  private static final int TWENTY_FIVE_HOURS = 25 * 60 * 60 * 1000;
  private static final int TWO_HOURS = 2 * 60 * 60 * 1000;
  private static final int MIN_CONFLICT_LIST_SIZE = 5;

  private final Appointment appointment;

  AppointmentAvailability(Appointment appointment) {
    this.appointment = appointment;
  }

  static final class Conflict {
    private final Instance mInstance;
    private final FreeBusy mFreeBusy;
    private final String mFreeBusyStatus;

    Conflict(Instance inst, String fbStatus, FreeBusy fb) {
      mInstance = inst;
      mFreeBusyStatus = fbStatus;
      mFreeBusy = fb;
    }

    Instance getInstance() {
      return mInstance;
    }

    FreeBusy getFreeBusy() {
      return mFreeBusy;
    }

    boolean isBusy() {
      return isBusy(mFreeBusyStatus);
    }

    static boolean isBusy(String fbStatus) {
      return IcalXmlStrMap.FBTYPE_BUSY.equals(fbStatus)
          || IcalXmlStrMap.FBTYPE_BUSY_UNAVAILABLE.equals(fbStatus);
    }
  }

  static final class ConflictCheckResult {
    private final List<Conflict> mConflicts;
    private final boolean mTooManyConflicts;
    private final boolean mHasMoreConflicts;

    ConflictCheckResult(
        List<Conflict> conflicts, boolean tooManyConflicts, boolean hasMoreConflicts) {
      mConflicts = conflicts;
      mTooManyConflicts = tooManyConflicts;
      mHasMoreConflicts = hasMoreConflicts;
    }

    List<Conflict> getConflicts() {
      return mConflicts;
    }

    boolean tooManyConflicts() {
      return mTooManyConflicts;
    }

    boolean hasMoreConflicts() {
      return mHasMoreConflicts;
    }
  }

  ConflictCheckResult checkAvailability(
      long now, Invite invite, int maxNumConflicts, int maxPctConflicts)
      throws ServiceException {

    long st, et;
    if (invite.isRecurrence()) {
      // Resource is getting invited to a recurring appointment.
      st = appointment.getStartTime();
      et = appointment.getEndTime();
    } else {
      // Resource is getting invited to a single instance.
      ParsedDateTime dtStart = invite.getStartTime();
      ParsedDateTime dtEnd = invite.getEffectiveEndTime();
      if (dtStart != null && dtEnd != null) {
        st = dtStart.getUtcTime();
        et = dtEnd.getUtcTime();
      } else {
        // Start time and/or end time can't be determined.  Give up.
        return null;
      }
    }
    // Ignore conflicts in the past.
    st = Math.max(st, now);
    if (et <= st) return null;

    OperationContext octxt = new OperationContext(appointment.getAccount());
    Collection<Instance> instances;
    if (invite.isRecurrence()) {
      instances = appointment.expandInstances(st, et, false);
    } else {
      instances = new ArrayList<>(1);
      instances.add(Instance.fromInvite(appointment.getId(), invite));
    }
    if (instances == null || instances.isEmpty()) return null;

    int maxByPct = maxPctConflicts * instances.size() / 100;
    int maxConflicts = Math.min(maxNumConflicts, maxByPct);

    List<Conflict> list = new ArrayList<>();
    int numConflicts = 0;
    boolean hasMoreConflicts = false;
    for (Instance inst : instances) {
      if (numConflicts > Math.max(maxConflicts, MIN_CONFLICT_LIST_SIZE - 1)) {
        hasMoreConflicts = true;
        break;
      }
      long start = inst.getStart();
      long end = inst.getEnd();
      // Run free/busy search of this user between instance start/end times.
      FreeBusy fb =
          appointment
              .getMailbox()
              .getFreeBusy(octxt, appointment.getAccount(), start, end, appointment);
      String status = fb.getBusiest();
      if (Conflict.isBusy(status)) {
        list.add(new Conflict(inst, status, fb));
        numConflicts++;
      }
    }
    return new ConflictCheckResult(list, numConflicts > maxConflicts, hasMoreConflicts);
  }

  static String getBusyTimesString(
      OperationContext octxt,
      Mailbox mbox,
      List<Conflict> list,
      TimeZone tz,
      Locale lc,
      boolean hasMoreConflicts)
      throws ServiceException {
    StringBuilder sb = new StringBuilder();
    for (Conflict avail : list) {
      if (!avail.isBusy()) continue;

      // List conflicting appointments and their organizers.
      FreeBusy fb = avail.getFreeBusy();
      List<FBInstance> instances = new ArrayList<>();
      for (Interval interval : fb) {
        // busy intervals only
        if (Conflict.isBusy(interval.getStatus())) {
          // busy appointments only
          for (FBInstance fbinst : interval.getInstances()) {
            if (Conflict.isBusy(fbinst.getFreeBusy())) instances.add(fbinst);
          }
        }
      }
      for (FBInstance instance : instances) {
        Date startDate = new Date(instance.getStartTime());
        Date endDate = new Date(instance.getEndTime());
        String start = CalendarMailSender.formatDateTime(startDate, tz, lc);
        sb.append(" * ").append(start);
        String end;
        if (DateTimeUtil.sameDay(startDate, endDate, tz)) {
          end = CalendarMailSender.formatTime(endDate, tz, lc);
          sb.append(" - ").append(end);
        } else {
          end = CalendarMailSender.formatDateTime(endDate, tz, lc);
          sb.append("\r\n   - ").append(end);
        }

        int apptId = instance.getApptId();
        long recurIdDt = instance.getRecurIdDt();
        Appointment appt = mbox.getCalendarItemById(octxt, apptId);
        Invite inv = appt.getInviteForRecurId(recurIdDt);
        if (inv != null && inv.hasOrganizer()) {
          ZOrganizer organizer = inv.getOrganizer();
          String orgDispName;
          if (organizer.hasCn())
            orgDispName = organizer.getCn() + " <" + organizer.getAddress() + ">";
          else orgDispName = organizer.getAddress();
          sb.append(
              L10nUtil.getMessage(MsgKey.calendarResourceConflictScheduledBy, lc, orgDispName));
        }
        sb.append("\r\n");
      }
    }
    if (hasMoreConflicts) sb.append(" * ...\r\n");
    return sb.toString();
  }

  static String getDeclinedTimesString(
      List<Conflict> conflicts, boolean allDay, TimeZone tz, Locale lc) {
    StringBuilder sb = new StringBuilder();
    for (Conflict conflict : conflicts) {
      // List declined instances' start times.
      Instance instance = conflict.getInstance();
      Date startDate = new Date(instance.getStart());
      Date endDate = new Date(instance.getEnd());
      if (!allDay) {
        String start = CalendarMailSender.formatDateTime(startDate, tz, lc);
        sb.append(" * ").append(start);
        if (DateTimeUtil.sameDay(startDate, endDate, tz)) {
          String end = CalendarMailSender.formatTime(endDate, tz, lc);
          sb.append(" - ").append(end);
        } else {
          String end = CalendarMailSender.formatDateTime(endDate, tz, lc);
          sb.append("\r\n   - ").append(end);
        }
      } else {
        // all-day appointments
        String start = CalendarMailSender.formatDate(startDate, tz, lc);
        sb.append(" * ").append(start);
        // If each instance is two days or longer, show the end date.
        long duration = endDate.getTime() - startDate.getTime();
        if (duration > TWENTY_FIVE_HOURS) { // longest 1-day = 25 hours on DST "fall back" day
          // Bring back end date by 2 hours so the displayed date is inclusive end date,
          // not exclusive.  Without DST, we can bring it back by just 1 millisecond, but
          // if the instance spans the DST "fall back" date we have a 25-hour day.  Negating
          // that requires pulling back end date by at least 1 hour and 1 millisecond.
          // Pulling back by 2 hours should work in all cases.
          endDate = new Date(instance.getEnd() - TWO_HOURS);
          String end = CalendarMailSender.formatDate(endDate, tz, lc);
          sb.append(" - ").append(end);
        }
      }
      sb.append("\r\n");
    }
    return sb.toString();
  }
}
