// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetAccountMembershipRequest;

/**
 * @author schemers
 */
public class GetAccountMembership extends AdminDocumentHandler {

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
        GetAccountMembershipRequest req = zsc.elementToJaxb(request);
        AccountBy acctBy = req.getAccount().getBy().toKeyAccountBy();
        String accountSelectorKey = req.getAccount().getKey();
        Account account = prov.get(acctBy, accountSelectorKey, zsc.getAuthToken());
        defendAgainstAccountHarvesting(account, acctBy, accountSelectorKey, zsc, Admin.R_getAccountMembership);

        HashMap<String,String> via = new HashMap<>();
        List<Group> groups = prov.getGroups(account, false, via);

        Element response = zsc.createElement(AdminConstants.GET_ACCOUNT_MEMBERSHIP_RESPONSE);
        for (Group group: groups) {
            Element eDL = response.addNonUniqueElement(AdminConstants.E_DL);
            eDL.addAttribute(AdminConstants.A_NAME, group.getName());
            eDL.addAttribute(AdminConstants.A_ID,group.getId());
            eDL.addAttribute(AdminConstants.A_DYNAMIC, group.isDynamic());
            String viaDl = via.get(group.getName());
            if (viaDl != null) {
                eDL.addAttribute(AdminConstants.A_VIA, viaDl);
            }

            try {
                if (group.isDynamic()) {
                    checkDynamicGroupRight(zsc, (DynamicGroup) group, needGetAttrsRight());
                } else {
                    checkDistributionListRight(zsc, (DistributionList) group, needGetAttrsRight());
                }

                String isAdminGroup = group.getAttr(Provisioning.A_zimbraIsAdminGroup);
                if (isAdminGroup != null) {
                    eDL.addNonUniqueElement(AdminConstants.E_A)
                        .addAttribute(AdminConstants.A_N, Provisioning.A_zimbraIsAdminGroup).setText(isAdminGroup);
                }
            } catch (ServiceException e) {
                if (ServiceException.PERM_DENIED.equals(e.getCode())) {
                    ZimbraLog.acl.warn("no permission to view %s of dl %s",
                            Provisioning.A_zimbraIsAdminGroup, group.getName());
                }
            }
        }
        return response;
    }

    private Set<String> needGetAttrsRight() {
        Set<String> attrsNeeded = new HashSet<>();
        attrsNeeded.add(Provisioning.A_zimbraIsAdminGroup);
        return attrsNeeded;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getAccountMembership);
        notes.add("If the authed admin has get attr right on  distribution list attr " +
                Provisioning.A_zimbraIsAdminGroup + ", it is returned in the response if set.");
    }
}
