// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.mailbox.ItemIdentifier;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.GetIMAPRecentCutoffRequest;
import com.zimbra.soap.mail.message.GetIMAPRecentCutoffResponse;

public class GetIMAPRecentCutoff extends MailDocumentHandler {

    private static final String[] TARGET_FOLDER_PATH = new String[] { MailConstants.A_ID };
    @Override
    protected String[] getProxiedIdPath(Element request) {
        return TARGET_FOLDER_PATH;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        GetIMAPRecentCutoffRequest req = zsc.elementToJaxb(request);
        ItemIdentifier itemIdent = ItemIdentifier.fromOwnerAndRemoteId(mbox.getAccountId(), req.getId());
        if (!mbox.getAccountId().equals(itemIdent.accountId)) {
            throw MailServiceException.NO_SUCH_FOLDER(req.getId());
        }
        return zsc.jaxbToElement(new GetIMAPRecentCutoffResponse(
                mbox.getImapRecentCutoff(octxt, itemIdent.id)));
    }
}
