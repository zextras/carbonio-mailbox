/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on Sep 8, 2004
 */
package com.zimbra.cs.service.mail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.OperationContext;
import com.zimbra.cs.mailbox.Note.Rectangle;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.Element;
import com.zimbra.soap.SoapFaultException;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author dkarp
 */
public class NoteAction extends ItemAction {

    protected String[] getProxiedIdPath(Element request) {
        String operation = getXPath(request, OPERATION_PATH);
        if (operation != null && NOTE_OPS.contains(operation.toLowerCase()))
            return TARGET_ITEM_PATH;
        return super.getProxiedIdPath(request);
    }

	public static final String OP_EDIT       = "edit";
	public static final String OP_REPOSITION = "pos";

    private static final Set<String> NOTE_OPS = new HashSet<String>(Arrays.asList(new String[] {
        OP_EDIT, OP_REPOSITION
    }));

	public Element handle(Element request, Map<String, Object> context) throws ServiceException, SoapFaultException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);

        Element action = request.getElement(MailService.E_ACTION);
        String operation = action.getAttribute(MailService.A_OPERATION).toLowerCase();

        if (operation.endsWith(OP_READ) || operation.endsWith(OP_SPAM))
            throw ServiceException.INVALID_REQUEST("invalid operation on note: " + operation, null);
        String successes;
        if (NOTE_OPS.contains(operation))
            successes = handleNote(context, request, operation);
        else
            successes = handleCommon(context, request, operation, MailItem.TYPE_NOTE);

        Element response = lc.createElement(MailService.NOTE_ACTION_RESPONSE);
        Element act = response.addUniqueElement(MailService.E_ACTION);
        act.addAttribute(MailService.A_ID, successes);
        act.addAttribute(MailService.A_OPERATION, operation);
        return response;
	}

    private String handleNote(Map<String, Object> context, Element request, String operation) throws ServiceException {
        Element action = request.getElement(MailService.E_ACTION);

        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(lc);
        OperationContext octxt = lc.getOperationContext();
        ItemId iid = new ItemId(action.getAttribute(MailService.A_ID), lc);

        if (operation.equals(OP_EDIT)) {
            String content = action.getAttribute(MailService.E_CONTENT);
            mbox.editNote(octxt, iid.getId(), content);
        } else if (operation.equals(OP_REPOSITION)) {
            String strBounds = action.getAttribute(MailService.A_BOUNDS, null);
            mbox.repositionNote(octxt, iid.getId(), new Rectangle(strBounds));
        } else
            throw ServiceException.INVALID_REQUEST("unknown operation: " + operation, null);

        return lc.formatItemId(iid);
    }
}
