// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AlwaysOnCluster;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAllAlwaysOnClusters extends AdminDocumentHandler {

    public static final String BY_NAME = "name";
    public static final String BY_ID = "id";

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        List<AlwaysOnCluster> clusters = prov.getAllAlwaysOnClusters();

        AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);

        Element response = zsc.createElement(AdminConstants.GET_ALL_ALWAYSONCLUSTERS_RESPONSE);
        for (Iterator<AlwaysOnCluster> it = clusters.iterator(); it.hasNext(); ) {
            AlwaysOnCluster cluster = it.next();
            if (aac.hasRightsToList(cluster, Admin.R_listAlwaysOnCluster, null))
                GetAlwaysOnCluster.encodeAlwaysOnCluster(response, cluster, null, aac.getAttrRightChecker(cluster));
        }

        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_listAlwaysOnCluster);
        relatedRights.add(Admin.R_getAlwaysOnCluster);

        notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
    }
}

