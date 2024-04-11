// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ShareInfo;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.account.GetShareInfo.ResultFilter;
import com.zimbra.cs.service.account.GetShareInfo.ResultFilterByTarget;
import com.zimbra.cs.service.account.GetShareInfo.ShareInfoVisitor;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetShareInfoRequest;
import com.zimbra.soap.admin.message.GetShareInfoResponse;
import com.zimbra.soap.type.GranteeChooser;

public class GetShareInfo extends ShareInfoHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_OWNER };
    @Override
    protected String[] getProxiedAccountElementPath()  { return TARGET_ACCOUNT_PATH; }

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
        OperationContext octxt = getOperationContext(zsc, context);
        Provisioning prov = Provisioning.getInstance();
        GetShareInfoRequest req = zsc.elementToJaxb(request);
        GranteeChooser granteeChooser = req.getGrantee();

        byte granteeType = com.zimbra.cs.service.account.GetShareInfo.getGranteeType(
                granteeChooser == null ? null : granteeChooser.getType());
        String granteeId = null;
        String granteeName = null;
        if (granteeChooser != null) {
            granteeId = granteeChooser.getId();
            granteeName = granteeChooser.getName();
        }

        Account ownerAcct = null;
        AccountBy acctBy = req.getOwner().getBy().toKeyAccountBy();
        String accountSelectorKey = req.getOwner().getKey();
        ownerAcct = prov.get(acctBy, accountSelectorKey);

        // in the account namespace GetShareInfo
        // to defend against harvest attacks return "no shares" instead of error when an invalid user name/id is used.
        // this is the admin namespace GetShareInfo, we want to let the admin know if the owner name is bad
        // unless the admin is a domain admin for a different domain...
        defendAgainstAccountOrCalendarResourceHarvesting(ownerAcct, acctBy, accountSelectorKey, zsc,
                Admin.R_adminLoginAs, Admin.R_adminLoginCalendarResourceAs);
        GetShareInfoResponse response = new GetShareInfoResponse();

        ResultFilter resultFilter = new ResultFilterByTarget(granteeId, granteeName);
        ShareInfoVisitor visitor = new ShareInfoVisitor(prov, response, null, resultFilter);
        ShareInfo.Discover.discover(octxt, prov, null, granteeType, ownerAcct, visitor);
        visitor.finish();

        return zsc.jaxbToElement(response);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_adminLoginAs);
        relatedRights.add(Admin.R_adminLoginCalendarResourceAs);
        notes.add(AdminRightCheckPoint.Notes.ADMIN_LOGIN_AS);
    }
}
