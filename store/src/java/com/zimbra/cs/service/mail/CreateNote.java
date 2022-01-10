// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Sep 8, 2004
 */
package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Note;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Note.Rectangle;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author dkarp
 */
public class CreateNote extends MailDocumentHandler {

    private static final String[] TARGET_FOLDER_PATH = new String[] { MailConstants.E_NOTE, MailConstants.A_FOLDER };
    private static final String[] RESPONSE_ITEM_PATH = new String[] { };
    protected String[] getProxiedIdPath(Element request)     { return TARGET_FOLDER_PATH; }
    protected boolean checkMountpointProxy(Element request)  { return true; }
    protected String[] getResponseItemPath()  { return RESPONSE_ITEM_PATH; }

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
		ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

        Element t = request.getElement(MailConstants.E_NOTE);
        ItemId iidFolder = new ItemId(t.getAttribute(MailConstants.A_FOLDER), zsc);
        String content = t.getAttribute(MailConstants.E_CONTENT);
        byte color = (byte) t.getAttributeLong(MailConstants.A_COLOR, MailItem.DEFAULT_COLOR);
        String strBounds = t.getAttribute(MailConstants.A_BOUNDS, null);
        Rectangle bounds = new Rectangle(strBounds);

        Note note = mbox.createNote(octxt, content, bounds, color, iidFolder.getId());

        Element response = zsc.createElement(MailConstants.CREATE_NOTE_RESPONSE);
        if (note != null)
        	ToXML.encodeNote(response, ifmt, octxt, note);
        return response;
	}
}
