// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AlwaysOnCluster;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

public final class ModifyAlwaysOnCluster extends AdminDocumentHandler {

    private static final String[] TARGET_SERVER_PATH = new String[] { AdminConstants.E_ID };

    @Override
    protected String[] getProxiedServerPath() {
        return TARGET_SERVER_PATH;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        String id = request.getAttribute(AdminConstants.E_ID);
        Map<String, Object> attrs = AdminService.getAttrs(request);

        AlwaysOnCluster cluster = prov.get(Key.AlwaysOnClusterBy.id, id);
        if (cluster == null) {
            throw AccountServiceException.NO_SUCH_ALWAYSONCLUSTER(id);
        }
        checkRight(zsc, context, cluster, attrs);

        // pass in true to checkImmutable
        prov.modifyAttrs(cluster, attrs, true);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "ModifyAlwaysOnCluster","name", cluster.getName()}, attrs));

        Element response = zsc.createElement(AdminConstants.MODIFY_ALWAYSONCLUSTER_RESPONSE);
        GetAlwaysOnCluster.encodeCluster(response, cluster);
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY,
                Admin.R_modifyAlwaysOnCluster.getName(), "alwaysOnCluster"));
    }
}
