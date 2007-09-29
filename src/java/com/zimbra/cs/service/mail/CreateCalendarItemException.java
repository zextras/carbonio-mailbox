/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006, 2007 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.service.mail;

import java.util.List;
import java.util.Map;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.ZAttendee;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;


public class CreateCalendarItemException extends CreateCalendarItem {
    
    private static Log sLog = LogFactory.getLog(CreateCalendarItemException.class);

    private static final String[] TARGET_PATH = new String[] { MailService.A_ID };
    protected String[] getProxiedIdPath(Element request)     { return TARGET_PATH; }
    protected boolean checkMountpointProxy(Element request)  { return false; }

    protected class CreateCalendarItemExceptionInviteParser extends ParseMimeMessage.InviteParser
    {
        private String mUid;
        private Invite mDefaultInvite;

        CreateCalendarItemExceptionInviteParser(String uid, Invite defaultInvite) {
            mUid = uid;
            mDefaultInvite = defaultInvite;
        }

        public ParseMimeMessage.InviteParserResult parseInviteElement(ZimbraSoapContext lc, Account account, Element inviteElem)
        throws ServiceException {
            ParseMimeMessage.InviteParserResult toRet =
                CalendarUtils.parseInviteForCreate(
                        account, getItemType(), inviteElem,
                        mDefaultInvite.getTimeZoneMap(), mUid, true,
                        CalendarUtils.RECUR_NOT_ALLOWED);

            // Send cancellations to any attendees who have been removed.
            List<ZAttendee> removedAttendees =
                CalendarUtils.getRemovedAttendees(mDefaultInvite, toRet.mInvite);
            if (removedAttendees.size() > 0)
                updateRemovedInvitees(lc, account, mDefaultInvite.getCalendarItem().getMailbox(),
                                      mDefaultInvite.getCalendarItem(), toRet.mInvite, removedAttendees);

            return toRet;
        }
    };

    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account acct = getRequestedAccount(zsc);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = zsc.getOperationContext();
        
        ItemId iid = new ItemId(request.getAttribute(MailService.A_ID), zsc);
        int compNum = (int) request.getAttributeLong(MailService.E_INVITE_COMPONENT);
        sLog.info("<CreateCalendarItemException id=" + zsc.formatItemId(iid) + " comp=" + compNum + "> " + zsc.toString());
        
        // <M>
        Element msgElem = request.getElement(MailService.E_MSG);
        
        if (msgElem.getAttribute(MailService.A_FOLDER, null) != null) {
            throw ServiceException.FAILURE("You may not specify a target Folder when creating an Exception for an existing calendar item", null);
        }
        
        Element response = getResponseElement(zsc);
        synchronized(mbox) {
            CalendarItem calItem = mbox.getCalendarItemById(octxt, iid.getId()); 
            Invite inv = calItem.getInvite(iid.getSubpartId(), compNum);
            
            if (inv.hasRecurId()) {
                throw MailServiceException.INVITE_OUT_OF_DATE("Invite id=" + zsc.formatItemId(iid) + " comp=" + compNum + " is not the a default invite");
            }
            
            if (calItem == null)
                throw MailServiceException.NO_SUCH_CALITEM(inv.getUid(), " for CreateCalendarItemExceptionRequest(" + iid + "," + compNum + ")");
            else if (!calItem.isRecurring())
                throw ServiceException.INVALID_REQUEST("CalendarItem " + calItem.getId() + " is not a recurring calendar item", null);
            
            CreateCalendarItemExceptionInviteParser parser = new CreateCalendarItemExceptionInviteParser(calItem.getUid(), inv);
            CalSendData dat = handleMsgElement(zsc, msgElem, acct, mbox, parser);
            
            return sendCalendarMessage(zsc, octxt, calItem.getFolderId(), acct, mbox, dat, response, false);
        }
    }
}
