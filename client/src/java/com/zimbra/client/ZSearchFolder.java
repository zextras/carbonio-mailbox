// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import org.json.JSONException;

import com.google.common.base.Joiner;
import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.client.event.ZModifySearchFolderEvent;
import com.zimbra.common.mailbox.SearchFolderStore;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.soap.mail.type.SearchFolder;
import com.zimbra.soap.type.SearchSortBy;

public final class ZSearchFolder extends ZFolder implements SearchFolderStore {

    private String query;
    private String types;
    private SearchSortBy sortBy;

    public ZSearchFolder(Element e, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        super(e, parent, mailbox);
        query = e.getAttribute(MailConstants.A_QUERY);
        types = e.getAttribute(MailConstants.A_SEARCH_TYPES, null);
        try {
            sortBy = SearchSortBy.fromString(e.getAttribute(MailConstants.A_SORTBY, SearchSortBy.dateDesc.name()));
        } catch (ServiceException se) {
            sortBy = SearchSortBy.dateDesc;
        }
    }

    public ZSearchFolder(SearchFolder f, ZFolder parent, ZMailbox mailbox) throws ServiceException {
        super(f, parent, mailbox);
        query = f.getQuery();
        types = Joiner.on(',').join(f.getTypes());
        try {
            sortBy = SearchSortBy.fromString(SystemUtil.coalesce(f.getSortBy(), SearchSortBy.dateDesc).toString());
        } catch (ServiceException se) {
            sortBy = SearchSortBy.dateDesc;
        }
    }

    @Override
    public void modifyNotification(ZModifyEvent e) throws ServiceException {
        if (e instanceof ZModifySearchFolderEvent) {
            ZModifySearchFolderEvent sfe = (ZModifySearchFolderEvent) e;
            if (sfe.getId().equals(getId())) {
                query = sfe.getQuery(query);
                types = sfe.getTypes(types);
                sortBy = sfe.getSortBy(sortBy);
                super.modifyNotification(e);
            }
        }
    }

    @Override
    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject jo = super.toZJSONObject();
        jo.put("query", query);
        jo.put("types", types);
        jo.put("sortBy", sortBy.name());
        return jo;
    }

    @Override
    public String getQuery() {
        return query;
    }

    public SearchSortBy getSortBy() {
        return sortBy;
    }

    public String getTypes() {
        return types;
    }

    /** Returns the set of item types returned by this search, or <code>""</code> if none were specified. */
    @Override
    public String getReturnTypes() {
        return (types == null ? "" : types);
    }

    @Override
    public String toString() {
        return String.format("[ZSearchFolder %s]", getPath());
    }

    @Override
    public ZSearchContext getSearchContext() {
        ZSearchParams params = new ZSearchParams(query);
        params.setTypes(types);
        params.setSortBy(sortBy);
        return new ZSearchContext(params,getMailbox());
    }

}
