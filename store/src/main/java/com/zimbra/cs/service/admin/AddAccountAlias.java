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

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AddAccountAliasRequest;
import com.zimbra.soap.admin.message.AddAccountAliasResponse;

/**
 * @author schemers
 */
public class AddAccountAlias extends AdminDocumentHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_ID };
    @Override
    protected String[] getProxiedAccountPath()  { return TARGET_ACCOUNT_PATH; }

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
        AddAccountAliasRequest req = zsc.elementToJaxb(request);

        String id = req.getId();
        String alias = req.getAlias();

        Account account = prov.get(AccountBy.id, id);

        defendAgainstAccountOrCalendarResourceHarvesting(account, AccountBy.id, id, zsc,
                Admin.R_addAccountAlias, Admin.R_addCalendarResourceAlias);

        // if the admin can create an alias in the domain
        checkDomainRightByEmail(zsc, alias, Admin.R_createAlias);

        prov.addAlias(account, alias);
        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "AddAccountAlias","name", account.getName(), "alias", alias}));

        return zsc.jaxbToElement(new AddAccountAliasResponse());
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_addCalendarResourceAlias);
        relatedRights.add(Admin.R_addAccountAlias);
        relatedRights.add(Admin.R_createAlias);

        notes.add("Need " + Admin.R_createAlias.getName() + " right on the domain in which the alias is to be created.");
        notes.add("Need " + Admin.R_addAccountAlias.getName() + " right if adding alias for an account.");
        notes.add("Need " + Admin.R_addCalendarResourceAlias.getName() + " right if adding alias for a calendar resource.");
    }
}
