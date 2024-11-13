// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only
package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

import java.util.Map;

public class SendSecureMsg extends SendMsg {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        return handle(request, context, MailConstants.SEND_SECURE_MSG_RESPONSE);
    }

}
