// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;

public class CounterCalendarItem extends CalendarRequest {

    private class InviteParser extends ParseMimeMessage.InviteParser {
        private Invite mInv;
        
        InviteParser(Invite inv) {
            mInv = inv;
        }
        
        public ParseMimeMessage.InviteParserResult parseInviteElement(
                ZimbraSoapContext lc, OperationContext octxt, Account account, Element inviteElem)
        throws ServiceException {
            return CalendarUtils.parseInviteForCounter(account, mInv, getItemType(), inviteElem);
        }
    };

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account acct = getRequestedAccount(zsc);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        Invite oldInv = null;

        // proxy handling
        String idStr = request.getAttribute(MailConstants.A_ID, null);
        if (idStr != null) {
            ItemId iid = new ItemId(idStr, zsc);
            if (!iid.belongsTo(acct))
                return proxyRequest(request, context, iid.getAccountId());
        
            CalendarItem calItem = mbox.getCalendarItemById(octxt, iid.getId());
            if (calItem == null) {
                throw MailServiceException.NO_SUCH_CALITEM(iid.toString(), "Could not find calendar item");
            }

            // Conflict detection.  Do it only if requested by client.  (for backward compat)
            int modSeq = (int) request.getAttributeLong(MailConstants.A_MODIFIED_SEQUENCE, 0);
            int revision = (int) request.getAttributeLong(MailConstants.A_REVISION, 0);
            if (modSeq != 0 && revision != 0 &&
                    (modSeq < calItem.getModifiedSequence() || revision < calItem.getSavedSequence()))
                throw MailServiceException.INVITE_OUT_OF_DATE(iid.toString());
        
            int compNum = (int) request.getAttributeLong(MailConstants.A_CAL_COMP, 0);
            if (compNum != 0) {
                oldInv = calItem.getInvite(iid.getSubpartId(), compNum);
                if (oldInv == null)
                    throw MailServiceException.INVITE_OUT_OF_DATE(iid.toString());
            }
        }

        Element msgElem = request.getElement(MailConstants.E_MSG);
        InviteParser parser = new InviteParser(oldInv);
        CalSendData dat = handleMsgElement(zsc, octxt, msgElem, acct, mbox, parser);

        MailSender mailSender = mbox.getMailSender();
        mailSender.sendMimeMessage(octxt, mbox, dat.mMm);
        Element response = getResponseElement(zsc);
        return response;
    }
}
