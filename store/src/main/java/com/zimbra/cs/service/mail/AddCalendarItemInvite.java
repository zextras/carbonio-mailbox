// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import javax.mail.MessagingException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Mailbox.AddInviteData;
import com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData;
import com.zimbra.cs.mailbox.calendar.CalendarMailSender;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.RecurId;
import com.zimbra.cs.mailbox.calendar.ZOrganizer;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;

public class AddCalendarItemInvite extends CalendarRequest {

    protected class AddInviteParser extends ParseMimeMessage.InviteParser {
        @Override
        public ParseMimeMessage.InviteParserResult parseInviteElement(ZimbraSoapContext lc, OperationContext octxt,
                Account account, Element inviteElem) throws ServiceException {
            return CalendarUtils.parseInviteForAddInvite(account, getItemType(), inviteElem, null);
        }
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account acct = getRequestedAccount(zsc);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);

        AddInviteParser parser = new AddInviteParser();
        SetCalendarItemData scid = SetCalendarItem.getSetCalendarItemData(zsc, octxt, acct, mbox, request, parser);

        Invite inv = scid.invite;
        CalendarItem calItem = mbox.getCalendarItemByUid(octxt, inv.getUid());
        int folderId = inv.isTodo() ? Mailbox.ID_FOLDER_TASKS : Mailbox.ID_FOLDER_CALENDAR;
        if (calItem != null) {
            int f = calItem.getFolderId();
            if (f != Mailbox.ID_FOLDER_TRASH && f != Mailbox.ID_FOLDER_SPAM)
                folderId = f;
        }

        // Bug 38550/41239: If ORGANIZER is missing, set it to email sender.  If that sender
        // is the same user as the recipient, don't set organizer and clear attendees instead.
        // We don't want to set organizer to receiving user unless we're absolutely certain
        // it's the correct organizer.
        if (!inv.hasOrganizer() && inv.hasOtherAttendees()) {
            if (scid.message == null) {
                ZimbraLog.calendar.info(
                        "Got malformed invite without organizer.  Clearing attendees to prevent inadvertent cancels.");
                inv.clearAttendees();
            } else {
                String fromEmail = scid.message.getSenderEmail(true);
                if (fromEmail != null) {
                    boolean dangerousSender = false;
                    // Is sender == recipient?  If so, clear attendees.
                    String intendedForAddress;
                    try {
                        intendedForAddress = scid.message.getMimeMessage().getHeader(
                                CalendarMailSender.X_ZIMBRA_CALENDAR_INTENDED_FOR, null);
                    } catch (MessagingException e) {
                        throw ServiceException.FAILURE("error parsing message", e);
                    }
                    if (intendedForAddress != null && intendedForAddress.length() > 0) {
                        if (intendedForAddress.equalsIgnoreCase(fromEmail)) {
                            ZimbraLog.calendar.info(
                                    "Got malformed invite without organizer.  Clearing attendees to prevent inadvertent cancels.");
                            inv.clearAttendees();
                            dangerousSender = true;
                        }
                    } else if (AccountUtil.addressMatchesAccount(acct, fromEmail)) {
                        ZimbraLog.calendar.info(
                                "Got malformed invite without organizer.  Clearing attendees to prevent inadvertent cancels.");
                        inv.clearAttendees();
                        dangerousSender = true;
                    }
                    if (!dangerousSender) {
                        ZOrganizer org = new ZOrganizer(fromEmail, null);
                        String senderEmail = scid.message.getSenderEmail(false);
                        if (senderEmail != null && !senderEmail.equalsIgnoreCase(fromEmail))
                            org.setSentBy(senderEmail);
                        inv.setOrganizer(org);
                        ZimbraLog.calendar.info(
                                "Got malformed invite that lists attendees without specifying an organizer.  " +
                                "Defaulting organizer to: " + org.toString());
                    }
                }
            }
        }

        // trace logging
        String calItemIdStr = calItem != null ? Integer.toString(calItem.getId()) : "(new)";
        if (!inv.hasRecurId())
            ZimbraLog.calendar.info("<AddCalendarItemInvite> id=%s, folderId=%d, subject=\"%s\", UID=%s",
                    calItemIdStr, folderId, inv.isPublic() ? inv.getName() : "(private)",
                    inv.getUid());
        else
            ZimbraLog.calendar.info("<AddCalendarItemInvite> id=%s, folderId=%d, subject=\"%s\", UID=%s, recurId=%s",
                    calItemIdStr, folderId, inv.isPublic() ? inv.getName() : "(private)",
                    inv.getUid(), inv.getRecurId().getDtZ());

        Element response = getResponseElement(zsc);
        if (calItem != null) {
            // If the calendar item already has the invite, no need to add again.
            RecurId rid = scid.invite.getRecurId();
            Invite matchingInv = calItem.getInvite(rid);
            if (matchingInv != null && matchingInv.isSameOrNewerVersion(scid.invite)) {
                response.addAttribute(MailConstants.A_CAL_ID, calItem.getId());
                response.addAttribute(MailConstants.A_CAL_INV_ID, matchingInv.getMailItemId());
                response.addAttribute(MailConstants.A_CAL_COMPONENT_NUM, matchingInv.getComponentNum());
                return response;
            }
        }

        AddInviteData aid = mbox.addInvite(octxt, inv, folderId, scid.message, false, false, true);
        if (aid != null) {
            calItem = mbox.getCalendarItemById(octxt, aid.calItemId);
            if (calItem != null) {
                Invite[] invs = calItem.getInvites(aid.invId);
                if (invs != null && invs.length > 0) {
                    response.addAttribute(MailConstants.A_CAL_ID, aid.calItemId);
                    response.addAttribute(MailConstants.A_CAL_INV_ID, aid.invId);
                    response.addAttribute(MailConstants.A_CAL_COMPONENT_NUM, invs[0].getComponentNum());
                }
            }
        }
        return response;
    }

}
