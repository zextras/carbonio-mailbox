// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.type.AccountSelector;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author schemers
 */
public class GetAccount extends AdminDocumentHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_ACCOUNT };
    @Override
    protected String[] getProxiedAccountElementPath()  { return TARGET_ACCOUNT_PATH; }

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

        GetAccountRequest req = zsc.elementToJaxb(request);

        AccountSelector acctSel = req.getAccount();
        if (acctSel == null) {
            throw ServiceException.INVALID_REQUEST(String.format("missing <%s>", AdminConstants.E_ACCOUNT), null);
        }
        String accountSelectorKey = acctSel.getKey();
        AccountBy by = acctSel.getBy().toKeyAccountBy();

        Account account = prov.get(by, accountSelectorKey, zsc.getAuthToken());
        defendAgainstAccountHarvesting(account, by, accountSelectorKey, zsc, Admin.R_getAccountInfo);

        AdminAccessControl aac = checkAccountRight(zsc, account, AdminRight.PR_ALWAYS_ALLOW);
        Element response = zsc.createElement(AdminConstants.GET_ACCOUNT_RESPONSE);

        boolean applyCos = !Boolean.FALSE.equals(req.isApplyCos());
        Set<String> reqAttrs = getReqAttrs(req.getAttrs(), AttributeClass.account);
        ToXML.encodeAccount(response, account, applyCos, reqAttrs, aac.getAttrRightChecker(account));

        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getAccount);
        notes.add(String.format(AdminRightCheckPoint.Notes.GET_ENTRY, Admin.R_getAccount.getName()));
    }
}
