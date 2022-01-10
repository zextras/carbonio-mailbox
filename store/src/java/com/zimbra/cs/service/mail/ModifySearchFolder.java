// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.SearchFolder;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class ModifySearchFolder extends MailDocumentHandler  {

    private static final String[] TARGET_FOLDER_PATH = new String[] { MailConstants.E_SEARCH, MailConstants.A_ID };
    protected String[] getProxiedIdPath(Element request)     { return TARGET_FOLDER_PATH; }
    protected boolean checkMountpointProxy(Element request)  { return false; }

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
		ZimbraSoapContext zsc = getZimbraSoapContext(context);
		Mailbox mbox = getRequestedMailbox(zsc);
		OperationContext octxt = getOperationContext(zsc, context);
        ItemIdFormatter ifmt = new ItemIdFormatter(zsc);
		
        Element t = request.getElement(MailConstants.E_SEARCH);
        ItemId iid = new ItemId(t.getAttribute(MailConstants.A_ID), zsc);
        String query = t.getAttribute(MailConstants.A_QUERY);
        String types = t.getAttribute(MailConstants.A_SEARCH_TYPES, null);
        String sort = t.getAttribute(MailConstants.A_SORTBY, null);
        
        mbox.modifySearchFolder(octxt, iid.getId(), query, types, sort);
        SearchFolder search = mbox.getSearchFolderById(octxt, iid.getId());
        
        Element response = zsc.createElement(MailConstants.MODIFY_SEARCH_FOLDER_RESPONSE);
    	ToXML.encodeSearchFolder(response, ifmt, search);
        return response;
	}
}
