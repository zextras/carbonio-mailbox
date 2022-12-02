// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.zimbra.common.service.ServiceException;

/**
 * Groups hit results for various reasons.
 */
public abstract class BufferingResultsGrouper implements ZimbraQueryResults {

    protected final ZimbraQueryResults hits;
    protected List<ZimbraHit> bufferedHit = new LinkedList<ZimbraHit>();
    protected boolean atStart = true;

    /**
     * Fills the hit buffer if necessary.  May be called even if the buffer has entries in it,
     * implementation may ignore it (but must return true) in those cases.
     *
     * @return TRUE if there some hits in the buffer, FALSE if not.
     */
    protected abstract boolean bufferHits() throws ServiceException;

    @Override
    public SortBy getSortBy() {
        return hits.getSortBy();
    }

    public BufferingResultsGrouper(ZimbraQueryResults hits) {
        this.hits = hits;
    }

    @Override
    public long getCursorOffset() {
        return hits.getCursorOffset();
    }

    @Override
    public void resetIterator() throws ServiceException {
        if (!atStart) {
            bufferedHit.clear();
            hits.resetIterator();
            atStart = true;
        }
    }

    @Override
    public boolean hasNext() throws ServiceException {
        return bufferHits();
    }

    @Override
    public ZimbraHit peekNext() throws ServiceException {
        if (bufferHits()) {
            return bufferedHit.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ZimbraHit skipToHit(int hitNo) throws ServiceException {
        resetIterator();
        for (int i = 0; i < hitNo; i++) {
            if (!hasNext()) {
                return null;
            }
            getNext();
        }
        return getNext();
    }

    @Override
    public ZimbraHit getNext() throws ServiceException {
        atStart = false;
        if (bufferHits()) {
            return bufferedHit.remove(0);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        hits.close();
    }

    @Override
    public List<QueryInfo> getResultInfo() {
        return hits.getResultInfo();
    }

    @Override
    public boolean isPreSorted() {
        return hits.isPreSorted();
    }
}
