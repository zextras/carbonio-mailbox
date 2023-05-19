// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.type.GetCalendarItemRequestBase;

/**
 * @author tim
 */
public class GetCalendarItem extends CalendarRequest {
    private static final Log LOG = LogFactory.getLog(GetCalendarItem.class);

    private static final String[] TARGET_ITEM_PATH = new String[] { MailConstants.A_ID };
    private static final String[] RESPONSE_ITEM_PATH = new String[0];

    @Override
    protected String[] getProxiedIdPath(Element request) {
        return TARGET_ITEM_PATH;
    }

    @Override
    protected boolean checkMountpointProxy(Element request) {
        return false;
    }

    @Override
    protected String[] getResponseItemPath() {
        return RESPONSE_ITEM_PATH;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);
        ItemIdFormatter ifmt = new ItemIdFormatter(zsc);
        GetCalendarItemRequestBase req = zsc.elementToJaxb(request);

        boolean sync = req.getSync() != null && req.getSync();
        boolean includeContent = req.getIncludeContent() != null && req.getIncludeContent();
        boolean includeInvites = req.getIncludeInvites() == null || req.getIncludeInvites();
        ItemId iid = null;
        String uid = req.getUid();
        String id = req.getId();
        if (uid != null) {
            if (id != null) {
                throw ServiceException.INVALID_REQUEST("either id or uid should be specified, but not both", null);
            }
            LOG.info("<GetCalendarItem uid=" + uid + "> " + zsc);
        } else {
            iid = new ItemId(id, zsc);
            LOG.info("<GetCalendarItem id=" + iid.getId() + "> " + zsc);
        }

        // want to return modified date only on sync-related requests
        int fields = ToXML.NOTIFY_FIELDS;
        if (sync) {
            fields |= Change.CONFLICT;
        }
        Element response = getResponseElement(zsc);
        CalendarItem calItem = null;
        mbox.lock.lock(false);
        try {
            if (uid != null) {
                calItem = mbox.getCalendarItemByUid(octxt, uid);
                if (calItem == null) {
                    throw MailServiceException.NO_SUCH_CALITEM(uid);
                }
            } else {
                calItem = mbox.getCalendarItemById(octxt, iid);
                if (calItem == null) {
                    throw MailServiceException.NO_SUCH_CALITEM(iid.toString());
                }
            }
        } finally {
            mbox.lock.release();
        }
        ToXML.encodeCalendarItemSummary(response, ifmt, octxt, calItem, fields, includeInvites, includeContent);

        return response;
    }
}
