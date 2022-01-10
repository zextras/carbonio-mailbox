// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.admin;


import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetDataSourcesRequest;

/**
 * @author schemers
 */
public class GetDataSources extends AdminDocumentHandler  {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_ID };
    @Override
    protected String[] getProxiedAccountPath()  { return TARGET_ACCOUNT_PATH; }

    /**
     * must be careful and only allow modifies to accounts/attrs domain admin has access to
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
        GetDataSourcesRequest req = zsc.elementToJaxb(request);
        String id = req.getId();
        Account account = prov.get(AccountBy.id, id, zsc.getAuthToken());
        defendAgainstAccountOrCalendarResourceHarvesting(account, AccountBy.id, id, zsc,
                Admin.R_adminLoginAs, Admin.R_adminLoginCalendarResourceAs);

        Element response = zsc.createElement(AdminConstants.GET_DATA_SOURCES_RESPONSE);
        List<DataSource> sources = Provisioning.getInstance().getAllDataSources(account);
        for (DataSource ds : sources) {
            com.zimbra.cs.service.account.ToXML.encodeDataSource(response, ds);
        }
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_adminLoginAs);
        relatedRights.add(Admin.R_adminLoginCalendarResourceAs);
        notes.add(AdminRightCheckPoint.Notes.ADMIN_LOGIN_AS);
    }
}

