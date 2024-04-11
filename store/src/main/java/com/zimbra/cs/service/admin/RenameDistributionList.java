// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.RenameDistributionListRequest;

public class RenameDistributionList extends AdminDocumentHandler {

    /**
     * must be careful and only allow access to domain if domain admin
     */
    @Override
    public boolean domainAuthSufficient(Map context) {
        return true;
    }

    /**
     * @return true - which means accept responsibility for measures to prevent account harvesting by delegate admins
     */
    @Override
    public boolean defendsAgainstDelegateAdminAccountHarvesting() {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        RenameDistributionListRequest req = zsc.elementToJaxb(request);
        String id = req.getId();
        String newName = req.getNewName();
        Group group = prov.getGroup(DistributionListBy.id, id);
        defendAgainstGroupHarvesting(group, DistributionListBy.id, id, zsc,
                Admin.R_renameGroup, Admin.R_renameDistributionList);

        // check if the admin can "create DL" in the domain (can be same or diff)
        checkDomainRightByEmail(zsc, newName, Admin.R_createDistributionList);

        String oldName = group.getName();
        prov.renameGroup(id, newName);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "RenameDistributionList", "name", oldName, "newName", newName}));

        // get again with new name...
        group = prov.getGroup(DistributionListBy.id, id);
        if (group == null) {
            throw ServiceException.FAILURE("unable to get distribution list after rename: " + id, null);
        }
        Element response = zsc.createElement(AdminConstants.RENAME_DISTRIBUTION_LIST_RESPONSE);
        GetDistributionList.encodeDistributionList(response, group);
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_renameDistributionList);
        relatedRights.add(Admin.R_createDistributionList);
    }
}
