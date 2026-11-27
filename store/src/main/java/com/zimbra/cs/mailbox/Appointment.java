// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.cs.account.Provisioning;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.MimeMessage;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.mailbox.AppointmentAvailability.Conflict;
import com.zimbra.cs.mailbox.AppointmentAvailability.ConflictCheckResult;
import com.zimbra.cs.mailbox.calendar.CalendarMailSender;
import com.zimbra.cs.mailbox.calendar.IcalXmlStrMap;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.InviteInfo;
import com.zimbra.cs.mailbox.calendar.RecurId;
import com.zimbra.cs.mailbox.calendar.Util;
import com.zimbra.cs.mailbox.calendar.ZAttendee;
import com.zimbra.cs.mailbox.calendar.CalendarMailSender.Verb;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.redolog.op.CreateCalendarItemPlayer;
import com.zimbra.cs.redolog.op.CreateCalendarItemRecorder;
import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;


/**
 * An APPOINTMENT consists of one or more INVITES in the same series -- ie that
 * have the same UID. From the appointment you can get the INSTANCES which are
 * the start/end times of each occurence.
 *
 * Sample Appointment: APPOINTMENT UID=1234 (two INVITES above) ...Instances on
 * every monday with name "Gorilla Discussion" EXCEPT for the 21st, where we
 * talk about lefties instead. CANCELED for the 28th
 *
 * @since Feb 17, 2005
 */
public final class Appointment extends CalendarItem {

    public Appointment(Mailbox mbox, UnderlyingData data) throws ServiceException {
        this(mbox, data, false);
    }
    
    public Appointment(Mailbox mbox, UnderlyingData data, boolean skipCache) throws ServiceException {
        super(mbox, data, skipCache);
        if (mData.type != Type.APPOINTMENT.toByte()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return this accounts "effective" FBA data -- ie the FBA that is the result of the most recent and
     * most specific (specific b/c some replies might be for just one instance, some might be for recurrence-id=0,
     * etc) given the requested Invite and Instance to check against.
     *
     * For example, imagine an appt with no exceptions, but two replies:
     *    RECUR=0, REPLY=accept (reply to the default invite, accept it)
     *    RECUR=20051010 REPLY=decline (reply to DECLINE the instance on 10/10/2005
     *
     * The FBA for the 10/10 instance will obviously be different than the one for any other instance.  If you
     * add Exceptions into the mix, then there are even more permutations.
     *
     * @param inv
     * @param inst
     * @return
     * @throws ServiceException
     */
    public String getEffectiveFreeBusyActual(Invite inv, Instance inst) throws ServiceException {
        if (!inv.isOrganizer()) {
            Mailbox mailbox = getMailbox();
            ZAttendee at = getReplyList().getEffectiveAttendee(
                Provisioning.getInstance().getAccountByMailbox(mailbox), inv, inst);
            if (at != null) {
                if (at.hasPartStat())
                    return inv.partStatToFreeBusyActual(at.getPartStat());
                else
                    return inv.getFreeBusyActual();
            }
        }
        return inv.getFreeBusyActual();
    }

    @Override
    protected String processPartStat(Invite invite, MimeMessage mmInv, boolean forCreate, String defaultPartStat)
            throws ServiceException {
        Mailbox mbox = getMailbox();
        OperationContext octxt = mbox.getOperationContext();
        CreateCalendarItemPlayer player =
            octxt != null ? (CreateCalendarItemPlayer) octxt.getPlayer() : null;
        long opTime = octxt != null ? octxt.getTimestamp() : System.currentTimeMillis();

        Mailbox mailbox = getMailbox();
        Account account = Provisioning.getInstance().getAccountByMailbox(mailbox);
        boolean onBehalfOf = false;
        Account authAcct = account;
        if (octxt != null) {
            Account authuser = octxt.getAuthenticatedUser();
            if (authuser != null) {
                onBehalfOf = !account.getId().equalsIgnoreCase(authuser.getId());
                if (onBehalfOf)
                    authAcct = authuser;
            }
        }
        boolean asAdmin = octxt != null ? octxt.isUsingAdminPrivileges() : false;
        boolean allowPrivateAccess = allowPrivateAccess(authAcct, asAdmin);

        String partStat = defaultPartStat;
        if (player != null) {
            String p = player.getCalendarItemPartStat();
            if (p != null) partStat = p;
        }

        // See if we have RSVP=FALSE for the attendee.  Let's assume RSVP was requested unless it is
        // explicitly set to FALSE.
        boolean rsvpRequested = true;
        ZAttendee attendee = invite.getMatchingAttendee(account);
        if (attendee != null) {
            Boolean rsvp = attendee.getRsvp();
            if (rsvp != null)
                rsvpRequested = rsvp;
        }
        RedoLogProvider redoProvider = RedoLogProvider.getInstance();
        // Don't send reply emails if we're not on master (in redo-driven master/replica setup).
        // Don't send reply emails if we're replaying redo for reasons other than crash recovery.
        // In other words, we DO want to send emails during crash recovery, because we're completing
        // an interrupted transaction.  But we don't send emails during redo reply phase of restore.
        // Also don't send emails for cancel invites.  (Organizer doesn't expect reply for cancels.)
        // And don't send emails for task requests.
        // Don't send reply emails from a system account. (e.g. archiving, galsync, ham/spam)
        boolean needReplyEmail =
            rsvpRequested &&
            redoProvider.isMaster() &&
            (player == null || redoProvider.getRedoLogManager().getInCrashRecovery()) &&
            invite.hasOrganizer() &&
            !invite.isCancel() &&
            !invite.isTodo() &&
            !account.isIsSystemResource();

        if (invite.isOrganizer()) {
            // Organizer always accepts.
            partStat = IcalXmlStrMap.PARTSTAT_ACCEPTED;
        } else if (account instanceof CalendarResource && octxt == null) {
            // Auto accept/decline processing should only occur during email delivery.  In particular,
            // don't do it if we're here during ics import.  We're in email delivery if octxt == null.
            // (There needs to be a better way to determine that...)

            boolean replySent = false;
            CalendarResource resource = (CalendarResource) account;
            Locale lc;
            Account organizer = invite.getOrganizerAccount();
            if (organizer != null)
                lc = organizer.getLocale();
            else
                lc = resource.getLocale();
            if (resource.autoAcceptDecline() || resource.autoDeclineIfBusy() || resource.autoDeclineRecurring()) {
                boolean replyListUpdated = false;
                // We'll accept unless one of the checks below fails.
                // If auto-accept is enabled, assume it'll be accepted until it gets declined.
                if (resource.autoAcceptDecline())
                    partStat = IcalXmlStrMap.PARTSTAT_ACCEPTED;
                if (isRecurring() && resource.autoDeclineRecurring()) {
                    // Decline because resource is configured to decline all recurring appointments.
                    partStat = IcalXmlStrMap.PARTSTAT_DECLINED;
                    if (needReplyEmail) {
                        String reason =
                            L10nUtil.getMessage(MsgKey.calendarResourceDeclineReasonRecurring, lc);
                        Invite replyInv = makeReplyInvite(
                                account, authAcct, lc, onBehalfOf, allowPrivateAccess, invite, invite.getRecurId(),
                                CalendarMailSender.VERB_DECLINE);
                        CalendarMailSender.sendResourceAutoReply(
                                octxt, mbox, true,
                                CalendarMailSender.VERB_DECLINE, false,
                                reason + "\r\n",
                                this, invite, new Invite[] { replyInv }, mmInv);
                        replySent = true;
                    }
                } else if (resource.autoDeclineIfBusy()) {
                    // Auto decline is enabled.  Let's check for conflicts.
                    int maxNumConflicts = resource.getMaxNumConflictsAllowed();
                    int maxPctConflicts = resource.getMaxPercentConflictsAllowed();
                    ConflictCheckResult checkResult =
                        new AppointmentAvailability(this).checkAvailability(opTime, invite, maxNumConflicts, maxPctConflicts);
                    if (checkResult != null) {
                        List<Conflict> conflicts = checkResult.getConflicts();
                        if (conflicts.size() > 0) {
                            if (invite.isRecurrence() && !checkResult.tooManyConflicts()) {
                                // There are some conflicts, but within resource's allowed limit.
                                if (resource.autoAcceptDecline()) {
                                    // Let's accept partially.  (Accept the series and decline conflicting instances.)
                                    List<Invite> replyInvites = new ArrayList<>();
                                    // the REPLY for the ACCEPT of recurrence series
                                    Invite acceptInv = makeReplyInvite(
                                            account, authAcct, lc, onBehalfOf, allowPrivateAccess, invite, invite.getRecurId(),
                                            CalendarMailSender.VERB_ACCEPT);

                                    for (Conflict conflict : conflicts) {
                                        Instance inst = conflict.getInstance();
                                        InviteInfo invInfo = inst.getInviteInfo();
                                        Invite inv = getInvite(invInfo.getMsgId(), invInfo.getComponentId());
                                        RecurId rid = inst.makeRecurId(inv);

                                        // Record the decline status in reply list.
                                        getReplyList().modifyPartStat(
                                                resource, rid, null, resource.getName(), null, null,
                                                IcalXmlStrMap.PARTSTAT_DECLINED, false, invite.getSeqNo(), opTime);
                                        replyListUpdated = true;

                                        // Make REPLY VEVENT for the declined instance.
                                        Invite replyInv = makeReplyInvite(
                                                account, authAcct, lc, onBehalfOf, allowPrivateAccess, inv, rid,
                                                CalendarMailSender.VERB_DECLINE);
                                        replyInvites.add(replyInv);
                                    }

                                    if (needReplyEmail) {
                                        ICalTimeZone tz = chooseReplyTZ(invite);
                                        // Send one email to accept the series.
                                        String declinedInstances = AppointmentAvailability.getDeclinedTimesString(
                                                conflicts, invite.isAllDayEvent(), tz, lc);
                                        String msg =
                                            L10nUtil.getMessage(MsgKey.calendarResourceDeclinedInstances, lc) +
                                            "\r\n\r\n" + declinedInstances;
                                        CalendarMailSender.sendResourceAutoReply(
                                                octxt, mbox, true,
                                                CalendarMailSender.VERB_ACCEPT, true,
                                                msg,
                                                this, invite, new Invite[] { acceptInv }, mmInv);
                                        // Send another email to decline instances, all in one email.
                                        String conflictingTimes = AppointmentAvailability.getBusyTimesString(octxt, mbox, conflicts, tz, lc, false);
                                        msg =
                                            L10nUtil.getMessage(MsgKey.calendarResourceDeclinedInstances, lc) +
                                            "\r\n\r\n" + declinedInstances + "\r\n" +
                                            L10nUtil.getMessage(MsgKey.calendarResourceDeclineReasonConflict, lc) +
                                            "\r\n\r\n" + conflictingTimes;
                                        CalendarMailSender.sendResourceAutoReply(
                                                octxt, mbox, true,
                                                CalendarMailSender.VERB_DECLINE, true,
                                                msg,
                                                this, invite, replyInvites.toArray(new Invite[0]), mmInv);
                                        replySent = true;
                                    }
                                } else {
                                    // Auto-accept is not enabled.  Auto-decline is enabled, but there weren't
                                    // enough conflicting instances to decline outright.  So we just stay
                                    // silent and let the human admin deal with it.  This case is rather
                                    // ambiguous, and can be avoided by configuring the resource to allow
                                    // zero conflicting instance.
                                }
                            } else {
                                // Too many conflicts.  Decline outright.
                                partStat = IcalXmlStrMap.PARTSTAT_DECLINED;
                                if (needReplyEmail) {
                                    ICalTimeZone tz = chooseReplyTZ(invite);
                                    String msg =
                                        L10nUtil.getMessage(MsgKey.calendarResourceDeclineReasonConflict, lc) +
                                        "\r\n\r\n" +
                                        AppointmentAvailability.getBusyTimesString(octxt, mbox, conflicts, tz, lc, checkResult.hasMoreConflicts());
                                    Invite replyInv = makeReplyInvite(
                                            account, authAcct, lc, onBehalfOf, allowPrivateAccess, invite, invite.getRecurId(),
                                            CalendarMailSender.VERB_DECLINE);
                                    CalendarMailSender.sendResourceAutoReply(
                                            octxt, mbox, true,
                                            CalendarMailSender.VERB_DECLINE, false,
                                            msg,
                                            this, invite, new Invite[] { replyInv }, mmInv);
                                    replySent = true;
                                }
                            }
                        }
                    }
                }
                if (!replySent && IcalXmlStrMap.PARTSTAT_ACCEPTED.equals(partStat)) {
                    if (needReplyEmail) {
                        Invite replyInv = makeReplyInvite(
                                account, authAcct, lc, onBehalfOf, allowPrivateAccess, invite, invite.getRecurId(),
                                CalendarMailSender.VERB_ACCEPT);
                        CalendarMailSender.sendResourceAutoReply(
                                octxt, mbox, true,
                                CalendarMailSender.VERB_ACCEPT, false,
                                null,
                                this, invite, new Invite[] { replyInv }, mmInv);
                    }
                }
                // Record the final outcome in the replies list.
                if (IcalXmlStrMap.PARTSTAT_NEEDS_ACTION.equals(partStat)) {
                    getReplyList().modifyPartStat(
                            resource, invite.getRecurId(), null, resource.getName(), null, null,
                            partStat, false, invite.getSeqNo(), opTime);
                    replyListUpdated = true;
                }
                if (forCreate && replyListUpdated)
                    saveMetadata();
            }
        }

        CreateCalendarItemRecorder recorder =
            (CreateCalendarItemRecorder) mbox.getRedoRecorder();
        recorder.setCalendarItemPartStat(partStat);

        invite.updateMyPartStat(account, partStat);
        if (forCreate) {
            Invite defaultInvite = getDefaultInviteOrNull();
            if (defaultInvite != null && !defaultInvite.equals(invite) &&
                !partStat.equals(defaultInvite.getPartStat())) {
                defaultInvite.updateMyPartStat(account, partStat);
                saveMetadata();
            }
        }

        return partStat;
    }

    // Figure out the timezone to use for expressing start/end times for
    // conflicting meetings.  Do our best to use a timezone familiar to the
    // organizer.
    private ICalTimeZone chooseReplyTZ(Invite invite) throws ServiceException {
        Mailbox mailbox = getMailbox();
        Account account = Provisioning.getInstance().getAccountByMailbox(mailbox);
        Account organizer = invite.getOrganizerAccount();
        ICalTimeZone tz = invite.getStartTime().getTimeZone();
        if (tz == null && invite.isAllDayEvent()) {
            // floating time: use resource's timezone
            tz = Util.getAccountTimeZone(account);
            if (tz == null)
                ICalTimeZone.getUTC();
        } else {
            // tz != null || !allday
            if (tz == null || tz.sameAsUTC()) {
                if (organizer != null) {
                    // For this case, let's assume the sender didn't really mean UTC.
                    // This happens with Outlook and possibly more clients.
                    tz = Util.getAccountTimeZone(organizer);
                } else {
                    // If organizer is not a local user, use resource's timezone.
                    tz = Util.getAccountTimeZone(account);
                    if (tz == null)
                        ICalTimeZone.getUTC();
                }
            } else {
                // Timezone is not UTC.  We can safely assume the client sent the
                // correct local timezone.
            }
        }
        return tz;
    }

    private Invite makeReplyInvite(Account account, Account authAccount, Locale lc,
                                   boolean onBehalfOf, boolean allowPrivateAccess,
                                   Invite inv, RecurId rid, Verb verb)
    throws ServiceException {
        boolean hidePrivate = !inv.isPublic() && !allowPrivateAccess;
        String subject;
        if (hidePrivate)
            subject = L10nUtil.getMessage(MsgKey.calendarSubjectWithheld, lc);
        else
            subject = inv.getName();
        String replySubject = CalendarMailSender.getReplySubject(verb, subject, lc);
        ParsedDateTime ridDt = rid != null ? rid.getDt() : null;
        Invite replyInv = CalendarMailSender.replyToInvite(
                account, authAccount, onBehalfOf, allowPrivateAccess, inv, verb, replySubject, ridDt);
        return replyInv;
    }
}
