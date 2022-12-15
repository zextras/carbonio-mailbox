// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.session.AdminSession;
import com.zimbra.cs.session.Session;

import com.zimbra.soap.ZimbraSoapContext;

public class GetAllMailboxes extends AdminDocumentHandler {

    private static final String GET_ALL_MAILBOXES_CACHE_KEY = "GetAllMailboxes";

    public static final String SORT_TOTAL_USED = "totalUsed";

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        // allow only system admin for now
        checkRight(zsc, context, null, AdminRight.PR_SYSTEM_ADMIN_ONLY);

        int limit = (int) request.getAttributeLong(AdminConstants.A_LIMIT, Integer.MAX_VALUE);
        if (limit == 0)
            limit = Integer.MAX_VALUE;
        int offset = (int) request.getAttributeLong(AdminConstants.A_OFFSET, 0);

        /* sorting not supported for now
        String sortBy = request.getAttribute(AdminConstants.A_SORT_BY, SORT_TOTAL_USED);
        final boolean sortAscending = request.getAttributeBool(AdminConstants.A_SORT_ASCENDING, false);

        if (!sortBy.equals(SORT_TOTAL_USED))
            throw ServiceException.INVALID_REQUEST("sortBy must be " +  SORT_TOTAL_USED, null);
        */

        MailboxesParams params = new MailboxesParams();
        List<Mailbox.MailboxData> mailboxes;

        AdminSession session = (AdminSession) getSession(zsc, Session.Type.ADMIN);
        if (session != null) {
            MailboxesParams cachedParams = (MailboxesParams) session.getData(GET_ALL_MAILBOXES_CACHE_KEY);
            if (cachedParams == null || !cachedParams.equals(params)) {
                mailboxes = params.doSearch();
                session.setData(GET_ALL_MAILBOXES_CACHE_KEY, params);
            } else {
                mailboxes = cachedParams.doSearch();
            }
        } else {
            mailboxes = params.doSearch();
        }

        Element response = zsc.createElement(AdminConstants.GET_ALL_MAILBOXES_RESPONSE);
        int i, limitMax = offset + limit;
        for (i = offset; i < limitMax && i < mailboxes.size(); i++) {
            Mailbox.MailboxData mailbox = mailboxes.get(i);

            Element mbx = response.addElement(AdminConstants.E_MAILBOX);
            mbx.addAttribute(AdminConstants.A_MT_ID, mailbox.id);
            mbx.addAttribute(AdminConstants.A_MT_GROUPID, mailbox.schemaGroupId);
            mbx.addAttribute(AdminConstants.A_MT_ACCOUNTID, mailbox.accountId);
            mbx.addAttribute(AdminConstants.A_MT_INDEXVOLUMEID, mailbox.indexVolumeId);
            mbx.addAttribute(AdminConstants.A_MT_ITEMIDCHECKPOINT, mailbox.lastItemId);
            mbx.addAttribute(AdminConstants.A_MT_CONTACTCOUNT, mailbox.contacts);
            mbx.addAttribute(AdminConstants.A_MT_SIZECHECKPOINT, mailbox.size);
            mbx.addAttribute(AdminConstants.A_MT_CHANGECHECKPOINT, mailbox.lastChangeId);
            mbx.addAttribute(AdminConstants.A_MT_TRACKINGSYNC, mailbox.trackSync);
            mbx.addAttribute(AdminConstants.A_MT_TRACKINGIMAP, mailbox.trackImap);
            if (mailbox.lastBackupDate > 0)
                mbx.addAttribute(AdminConstants.A_MT_LASTBACKUPAT, mailbox.lastBackupDate);
            // mbx.addAttribute(AdminConstants.A_MT_COMMENT, mailbox.comment);
            mbx.addAttribute(AdminConstants.A_MT_LASTSOAPACCESS, mailbox.lastWriteDate);
            mbx.addAttribute(AdminConstants.A_MT_NEWNESSAGES, mailbox.recentMessages);
        }
        response.addAttribute(AdminConstants.A_MORE, i < mailboxes.size());
        response.addAttribute(AdminConstants.A_SEARCH_TOTAL, mailboxes.size());
        return response;
    }

    protected class MailboxesParams {
        String mSortBy;
        boolean mSortAscending;

        List<Mailbox.MailboxData> mResult;

        // sorting now supported for now
        MailboxesParams() {
        }

        // sorting not supported for now, keep this signature to reinstate sorting if needed
        /*
        MailboxesParams(String sortBy, boolean sortAscending) {
            mSortBy = (sortBy == null) ? "" : sortBy;
            mSortAscending = sortAscending;
        }
        */

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MailboxesParams)) return false;
            if (o == this) return true;

            return true;
            /*
            MailboxesParams other = (MailboxesParams) o;
            return
                mSortBy.equals(other.mSortBy) &&
                mSortAscending == other.mSortAscending;
            */
        }

        List<Mailbox.MailboxData> doSearch() throws ServiceException {
            if (mResult != null) return mResult;

            List<Mailbox.MailboxData> result = null;
            DbConnection conn = null;
            try {
                conn = DbPool.getConnection();
                result = DbMailbox.getMailboxRawData(conn);
            } finally {
                DbPool.quietClose(conn);
            }
            mResult = result;
            return mResult;
        }
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
    }
}
