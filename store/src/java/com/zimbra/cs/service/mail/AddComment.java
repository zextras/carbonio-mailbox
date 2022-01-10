// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Comment;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;

public class AddComment extends MailDocumentHandler {

    private static final String[] PARENT_ID_PATH = new String[] { MailConstants.E_COMMENT, MailConstants.A_PARENT_ID };
    
    @Override
    protected String[] getProxiedIdPath(Element request)     { return PARENT_ID_PATH; }
    
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        String creator = zsc.getAuthtokenAccountId();

        Element c = request.getElement(MailConstants.E_COMMENT);
        String itemId = c.getAttribute(MailConstants.A_PARENT_ID);
        String text = c.getAttribute(MailConstants.A_TEXT);

        ItemId iid = new ItemId(itemId, zsc);
        Comment comment = mbox.createComment(octxt, iid.getId(), text, creator);

        Element response = zsc.createElement(MailConstants.ADD_COMMENT_RESPONSE);
        response.addElement(MailConstants.E_COMMENT).addAttribute(MailConstants.A_ID, comment.getId());
        return response;
    }
}
