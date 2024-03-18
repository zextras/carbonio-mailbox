// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.zimbra.cs.mailbox.MailItem;

/**
 * @since Oct 22, 2004
 * @author tim
 */
final class EmptyQueryResults extends ZimbraQueryResultsImpl {

    EmptyQueryResults(Set<MailItem.Type> types, SortBy searchOrder, SearchParams.Fetch fetch) {
        super(types, searchOrder, fetch);
    }

    @Override
    public long getCursorOffset() {
        return 0;
    }

    @Override
    public void resetIterator()  {
    }

    @Override
    public ZimbraHit getNext() {
        return null;
    }

    @Override
    public ZimbraHit peekNext() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public ZimbraHit skipToHit(int hitNo) {
        return null;
    }

    @Override
    public List<QueryInfo> getResultInfo() {
        return new ArrayList<>();
    }

}
