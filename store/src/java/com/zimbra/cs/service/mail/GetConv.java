// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.index.SearchParams;
import com.zimbra.cs.index.SearchParams.ExpandResults;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Conversation;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @since May 26, 2004
 * @author schemers
 */
public class GetConv extends MailDocumentHandler  {

    private static final String[] TARGET_CONV_PATH = new String[] { MailConstants.E_CONV, MailConstants.A_ID };

    @Override
    protected String[] getProxiedIdPath(Element request)  { return TARGET_CONV_PATH; }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

        Element econv = request.getElement(MailConstants.E_CONV);
        ItemId iid = new ItemId(econv.getAttribute(MailConstants.A_ID), zsc);

        SearchParams params = new SearchParams();
        params.setInlineRule(ExpandResults.valueOf(econv.getAttribute(MailConstants.A_FETCH, null), zsc));
        if (params.getInlineRule() != ExpandResults.NONE) {
            params.setWantHtml(econv.getAttributeBool(MailConstants.A_WANT_HTML, false));
            params.setMaxInlinedLength((int) econv.getAttributeLong(MailConstants.A_MAX_INLINED_LENGTH, -1));
            params.setWantExpandGroupInfo(econv.getAttributeBool(MailConstants.A_NEED_EXP, false));
            for (Element eHdr : econv.listElements(MailConstants.A_HEADER)) {
                params.addInlinedHeader(eHdr.getAttribute(MailConstants.A_ATTRIBUTE_NAME));
            }
        }

        Conversation conv = mbox.getConversationById(octxt, iid.getId());

        if (conv == null) {
            throw MailServiceException.NO_SUCH_CONV(iid.getId());
        }
        List<Message> msgs = mbox.getMessagesByConversation(octxt, conv.getId(), SortBy.DATE_ASC, -1);
        if (msgs.isEmpty() && zsc.isDelegatedRequest()) {
            throw ServiceException.PERM_DENIED("you do not have sufficient permissions");
        }
        Element response = zsc.createElement(MailConstants.GET_CONV_RESPONSE);
        ToXML.encodeConversation(response, ifmt, octxt, conv, msgs, params);
        return response;
    }
}
