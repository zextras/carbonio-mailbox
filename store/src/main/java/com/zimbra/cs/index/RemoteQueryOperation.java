// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.util.IOUtil;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.soap.SoapProtocol;

/**
 * A wrapper around a remote search (a search on data in another account).
 */
final class RemoteQueryOperation extends FilterQueryOperation {

    private ProxiedQueryResults results = null;
    private QueryTarget queryTarget = null;

    @Override
    public long getCursorOffset() {
        return -1;
    }

    /**
     * Try to OR an operation into this one.  Return FALSE if that isn't possible (incompatible query targets).
     *
     * @return FALSE
     */
    boolean tryAddOredOperation(QueryOperation op) {
        Set<QueryTarget> targets = op.getQueryTargets();
        assert(QueryTarget.getExplicitTargetCount(targets) == 1);
        assert(QueryTarget.hasExternalTarget(targets));

        for (QueryTarget target : targets) {
            assert(target != QueryTarget.LOCAL);
            if (target != QueryTarget.UNSPECIFIED) {
                if (queryTarget == null) {
                    queryTarget = target;
                } else if (!queryTarget.equals(target)) {
                    return false;
                }
            }
        }

        assert(queryTarget != null);

        if (operation == null) {
            operation = new UnionQueryOperation();
        }
        ((UnionQueryOperation) operation).add(op);
        return true;
    }

    @Override
    public String toString() {
        return "REMOTE[" + queryTarget + "]:" + operation;
    }

    protected void setup(SoapProtocol proto, AuthToken authToken, SearchParams params) throws ServiceException {
        Provisioning prov  = Provisioning.getInstance();
        Account acct = prov.get(AccountBy.id, queryTarget.toString());
        if (acct == null) {
            throw AccountServiceException.NO_SUCH_ACCOUNT(queryTarget.toString());
        }

        Server remoteServer = prov.getServer(acct);

        String queryString = operation.toQueryString();
        if (ZimbraLog.search.isDebugEnabled()) {
            ZimbraLog.search.debug("RemoteQuery=\"%s\" target=%s server=%s",
                    queryString, queryTarget, remoteServer.getName());
        }

        results = new ProxiedQueryResults(proto, authToken, queryTarget.toString(),
                remoteServer.getName(), params, queryString, params.getFetchMode());
    }

    @Override
    public void resetIterator() throws ServiceException {
        if (results != null) {
            results.resetIterator();
        }
    }

    @Override
    public ZimbraHit getNext() throws ServiceException {
        return results != null ? results.getNext() : null;
    }

    @Override
    public ZimbraHit peekNext() throws ServiceException {
        return results != null ? results.peekNext() : null;
    }

    @Override
    public void close() throws IOException {
        IOUtil.closeQuietly(results);
        super.close();
    }

    @Override
    public List<QueryInfo> getResultInfo() {
        if (results != null) {
            return results.getResultInfo();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isPreSorted() {
        return results.isPreSorted();
    }
}
