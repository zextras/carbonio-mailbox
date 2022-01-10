// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

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
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AddDistributionListAliasRequest;
import com.zimbra.soap.admin.message.AddDistributionListAliasResponse;

public class AddDistributionListAlias extends DistributionListDocumentHandler {

    /**
     * must be careful and only allow access to domain if domain admin
     */
    @Override
    public boolean domainAuthSufficient(Map context) {
        return true;
    }

    @Override
    protected Group getGroup(Element request) throws ServiceException {
        String id = request.getAttribute(AdminConstants.E_ID);
        return Provisioning.getInstance().getGroup(DistributionListBy.id, id);
    }

    /**
     * @return true - which means accept responsibility for measures to prevent account harvesting by delegate admins
     */
    @Override
    public boolean defendsAgainstDelegateAdminAccountHarvesting() {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        AddDistributionListAliasRequest req = zsc.elementToJaxb(request);

        String alias = req.getAlias();

        Group group = getGroupFromContext(context);
        String id = req.getId();
        defendAgainstGroupHarvesting(group, DistributionListBy.id, id, zsc,
            Admin.R_addGroupAlias, Admin.R_addDistributionListAlias);

        // if the admin can create an alias in the domain
        checkDomainRightByEmail(zsc, alias, Admin.R_createAlias);

        prov.addGroupAlias(group, alias);
        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "AddDistributionListAlias", "name", group.getName(), "alias", alias}));

        return zsc.jaxbToElement(new AddDistributionListAliasResponse());
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_addDistributionListAlias);
        relatedRights.add(Admin.R_addGroupAlias);
        relatedRights.add(Admin.R_createAlias);
    }
}
