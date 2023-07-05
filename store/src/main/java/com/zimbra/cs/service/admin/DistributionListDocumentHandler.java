// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.ZimbraSoapContext;

public abstract class DistributionListDocumentHandler extends AdminDocumentHandler {

    private static final String GROUP = "__GROUP__";

    protected abstract Group getGroup(Element request) throws ServiceException;

    protected final Group getGroupAndCacheInContext(Element request, Map<String, Object> context)
    throws ServiceException {
        Group group = getGroup(request);
        if (group != null) {
            context.put(GROUP, group);
        }
        return group;
    }

    protected Group getGroupFromContext(Map<String, Object> context) throws ServiceException {
        return (Group) context.get(GROUP);
    }

    @Override
    protected Element proxyIfNecessary(Element request, Map<String, Object> context)
    throws ServiceException {
        // if we've explicitly been told to execute here, don't proxy
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        if (zsc.getProxyTarget() != null) {
            return null;
        }

        // check whether we need to proxy to the home server of a group
        try {
            Group group = getGroupAndCacheInContext(request, context);

            if (group != null && !Provisioning.onLocalServer(group)) {
                Server server = group.getServer();
                if (server == null) {
                    throw ServiceException.PROXY_ERROR(
                            AccountServiceException.NO_SUCH_SERVER(
                            group.getAttr(Provisioning.A_zimbraMailHost)), "");
                }
                return proxyRequest(request, context, server);
            }

            return super.proxyIfNecessary(request, context);
        } catch (ServiceException e) {
            // if something went wrong proxying the request, just execute it locally
            if (ServiceException.PROXY_ERROR.equals(e.getCode())) {
                return null;
            }
            // but if it's a real error, it's a real error
            throw e;
        }
    }
}
