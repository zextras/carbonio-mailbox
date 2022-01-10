// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * A {@link QueryOperation} that filters results out of the result set. The base
 * class is a nop (passes through all hits).
 * <p>
 * Currently used only as a base class for other QueryOps that have to do
 * passthrough/filtering.
 */
abstract class FilterQueryOperation extends QueryOperation {

    protected QueryOperation operation;

    @Override
    protected QueryOperation combineOps(QueryOperation other, boolean union) {
        return null;
    }

    @Override
    protected void depthFirstRecurse(RecurseCallback cb) {
        operation.depthFirstRecurse(cb);
        cb.recurseCallback(this);
    }

    @Override
    QueryOperation expandLocalRemotePart(Mailbox mbox) throws ServiceException {
        operation.expandLocalRemotePart(mbox);
        return this;
    }

    @Override
    QueryOperation ensureSpamTrashSetting(Mailbox mbox, boolean includeTrash, boolean includeSpam)
        throws ServiceException {
        return operation.ensureSpamTrashSetting(mbox, includeTrash, includeSpam);
    }

    @Override
    void forceHasSpamTrashSetting() {
        operation.forceHasSpamTrashSetting();
    }

    @Override
    Set<QueryTarget> getQueryTargets() {
        return operation.getQueryTargets();
    }

    @Override
    boolean hasAllResults() {
        return operation.hasAllResults();
    }

    @Override
    boolean hasNoResults() {
        return operation.hasNoResults();
    }

    @Override
    boolean hasSpamTrashSetting() {
        return operation.hasSpamTrashSetting();
    }

    @Override
    QueryOperation optimize(Mailbox mbox) throws ServiceException {
        // optimize our sub-op, but *don't* optimize us out
        operation = operation.optimize(mbox);
        return this;
    }

    @Override
    protected void begin(QueryContext ctx) throws ServiceException {
        assert(context == null);
        context = ctx;
        operation.begin(ctx);
    }

    @Override
    String toQueryString() {
        return operation.toQueryString();
    }

    @Override
    public void close() throws IOException {
        operation.close();
    }

    @Override
    public ZimbraHit getNext() throws ServiceException {
        ZimbraHit hit = peekNext();
        if (hit != null) {
            operation.getNext(); // skip the current hit
        }
        return hit;
    }

    @Override
    public List<QueryInfo> getResultInfo() {
        return operation.getResultInfo();
    }

    @Override
    public ZimbraHit peekNext() throws ServiceException {
        return operation.peekNext();
    }

    @Override
    public void resetIterator() throws ServiceException {
        operation.resetIterator();
    }
}
