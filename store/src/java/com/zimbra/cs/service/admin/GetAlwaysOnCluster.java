// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccessManager.AttrRightChecker;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AlwaysOnCluster;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAlwaysOnCluster extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        Set<String> reqAttrs = getReqAttrs(request, AttributeClass.alwaysOnCluster);

        Element d = request.getElement(AdminConstants.E_ALWAYSONCLUSTER);
        String method = d.getAttribute(AdminConstants.A_BY);
        String name = d.getText();

        if (name == null || name.equals(""))
            throw ServiceException.INVALID_REQUEST("must specify a value for a server", null);

        AlwaysOnCluster cluster = prov.get(Key.AlwaysOnClusterBy.fromString(method), name);

        if (cluster == null)
            throw AccountServiceException.NO_SUCH_ALWAYSONCLUSTER(name);

        AdminAccessControl aac = checkRight(zsc, context, cluster, AdminRight.PR_ALWAYS_ALLOW);

        // reload the server
        prov.reload(cluster);

        Element response = zsc.createElement(AdminConstants.GET_ALWAYSONCLUSTER_RESPONSE);
        encodeAlwaysOnCluster(response, cluster, reqAttrs, aac.getAttrRightChecker(cluster));

        return response;
    }

    public static void encodeCluster(Element e, AlwaysOnCluster c) throws ServiceException {
        encodeAlwaysOnCluster(e, c, null, null);
    }

    public static void encodeAlwaysOnCluster(Element e, AlwaysOnCluster c, Set<String> reqAttrs,
            AttrRightChecker attrRightChecker) throws ServiceException {
        Element cluster = e.addElement(AdminConstants.E_ALWAYSONCLUSTER);
        cluster.addAttribute(AdminConstants.A_NAME, c.getName());
        cluster.addAttribute(AdminConstants.A_ID, c.getId());
        Map<String, Object> attrs = c.getUnicodeAttrs();

        ToXML.encodeAttrs(cluster, attrs, reqAttrs, attrRightChecker);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getAlwaysOnCluster);
        notes.add(String.format(AdminRightCheckPoint.Notes.GET_ENTRY, Admin.R_getAlwaysOnCluster.getName()));
    }
}