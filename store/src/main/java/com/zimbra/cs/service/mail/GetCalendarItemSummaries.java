// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Feb 17, 2005
 */
package com.zimbra.cs.service.mail;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.calendar.Geo;
import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.calendar.ParsedDuration;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.CalendarItem.AlarmData;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Task;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.InviteInfo;
import com.zimbra.cs.mailbox.calendar.RecurId;
import com.zimbra.cs.mailbox.calendar.ZOrganizer;
import com.zimbra.cs.mailbox.calendar.cache.CacheToXML;
import com.zimbra.cs.mailbox.calendar.cache.CalSummaryCache;
import com.zimbra.cs.mailbox.calendar.cache.CalSummaryCache.CalendarDataResult;
import com.zimbra.cs.mailbox.calendar.cache.CalendarItemData;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;


/**
 * @author tim
 */
public class GetCalendarItemSummaries extends CalendarRequest {

    private static Log mLog = LogFactory.getLog(GetCalendarItemSummaries.class);

    private static final String[] TARGET_FOLDER_PATH = new String[] { MailConstants.A_FOLDER };
    private static final String[] RESPONSE_ITEM_PATH = new String[] { };

    @Override
    protected String[] getProxiedIdPath(Element request) {
        return TARGET_FOLDER_PATH;
    }

    @Override
    protected boolean checkMountpointProxy(Element request) {
        return true;
    }

    @Override
    protected String[] getResponseItemPath() {
        return RESPONSE_ITEM_PATH;
    }

    private static final String DEFAULT_FOLDER = "" + Mailbox.ID_AUTO_INCREMENT;

    private static final long MSEC_PER_DAY = 1000*60*60*24;
    private static final long MAX_PERIOD_SIZE_IN_DAYS = 200;

    static class EncodeCalendarItemResult {
        Element element;
        int numInstancesExpanded;
    }

    /**
     * Encodes a calendar item
     *
     * @param parent
     * @param elementName
     *         name of element to add (MailConstants .E_APPOINTMENT or MailConstants.E_TASK)
     * @param rangeStart
     *         start period to expand instances (or -1 for no start time constraint)
     * @param rangeEnd
     *         end period to expand instances (or -1 for no end time constraint)
     * @param newFormat
     *         temporary HACK - true: SearchRequest, false: GetAppointmentSummaries
     * @return
     */
    static EncodeCalendarItemResult encodeCalendarItemInstances(
            ZimbraSoapContext lc, OperationContext octxt, CalendarItem calItem,
            Account acct, long rangeStart, long rangeEnd, boolean newFormat)
    throws ServiceException {
        EncodeCalendarItemResult toRet = new EncodeCalendarItemResult();
        ItemIdFormatter ifmt = new ItemIdFormatter(lc);
        Account authAccount = getAuthenticatedAccount(lc);
        boolean hidePrivate = !calItem.allowPrivateAccess(authAccount, lc.isUsingAdminPrivileges());

        try {
            boolean expandRanges;
            if (calItem instanceof Task) {
                expandRanges = true;
                if (rangeStart == -1 && rangeEnd == -1) {
                    rangeStart = Long.MIN_VALUE;
                    rangeEnd = Long.MAX_VALUE;
                }
            } else {
                expandRanges = (rangeStart != -1 && rangeEnd != -1 && rangeStart < rangeEnd);
            }

            boolean isAppointment = calItem instanceof Appointment;

            // Use the marshalling code in calendar summary cache for uniform output, when we can.
            if (isAppointment && expandRanges) {
                CalendarItemData calItemData = CalSummaryCache.reloadCalendarItemOverRange(calItem, rangeStart, rangeEnd);
                if (calItemData != null) {
                    int numInstances = calItemData.getNumInstances();
                    if (numInstances > 0) {
                        Element calItemElem = CacheToXML.encodeCalendarItemData(lc, ifmt, calItemData, !hidePrivate, !newFormat);
                        toRet.element = calItemElem;
                        toRet.numInstancesExpanded = numInstances;
                    }
                }
                return toRet;
            }
            // But there are other cases (e.g. tasks, no time range) that require the legacy code below.

            Element calItemElem = null; // don't initialize until we find at least one valid instance

            Invite defaultInvite = calItem.getDefaultInviteOrNull();

            if (defaultInvite == null) {
                mLog.info("Could not load defaultinfo for calendar item with id="+calItem.getId()+" SKIPPING");
                return toRet;
            }

            ParsedDuration defDuration = defaultInvite.getEffectiveDuration();
            // Duration is null if no DTEND or DURATION was present.  Assume 1 day for all-day
            // events and 1 second for non all-day.  (bug 28615)
            if (defDuration == null && !defaultInvite.isTodo()) {
                if (defaultInvite.isAllDayEvent()) {
                    defDuration = ParsedDuration.ONE_DAY;
                } else {
                    defDuration = ParsedDuration.ONE_SECOND;
                }
            }
            long defDurationMsecs = 0;
            if (defaultInvite.getStartTime() != null && defDuration != null) {
                ParsedDateTime s = defaultInvite.getStartTime();
                long et = s.add(defDuration).getUtcTime();
                defDurationMsecs = et - s.getUtcTime();
            }

            String defaultFba = null;
            if (calItem instanceof Appointment) {
                defaultFba = ((Appointment) calItem).getEffectiveFreeBusyActual(defaultInvite, null);
            }

            String defaultPtSt = calItem.getEffectivePartStat(defaultInvite, null);

            AlarmData alarmData = calItem.getAlarmData();

            // add all the instances:
            int numInRange = 0;

            if (expandRanges) {
                Collection<CalendarItem.Instance> instances = calItem.expandInstances(rangeStart, rangeEnd, true);
                long alarmTime = 0;
                long alarmInst = 0;
                if (alarmData != null) {
                    alarmTime = alarmData.getNextAt();
                    alarmInst = alarmData.getNextInstanceStart();
                }
                for (CalendarItem.Instance inst : instances) {
                    try {
                        InviteInfo invId = inst.getInviteInfo();
                        Invite inv = calItem.getInvite(invId.getMsgId(), invId.getComponentId());
                        boolean showAll = !hidePrivate || inv.isPublic();

                        // figure out which fields are different from the default and put their data here...

                        ParsedDuration invDuration = inv.getEffectiveDuration();
                        long instStart = inst.getStart();
                        // For an instance whose alarm time is within the time range, we must
                        // include it even if its start time is after the range.
                        long startOrAlarm = instStart == alarmInst ? alarmTime : instStart;

                        // Duration is null if no DTEND or DURATION was present.  Assume 1 day for all-day
                        // events and 1 second for non all-day.  (bug 28615)
                        if (invDuration == null) {
                            if (inv.isAllDayEvent())
                                invDuration = ParsedDuration.ONE_DAY;
                            else
                                invDuration = ParsedDuration.ONE_SECOND;
                        }
                        if (!inst.hasStart() ||
                            (startOrAlarm < rangeEnd && invDuration.addToTime(instStart) > rangeStart)) {
                            numInRange++;
                        } else {
                            continue;
                        }

                        if (calItemElem == null) {
                            calItemElem = lc.createElement(isAppointment ? MailConstants.E_APPOINTMENT : MailConstants.E_TASK);

                            if (showAll) {
                                // flags and tags
                                ToXML.recordItemTags(calItemElem, calItem, octxt);
                            }

                            // Organizer
                            if (inv.hasOrganizer()) {
                                ZOrganizer org = inv.getOrganizer();
                                org.toXml(calItemElem);
                            }

                            calItemElem.addAttribute("x_uid", calItem.getUid());
                            calItemElem.addAttribute(MailConstants.A_UID, calItem.getUid());
                        }

                        Element instElt = calItemElem.addElement(MailConstants.E_INSTANCE);

                        if (showAll) {
                            if (isAppointment && inv.isEvent()) {
                                String instFba = ((Appointment) calItem).getEffectiveFreeBusyActual(inv, inst);
                                if (instFba != null && (!instFba.equals(defaultFba) || inst.isException()))
                                    instElt.addAttribute(MailConstants.A_APPT_FREEBUSY_ACTUAL, instFba);
                            }
                            String instPtSt = calItem.getEffectivePartStat(inv, inst);
                            if (!defaultPtSt.equals(instPtSt) || inst.isException())
                                instElt.addAttribute(MailConstants.A_CAL_PARTSTAT, instPtSt);
                        }

                        if (inst.hasStart()) {
                            instElt.addAttribute(MailConstants.A_CAL_START_TIME, instStart);
                            if (inv.isAllDayEvent())
                                instElt.addAttribute(MailConstants.A_CAL_TZ_OFFSET, inst.getStartTzOffset());
                        }


                        if (inst.isException() && inv.hasRecurId()) {
                            RecurId rid = inv.getRecurId();
                            instElt.addAttribute(MailConstants.A_CAL_RECURRENCE_ID_Z, rid.getDtZ());
                        } else {
                            instElt.addAttribute(MailConstants.A_CAL_RECURRENCE_ID_Z, inst.getRecurIdZ());
                        }

                        if (inst.isException()) {

                            instElt.addAttribute(MailConstants.A_CAL_IS_EXCEPTION, true);

                            instElt.addAttribute(MailConstants.A_CAL_INV_ID, ifmt.formatItemId(calItem, inst.getMailItemId()));
                            instElt.addAttribute(MailConstants.A_CAL_COMPONENT_NUM, inst.getComponentNum());

                            if (showAll) {
                                // fragment has already been sanitized...
                                String frag = inv.getFragment();
                                if (frag != null && !frag.equals(""))
                                    instElt.addAttribute(MailConstants.E_FRAG, frag, Element.Disposition.CONTENT);

                                if (inv.getPriority() != null)
                                    instElt.addAttribute(MailConstants.A_CAL_PRIORITY, inv.getPriority());

                                if (inv.isEvent()) {
                                    if (inv.getFreeBusy() != null)
                                        instElt.addAttribute(MailConstants.A_APPT_FREEBUSY, inv.getFreeBusy());
                                    if (inv.getTransparency() != null)
                                        instElt.addAttribute(MailConstants.A_APPT_TRANSPARENCY, inv.getTransparency());
                                }

                                if (inv.isTodo()) {
                                    if (inv.getPercentComplete() != null)
                                        instElt.addAttribute(MailConstants.A_TASK_PERCENT_COMPLETE, inv.getPercentComplete());
                                }

                                if (inv.getName() != null)
                                    instElt.addAttribute(MailConstants.A_NAME, inv.getName());

                                if (inv.getLocation() != null)
                                    instElt.addAttribute(MailConstants.A_CAL_LOCATION, inv.getLocation());

                                List<String> categories = inv.getCategories();
                                if (categories != null) {
                                    for (String cat : categories) {
                                        instElt.addElement(MailConstants.E_CAL_CATEGORY).setText(cat);
                                    }
                                }
                                Geo geo = inv.getGeo();
                                if (geo != null)
                                    geo.toXml(instElt);

                                if (inv.hasOtherAttendees())
                                    instElt.addAttribute(MailConstants.A_CAL_OTHER_ATTENDEES, true);

                                if (inv.hasAlarm())
                                    instElt.addAttribute(MailConstants.A_CAL_ALARM, true);
                            }

                            instElt.addAttribute(MailConstants.A_CAL_ISORG, inv.isOrganizer());

                            if (inv.isTodo()) {
                                if (inst.hasEnd()) {
                                    instElt.addAttribute(MailConstants.A_TASK_DUE_DATE, inst.getEnd());
                                    if (inv.isAllDayEvent())
                                        instElt.addAttribute(MailConstants.A_CAL_TZ_OFFSET_DUE, inst.getEndTzOffset());
                                }
                            } else {
                                if (inst.hasStart() && inst.hasEnd()) {
                                    instElt.addAttribute(
                                            newFormat ? MailConstants.A_CAL_NEW_DURATION : MailConstants.A_CAL_DURATION,
                                            inst.getEnd() - inst.getStart());
                                }
                            }

                            if (inv.getStatus() != null)
                                instElt.addAttribute(MailConstants.A_CAL_STATUS, inv.getStatus());

                            if (inv.getClassProp() != null)
                                instElt.addAttribute(MailConstants.A_CAL_CLASS, inv.getClassProp());

                            if (inv.isAllDayEvent())
                                instElt.addAttribute(MailConstants.A_CAL_ALLDAY, true);
                            if (inv.isDraft())
                                instElt.addAttribute(MailConstants.A_CAL_DRAFT, true);
                            if (inv.isNeverSent())
                                instElt.addAttribute(MailConstants.A_CAL_NEVER_SENT, true);
                            if (inv.isRecurrence())
                                instElt.addAttribute(MailConstants.A_CAL_RECUR, true);
                        } else {
                            if (inv.isTodo()) {
                                if (inst.hasEnd()) {
                                    instElt.addAttribute(MailConstants.A_TASK_DUE_DATE, inst.getEnd());
                                    if (inv.isAllDayEvent())
                                        instElt.addAttribute(MailConstants.A_CAL_TZ_OFFSET_DUE, inst.getEndTzOffset());
                                }
                            } else {
                                // A non-exception instance can have duration that is different from
                                // the default duration due to daylight savings time transitions.
                                if (inst.hasStart() && inst.hasEnd() && defDurationMsecs != inst.getEnd()-inst.getStart()) {
                                    instElt.addAttribute(newFormat ? MailConstants.A_CAL_NEW_DURATION : MailConstants.A_CAL_DURATION, inst.getEnd()-inst.getStart());
                                }
                            }
                        }
                    } catch (MailServiceException.NoSuchItemException e) {
                        mLog.info("Error could not get instance "+inst.getMailItemId()+"-"+inst.getComponentNum()+
                            " for appt "+calItem.getId(), e);
                    }
                } // iterate all the instances
            } // if expandRanges


            if (!expandRanges || numInRange > 0) { // if we found any calItems at all, we have to encode the "Default" data here
                boolean showAll = !hidePrivate || defaultInvite.isPublic();
                if (calItemElem == null) {
                    calItemElem = lc.createElement(isAppointment ? MailConstants.E_APPOINTMENT : MailConstants.E_TASK);

                    calItemElem.addAttribute("x_uid", calItem.getUid());
                    calItemElem.addAttribute(MailConstants.A_UID, calItem.getUid());

                    if (showAll) {
                        // flags and tags
                        ToXML.recordItemTags(calItemElem, calItem, octxt);
                    }

                    // Organizer
                    if (defaultInvite.hasOrganizer()) {
                        ZOrganizer org = defaultInvite.getOrganizer();
                        org.toXml(calItemElem);
                    }
                }

                if (showAll) {
                    if (alarmData != null)
                        ToXML.encodeAlarmData(calItemElem, calItem, alarmData);

                    String defaultPriority = defaultInvite.getPriority();
                    if (defaultPriority != null)
                        calItemElem.addAttribute(MailConstants.A_CAL_PRIORITY, defaultPriority);
                    calItemElem.addAttribute(MailConstants.A_CAL_PARTSTAT, defaultPtSt);
                    if (defaultInvite.isEvent()) {
                        calItemElem.addAttribute(MailConstants.A_APPT_FREEBUSY, defaultInvite.getFreeBusy());
                        calItemElem.addAttribute(MailConstants.A_APPT_FREEBUSY_ACTUAL, defaultFba);
                        calItemElem.addAttribute(MailConstants.A_APPT_TRANSPARENCY, defaultInvite.getTransparency());
                    }
                    if (defaultInvite.isTodo()) {
                        String pctComplete = defaultInvite.getPercentComplete();
                        if (pctComplete != null)
                            calItemElem.addAttribute(MailConstants.A_TASK_PERCENT_COMPLETE, pctComplete);
                    }

                    calItemElem.addAttribute(MailConstants.A_NAME, defaultInvite.getName());
                    calItemElem.addAttribute(MailConstants.A_CAL_LOCATION, defaultInvite.getLocation());

                    List<String> categories = defaultInvite.getCategories();
                    if (categories != null) {
                        for (String cat : categories) {
                            calItemElem.addElement(MailConstants.E_CAL_CATEGORY).setText(cat);
                        }
                    }
                    Geo geo = defaultInvite.getGeo();
                    if (geo != null)
                        geo.toXml(calItemElem);

                    // fragment has already been sanitized...
                    String fragment = defaultInvite.getFragment();
                    if (!fragment.equals(""))
                        calItemElem.addAttribute(MailConstants.E_FRAG, fragment, Element.Disposition.CONTENT);

                    if (defaultInvite.hasOtherAttendees()) {
                        calItemElem.addAttribute(MailConstants.A_CAL_OTHER_ATTENDEES, defaultInvite.hasOtherAttendees());
                    }
                    if (defaultInvite.hasAlarm()) {
                        calItemElem.addAttribute(MailConstants.A_CAL_ALARM, defaultInvite.hasAlarm());
                    }
                }

                calItemElem.addAttribute(MailConstants.A_CAL_ISORG, defaultInvite.isOrganizer());
                calItemElem.addAttribute(MailConstants.A_ID, ifmt.formatItemId(calItem));
                calItemElem.addAttribute(MailConstants.A_CAL_INV_ID,
                        ifmt.formatItemId(calItem, defaultInvite.getMailItemId()));
                calItemElem.addAttribute(MailConstants.A_CAL_COMPONENT_NUM, defaultInvite.getComponentNum());
                calItemElem.addAttribute(MailConstants.A_FOLDER,
                        ifmt.formatItemId(new ItemId(calItem.getMailbox().getAccountId(), calItem.getFolderId())));

                calItemElem.addAttribute(MailConstants.A_CAL_STATUS, defaultInvite.getStatus());
                calItemElem.addAttribute(MailConstants.A_CAL_CLASS, defaultInvite.getClassProp());
                if (!defaultInvite.isTodo())
                    calItemElem.addAttribute(newFormat ? MailConstants.A_CAL_NEW_DURATION : MailConstants.A_CAL_DURATION, defDurationMsecs);
                if (defaultInvite.isAllDayEvent())
                    calItemElem.addAttribute(MailConstants.A_CAL_ALLDAY, defaultInvite.isAllDayEvent());
                if (defaultInvite.isDraft())
                    calItemElem.addAttribute(MailConstants.A_CAL_DRAFT, defaultInvite.isDraft());
                if (defaultInvite.isNeverSent())
                    calItemElem.addAttribute(MailConstants.A_CAL_NEVER_SENT, defaultInvite.isNeverSent());
                if (defaultInvite.isRecurrence())
                    calItemElem.addAttribute(MailConstants.A_CAL_RECUR, defaultInvite.isRecurrence());
                if (calItem.hasExceptions()) {
                    calItemElem.addAttribute(MailConstants.A_CAL_HAS_EXCEPTIONS, true);
                }

                toRet.element = calItemElem;
            }
            toRet.numInstancesExpanded = numInRange;
        } catch(MailServiceException.NoSuchItemException e) {
            mLog.info("Error could not get default invite for calendar item: "+ calItem.getId(), e);
        } catch (RuntimeException e) {
            mLog.info("Caught Exception "+e+ " while getting summary info for calendar item: "+calItem.getId(), e);
        }

        return toRet;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        Account acct = getRequestedAccount(zsc);

        long rangeStart = request.getAttributeLong(MailConstants.A_CAL_START_TIME);
        long rangeEnd = request.getAttributeLong(MailConstants.A_CAL_END_TIME);

        if (rangeEnd < rangeStart) {
            throw ServiceException.INVALID_REQUEST("End time must be after Start time", null);
        }

        long days = (rangeEnd-rangeStart)/MSEC_PER_DAY;
        if (days > MAX_PERIOD_SIZE_IN_DAYS) {
            throw ServiceException.INVALID_REQUEST("Requested range is too large (Maximum "+MAX_PERIOD_SIZE_IN_DAYS+" days)", null);
        }


        ItemId iidFolder = new ItemId(request.getAttribute(MailConstants.A_FOLDER, DEFAULT_FOLDER), zsc);

        Element response = getResponseElement(zsc);

        OperationContext octxt = getOperationContext(zsc, context);

        if (LC.calendar_cache_enabled.booleanValue()) {
            ItemIdFormatter ifmt = new ItemIdFormatter(zsc);
            int folderId = iidFolder.getId();
            if (folderId != Mailbox.ID_AUTO_INCREMENT) {
                CalendarDataResult result = mbox.getCalendarSummaryForRange(
                        octxt, folderId, getItemType(), rangeStart, rangeEnd);
                if (result != null) {
    	            for (Iterator<CalendarItemData> itemIter = result.data.calendarItemIterator(); itemIter.hasNext(); ) {
    	            	CalendarItemData calItemData = itemIter.next();
                        int numInstances = calItemData.getNumInstances();
                        if (numInstances > 0) {
                            Element calItemElem = CacheToXML.encodeCalendarItemData(
                                    zsc, ifmt, calItemData, result.allowPrivateAccess, true);
                            response.addElement(calItemElem);
                        }
    	            }
                }
            } else {
                List<CalendarDataResult> calDataResultList = mbox.getAllCalendarsSummaryForRange(
                        octxt, getItemType(), rangeStart, rangeEnd);
                for (CalendarDataResult result : calDataResultList) {
                    for (Iterator<CalendarItemData> itemIter = result.data.calendarItemIterator(); itemIter.hasNext(); ) {
                        CalendarItemData calItemData = itemIter.next();
                        int numInstances = calItemData.getNumInstances();
                        if (numInstances > 0) {
                            Element calItemElem = CacheToXML.encodeCalendarItemData(
                                    zsc, ifmt, calItemData, result.allowPrivateAccess, true);
                            response.addElement(calItemElem);
                        }
                    }
                }
            }
        } else {
	        Collection<CalendarItem> calItems = mbox.getCalendarItemsForRange(
	                octxt, getItemType(), rangeStart, rangeEnd, iidFolder.getId(), null);
	        for (CalendarItem calItem : calItems) {
	            EncodeCalendarItemResult encoded = encodeCalendarItemInstances(
	                    zsc, octxt, calItem, acct, rangeStart, rangeEnd, false);
	            if (encoded.element != null)
	                response.addElement(encoded.element);
	        }
        }

        return response;
    }
}
