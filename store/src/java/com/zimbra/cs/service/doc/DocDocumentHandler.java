// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.doc;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.service.mail.MailDocumentHandler;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;

public abstract class DocDocumentHandler extends MailDocumentHandler {
    private static final String[] TARGET_ID_PATH = new String[] { MailConstants.E_WIKIWORD, MailConstants.A_ID };
    private static final String[] TARGET_DOC_ID_PATH = new String[] { MailConstants.E_DOC, MailConstants.A_ID };
    private static final String[] TARGET_FOLDER_PATH = new String[] { MailConstants.E_WIKIWORD, MailConstants.A_FOLDER };
    private static final String[] TARGET_DOC_FOLDER_PATH = new String[] { MailConstants.E_DOC, MailConstants.A_FOLDER };

    @Override
    protected String[] getProxiedIdPath(Element request)     {
        String[] path = TARGET_ID_PATH;
        String id = getXPath(request, path);
        if (id == null) {
            path = TARGET_DOC_ID_PATH;
            id = getXPath(request, path);
        }
        if (id == null) {
            path = TARGET_FOLDER_PATH;
            id = getXPath(request, path);
        }
        if (id == null) {
            path = TARGET_DOC_FOLDER_PATH;
            id = getXPath(request, path);
        }
        return path; 
    }

    @Override
    protected boolean checkMountpointProxy(Element request)  { return true; }

    protected String getAuthor(ZimbraSoapContext zsc) throws ServiceException {
        return getAuthenticatedAccount(zsc).getName();
    }

    protected ItemId getRequestedFolder(Element request, ZimbraSoapContext zsc) throws ServiceException {
        for (Element elem : request.listElements()) {
            String fid = elem.getAttribute(MailConstants.A_FOLDER, null);
            if (fid != null) {
                return new ItemId(fid, zsc);
            }
        }
        return null;
    }
}
