// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;

public class GetMailboxMetadata extends MailDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);

        Element meta = request.getElement(MailConstants.E_METADATA);
        String section = meta.getAttribute(MailConstants.A_SECTION);
        Metadata metadata = mbox.getConfig(octxt, section);

        Element response = zsc.createElement(MailConstants.GET_MAILBOX_METADATA_RESPONSE);
        meta = response.addElement(MailConstants.E_METADATA);
        meta.addAttribute(MailConstants.A_SECTION, section);

        if (metadata != null) {
            for (Map.Entry<String, ?> entry : metadata.asMap().entrySet()) {
                meta.addKeyValuePair(entry.getKey(), entry.getValue().toString());
            }
        }

        return response;
    }
}
