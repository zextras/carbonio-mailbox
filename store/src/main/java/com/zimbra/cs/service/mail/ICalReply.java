// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.List;
import java.util.Map;

import com.zimbra.common.calendar.ZCalendar.ICalTok;
import com.zimbra.common.calendar.ZCalendar.ZCalendarBuilder;
import com.zimbra.common.calendar.ZCalendar.ZVCalendar;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.soap.ZimbraSoapContext;

public class ICalReply extends MailDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException, SoapFaultException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);

        Element icalElem = request.getElement(MailConstants.E_CAL_ICAL);
        String icalStr = icalElem.getText();
        String sender = icalElem.getAttribute(MailConstants.E_CAL_ATTENDEE, null);
        ZVCalendar cal = ZCalendarBuilder.build(icalStr);

        List<Invite> invites =
            Invite.createFromCalendar(mbox.getAccount(), null, cal, false);
        for (Invite inv : invites) {
            String method = inv.getMethod();
            if (!ICalTok.REPLY.toString().equals(method)) {
                throw ServiceException.INVALID_REQUEST(
                        "iCalendar method must be REPLY (was " + method + ")", null);
            }
        }
        for (Invite inv : invites) {
            mbox.processICalReply(octxt, inv, sender);
        }

        Element response = getResponseElement(zsc);
        return response;
    }
}
