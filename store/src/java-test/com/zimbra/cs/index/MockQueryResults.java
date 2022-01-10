// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;

/**
 * Mock implementation of {@link ZimbraQueryResults} for testing.
 *
 * @author ysasaki
 */
public final class MockQueryResults extends ZimbraQueryResultsImpl {

    private List<ZimbraHit> hits = new ArrayList<ZimbraHit>();
    private int next = 0;
    private final List<QueryInfo> queryInfo = new ArrayList<QueryInfo>();

    public MockQueryResults(Set<MailItem.Type> types, SortBy sort) {
        super(types, sort, SearchParams.Fetch.NORMAL);
    }

    public void add(ZimbraHit hit) {
        hits.add(hit);
    }

    @Override
    public long getCursorOffset() {
        return -1;
    }

    @Override
    public void resetIterator() {
        next = 0;
    }

    @Override
    public ZimbraHit getNext() {
        return hits.get(next++);
    }

    @Override
    public ZimbraHit peekNext() {
        return hits.get(next);
    }

    @Override
    public ZimbraHit skipToHit(int hitNo) throws ServiceException {
        next = hitNo;
        return getNext();
    }

    @Override
    public boolean hasNext() throws ServiceException {
        return next < hits.size();
    }

    @Override
    public void close() {
        hits = null;
    }

    @Override
    public List<QueryInfo> getResultInfo() {
        return queryInfo;
    }

}
