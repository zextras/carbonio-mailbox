// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.RemoveDistributionListMemberResponse;

public class RemoveDistributionListMember extends ReloadMemberPostProxyHandler {

    /**
     * @return true - which means accept responsibility for measures to prevent account harvesting by delegate admins
     */
    @Override
    public boolean defendsAgainstDelegateAdminAccountHarvesting() {
        return true;
    }

    @Override
    protected List<String> getMemberList(Element request, Map<String, Object> context)
            throws ServiceException {
        List<String> memberList = super.getMemberList(request, context);
        Group group = getGroupFromContext(context);
        memberList = addMembersFromAccountElements(request, memberList, group);
        return memberList;
    }

    private List<String> addMembersFromAccountElements(Element request, List<String> memberList, Group group) throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        for (Element elem : request.listElements(AdminConstants.E_ACCOUNT)) {
            Set<String> listAddresses = group.getAllMembersSet();
            Account account = prov.getAccount(elem.getTextTrim());
            if(account != null) {
                if(listAddresses.contains(account.getMail())) {
                    memberList.add(account.getMail());
                }
                List<String> accountAddresses = Arrays.asList(account.getAliases());
                for(String addr : accountAddresses) {
                    if(listAddresses.contains(addr)) {
                        memberList.add(addr);
                    }
                }
            }
        }
        return memberList;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        List<String> memberList = getMemberList(request, context);

        Group group = getGroupFromContext(context);
        String id = request.getAttribute(AdminConstants.E_ID);
        defendAgainstGroupHarvesting(group, DistributionListBy.id, id, zsc,
                Admin.R_removeGroupMember, Admin.R_removeDistributionListMember);

        memberList = addMembersFromAccountElements(request, memberList, group);

        String[] members = memberList.toArray(new String[0]);
        prov.removeGroupMembers(group, members);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "RemoveDistributionListMember", "name", group.getName(),
                "member", Arrays.deepToString(members)}));
        return zsc.jaxbToElement(new RemoveDistributionListMemberResponse());
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_removeDistributionListMember);
        relatedRights.add(Admin.R_removeGroupMember);
    }
}
