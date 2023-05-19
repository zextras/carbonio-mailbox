// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;

public abstract class ReloadMemberPostProxyHandler extends
        DistributionListDocumentHandler {

    @Override
    public boolean domainAuthSufficient(@SuppressWarnings("rawtypes") Map context) {
        return true;
    }

    @Override
    protected Group getGroup(Element request) throws ServiceException {
        String id = request.getAttribute(AdminConstants.E_ID);
        return Provisioning.getInstance().getGroup(DistributionListBy.id, id);
    }

    protected List<String> getMemberList(Element request, Map<String, Object> context) throws ServiceException {
        List<String> memberList = new LinkedList<>();
        for (Element elem : request.listElements(AdminConstants.E_DLM)) {
            memberList.add(elem.getTextTrim());
        }
        return memberList;
    }

    @Override
    public void postProxy(Element request, Element response,
            Map<String, Object> context) throws ServiceException {
        List<String> memberList = getMemberList(request, context);
        Provisioning prov = Provisioning.getInstance();
        for (String memberName : memberList) {
            Account acct = prov.get(AccountBy.name, memberName);
            if (acct != null) {
                prov.reload(acct);
            }
        }
    }
}
