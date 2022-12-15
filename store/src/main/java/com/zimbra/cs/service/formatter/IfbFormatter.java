// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.IDNUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.fb.FreeBusy;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;

public class IfbFormatter extends Formatter {

    private static final long ONE_MONTH = Constants.MILLIS_PER_DAY*31;
    
    @Override
    public FormatType getType() {
        return FormatType.IFB;
    }

    @Override
    public boolean requiresAuth() {
        return false;
    }

    @Override
    public Set<MailItem.Type> getDefaultSearchTypes() {
        return EnumSet.of(MailItem.Type.APPOINTMENT);
    }

    @Override
    public void formatCallback(UserServletContext context) throws IOException, ServiceException, UserServletException {
        context.resp.setCharacterEncoding("UTF-8");
        context.resp.setContentType(MimeConstants.CT_TEXT_CALENDAR);

        long rangeStart = context.getStartTime();
        long rangeEnd = context.getEndTime();

        if (rangeEnd < rangeStart)
            throw new UserServletException(HttpServletResponse.SC_BAD_REQUEST, "End time must be after Start time");

        long days = (rangeEnd-rangeStart)/Constants.MILLIS_PER_DAY;
        long maxDays = LC.calendar_freebusy_max_days.longValueWithinRange(0, 36600);
        if (days > maxDays)
            throw new UserServletException(HttpServletResponse.SC_BAD_REQUEST, "Requested range is too large (Maximum " + maxDays + " days)");

        String acctName = null;
        FreeBusy fb = null;
        if (context.targetMailbox != null) {
            String exuid = context.params.get(UserServlet.QP_EXUID);
            Appointment exAppt = null;
            if (exuid != null) {
                CalendarItem ci = context.targetMailbox.getCalendarItemByUid(context.opContext, exuid);
                if (ci instanceof Appointment)
                    exAppt = (Appointment) ci;
            }
            acctName = context.targetMailbox.getAccount().getName();
            fb = context.targetMailbox.getFreeBusy(context.opContext, acctName, rangeStart, rangeEnd, context.getFreeBusyCalendar(), exAppt);
        } else {
            // Unknown mailbox.  Fake an always-free response, to avoid harvest attacks.
            acctName = fixupAccountName(context.accountPath);
            fb = FreeBusy.emptyFreeBusy(acctName, rangeStart, rangeEnd);
        }

        String fbFormat = context.params.get(UserServlet.QP_FBFORMAT);
        String fbMsg;
        if ("event".equalsIgnoreCase(fbFormat)) {
            fbMsg = fb.toVCalendarAsVEvents();
        } else {
            String url = context.req.getRequestURL() + "?" + context.req.getQueryString();
            fbMsg = fb.toVCalendar(FreeBusy.Method.PUBLISH, acctName, null, url);
        }
        context.resp.getOutputStream().write(fbMsg.getBytes("UTF-8"));
    }

    @Override
    public long getDefaultStartTime() {
        return System.currentTimeMillis() - ONE_MONTH;
    }

    // eventually get this from query param ?end=long|YYYYMMMDDHHMMSS
    @Override
    public long getDefaultEndTime() {
        return System.currentTimeMillis() + (2 * ONE_MONTH);
    }

  

    private String fixupAccountName(String emailAddress) throws ServiceException {
        int index = emailAddress.indexOf('@');
        String domain = null;
        if (index == -1) {
            // domain is already in ASCII name
            domain = Provisioning.getInstance().getConfig().getAttr(Provisioning.A_zimbraDefaultDomainName, null);
            if (domain != null)
                emailAddress = emailAddress + "@" + domain;
        } else
            emailAddress = IDNUtil.toAsciiEmail(emailAddress);

        return emailAddress;
    }
}
