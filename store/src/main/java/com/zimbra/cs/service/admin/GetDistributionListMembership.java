// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetDistributionListMembershipRequest;
import com.zimbra.soap.admin.type.DistributionListSelector;

/**
 * @author schemers
 */
public class GetDistributionListMembership extends AdminDocumentHandler {

    /**
     * must be careful and only return accounts a domain admin can see
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
        GetDistributionListMembershipRequest req = zsc.elementToJaxb(request);

        int limit = (req.getLimit() == null) ? 0 : req.getLimit();
        if (limit < 0) {
            throw ServiceException.INVALID_REQUEST("limit" + limit + " is negative", null);
        }
        int offset = (req.getOffset() == null) ? 0 : req.getOffset();
        if (offset < 0) {
            throw ServiceException.INVALID_REQUEST("offset" + offset + " is negative", null);
        }

        DistributionListSelector dlSel = req.getDl();
        DistributionListBy dlBy = dlSel.getBy().toKeyDistributionListBy();
        String dlKey = dlSel.getKey();

        DistributionList distributionList = prov.get(dlBy, dlKey);
        defendAgainstGroupHarvesting(distributionList, dlBy, dlKey, zsc,
                Admin.R_getDistributionListMembership /* shouldn't be used */,
                Admin.R_getDistributionListMembership);

        HashMap<String,String> via = new HashMap<>();
        List<DistributionList> lists = prov.getDistributionLists(distributionList, false, via);

        Element response = zsc.createElement(AdminConstants.GET_DISTRIBUTION_LIST_MEMBERSHIP_RESPONSE);
        for (DistributionList dl: lists) {
            Element dlEl = response.addNonUniqueElement(AdminConstants.E_DL);
            dlEl.addAttribute(AdminConstants.A_NAME, dl.getName());
            dlEl.addAttribute(AdminConstants.A_ID,dl.getId());
            String viaDl = via.get(dl.getName());
            if (viaDl != null) dlEl.addAttribute(AdminConstants.A_VIA, viaDl);
        }
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getDistributionListMembership);
    }
}
