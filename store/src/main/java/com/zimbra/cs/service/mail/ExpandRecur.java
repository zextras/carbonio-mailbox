// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.calendar.ParsedDuration;
import com.zimbra.common.calendar.TimeZoneMap;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.CalendarItem.Instance;
import com.zimbra.cs.mailbox.calendar.RecurId;
import com.zimbra.cs.mailbox.calendar.Recurrence.CancellationRule;
import com.zimbra.cs.mailbox.calendar.Recurrence.ExceptionRule;
import com.zimbra.cs.mailbox.calendar.Recurrence.IException;
import com.zimbra.cs.mailbox.calendar.Recurrence.IRecurrence;
import com.zimbra.cs.mailbox.calendar.Recurrence.RecurrenceRule;
import com.zimbra.cs.mailbox.calendar.Util;
import com.zimbra.soap.ZimbraSoapContext;

public class ExpandRecur extends MailDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account authAcct = getAuthenticatedAccount(zsc);

        long rangeStart = request.getAttributeLong(MailConstants.A_CAL_START_TIME);
        long rangeEnd = request.getAttributeLong(MailConstants.A_CAL_END_TIME);
        long days = (rangeEnd-rangeStart)/Constants.MILLIS_PER_DAY;
        long maxDays = LC.calendar_freebusy_max_days.longValueWithinRange(0, 36600);
        if (days > maxDays)
            throw ServiceException.INVALID_REQUEST("Requested range is too large (Maximum " + maxDays + " days)", null);

        TimeZoneMap tzmap = new TimeZoneMap(Util.getAccountTimeZone(authAcct));
        ParsedRecurrence parsed = parseRecur(request, tzmap);
        List<Instance> instances = getInstances(parsed, rangeStart, rangeEnd);
        Element response = getResponseElement(zsc);
        if (instances != null) {
            for (Instance inst : instances) {
                addInstance(response, inst);
            }
        }
        return response;
    }

    protected static class ParsedRecurrence {
        public RecurrenceRule rrule;
        public List<IException> exceptions;  // set only when rrule == null
    }

    protected static ParsedRecurrence parseRecur(Element request, TimeZoneMap tzmap) throws ServiceException {
        CalendarUtils.parseTimeZones(request, tzmap);
        IRecurrence recurrence = null;
        List<IException> exceptions = new ArrayList<IException>();
        for (Iterator<Element> compIter = request.elementIterator(); compIter.hasNext();) {
            Element elem = compIter.next();
            String elemName = elem.getName();
            boolean isCancel = false;
            if (MailConstants.E_CAL_CANCEL.equals(elemName)) {
                isCancel = true;
            } else if (!MailConstants.E_INVITE_COMPONENT.equals(elemName) && !MailConstants.E_CAL_EXCEPT.equals(elemName)) {
                continue;
            }

            RecurId recurId = null;
            Element recurIdElem = elem.getOptionalElement(MailConstants.E_CAL_EXCEPTION_ID);
            if (recurIdElem != null)
                recurId = CalendarUtils.parseRecurId(recurIdElem, tzmap);
            if (!isCancel) {
                ParsedDateTime dtStart = null;
                Element dtStartElem = elem.getElement(MailConstants.E_CAL_START_TIME);
                dtStart = CalendarUtils.parseDateTime(dtStartElem, tzmap);
                ParsedDateTime dtEnd = null;
                Element dtEndElem = elem.getOptionalElement(MailConstants.E_CAL_END_TIME);
                if (dtEndElem != null)
                    dtEnd = CalendarUtils.parseDateTime(dtEndElem, tzmap);
                ParsedDuration dur = null;
                Element durElem = elem.getOptionalElement(MailConstants.E_CAL_DURATION);
                if (durElem != null)
                    dur = ParsedDuration.parse(durElem);
                if (dtEnd == null && dur == null)
                    throw ServiceException.INVALID_REQUEST(
                            "Must specify either " + MailConstants.E_CAL_END_TIME + " or " + MailConstants.E_CAL_DURATION +
                            " in " + elemName, null);
                Element recurElem = elem.getOptionalElement(MailConstants.E_CAL_RECUR);
                if (recurElem != null) {
                    // series with a rule
                    recurrence = CalendarUtils.parseRecur(recurElem, tzmap, dtStart, dtEnd, dur, recurId);
                } else {
                    // modified instance, or it has no rule and no recurrence-id
                    if (dur == null && dtStart != null && dtEnd != null)
                        dur = dtEnd.difference(dtStart);
                    if (recurId == null)
                        recurId = new RecurId(dtStart, RecurId.RANGE_NONE);
                    exceptions.add(new ExceptionRule(recurId, dtStart, dur, null));
                }
            } else if (recurId != null) {
                // canceled instance
                exceptions.add(new CancellationRule(recurId));
            }
        }
        
        ParsedRecurrence parsed = new ParsedRecurrence();
        if (recurrence instanceof RecurrenceRule) {
            RecurrenceRule rrule = (RecurrenceRule) recurrence;
            for (IException exception : exceptions) {
                rrule.addException(exception);
            }
            parsed.rrule = rrule;
        } else {
            parsed.exceptions = exceptions;
        }
        return parsed;
    }

    protected static List<Instance> getInstances(ParsedRecurrence parsed, long rangeStart, long rangeEnd)
    throws ServiceException {
        List<Instance> instances = null;
        if (parsed.rrule != null) {
            instances = parsed.rrule.expandInstances(0, rangeStart, rangeEnd);
        } else if (parsed.exceptions != null && !parsed.exceptions.isEmpty()) {
            instances = new ArrayList<Instance>(parsed.exceptions.size());
            for (IException except : parsed.exceptions) {
                if (except instanceof CancellationRule)
                    continue;  // Skip canceled instances.
                ParsedDateTime dtStart = except.getStartTime();
                long invStart = dtStart != null ? dtStart.getUtcTime() : 0;
                ParsedDateTime dtEnd = except.getEndTime();
                long invEnd = dtEnd != null ? dtEnd.getUtcTime() : 0;
                if ((invStart < rangeEnd && invEnd > rangeStart) || (dtStart == null)) {
                    boolean allDay = false;
                    int startTzo = 0, endTzo = 0;
                    if (dtStart != null) {
                        allDay = !dtStart.hasTime();
                        startTzo = dtStart.getOffset();
                    }
                    if (dtEnd != null)
                        endTzo = dtEnd.getOffset();
                    Instance inst = new Instance(0, null,
                                                 dtStart != null, dtEnd != null,
                                                 invStart, invEnd, allDay, startTzo, endTzo,
                                                 true, false);
                    instances.add(inst);
                }
            }
        }
        return instances;
    }

    protected static Element addInstance(Element parent, Instance inst) throws ServiceException {
        Element instElem = parent.addElement(MailConstants.E_INSTANCE);
        if (inst.hasStart() && inst.hasEnd()) {
            long instStart = inst.getStart();
            instElem.addAttribute(MailConstants.A_CAL_START_TIME, instStart);
            instElem.addAttribute(MailConstants.A_CAL_NEW_DURATION, inst.getEnd() - inst.getStart());
            if (inst.isAllDay()) {
                instElem.addAttribute(MailConstants.A_CAL_ALLDAY, true);
                instElem.addAttribute(MailConstants.A_CAL_TZ_OFFSET, inst.getStartTzOffset());
            }
            instElem.addAttribute(MailConstants.A_CAL_RECURRENCE_ID_Z, inst.getRecurIdZ());
        }
        return instElem;
    }
}
