// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.formatter.ContactCSV;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.ExportContactsRequest;
import com.zimbra.soap.mail.message.ExportContactsResponse;

/**
 * @author schemers
 */
public final class ExportContacts extends MailDocumentHandler  {

    private static final String[] TARGET_FOLDER_PATH =
                new String[] { MailConstants.A_FOLDER };

    @Override
    protected String[] getProxiedIdPath(Element request) {
        return TARGET_FOLDER_PATH;
    }

    @Override
    protected boolean checkMountpointProxy(Element request) {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);

        ExportContactsRequest req = zsc.elementToJaxb(request);
        String folder = req.getFolderId();
        ItemId iidFolder = folder == null ? null : new ItemId(folder, zsc);

        String ct = req.getContentType();
        if (ct == null)
            throw ServiceException.INVALID_REQUEST(
                    "content type missing", null);
        if (!ct.equals("csv"))
            throw ServiceException.INVALID_REQUEST(
                    "unsupported content type: " + ct, null);

        String format = req.getCsvFormat();
        String locale = req.getCsvLocale();
        String separator = req.getCsvDelimiter();
        Character sepChar = null;
        if ((separator != null) && (separator.length() > 0))
            sepChar = separator.charAt(0);

        List<Contact> contacts = mbox.getContactList(
                octxt, iidFolder != null ? iidFolder.getId() : -1);

        StringBuilder sb = new StringBuilder();
        if (contacts == null)
            contacts = new ArrayList<Contact>();

        try {
            ContactCSV contactCSV = new ContactCSV(mbox,octxt);
            contactCSV.toCSV(format, locale, sepChar, contacts.iterator(), sb);
        } catch (ContactCSV.ParseException e) {
            throw MailServiceException.UNABLE_TO_EXPORT_CONTACTS(
                        e.getMessage(), e);
        }

        ExportContactsResponse resp = new ExportContactsResponse(sb.toString());
        return zsc.jaxbToElement(resp);
    }
}
