// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.ZimbraSoapContext;

import java.util.Map;

/**
 * Unsets the zimbraCalendarReminderDeviceEmail account attr
 */
public class InvalidateReminderDevice extends MailDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        String emailAddr = request.getAttribute(MailConstants.A_ADDRESS);
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);
        String configuredAddr = account.getCalendarReminderDeviceEmail();
        if (emailAddr.equals(configuredAddr))
            account.unsetCalendarReminderDeviceEmail();
        else
            throw ServiceException.INVALID_REQUEST("Email address'" +
                    emailAddr + "' is not same as the " +
                    Provisioning.A_zimbraCalendarReminderDeviceEmail +
                    " attr value '" + configuredAddr + "'", null);
        return zsc.createElement(
                MailConstants.INVALIDATE_REMINDER_DEVICE_RESPONSE);
    }
}
