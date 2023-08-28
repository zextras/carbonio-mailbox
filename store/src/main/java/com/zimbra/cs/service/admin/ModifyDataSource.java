// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyDataSourceRequest;
import com.zimbra.soap.admin.type.DataSourceInfo;
import com.zimbra.soap.admin.type.DataSourceType;

public class ModifyDataSource extends AdminDocumentHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_ID };
    @Override
    protected String[] getProxiedAccountPath()  { return TARGET_ACCOUNT_PATH; }

    /**
     * must be careful and only allow modifies to accounts/attrs domain admin has access to
     */
    @SuppressWarnings("rawtypes")
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
    public Element handle(Element request, Map<String, Object> context) throws ServiceException, SoapFaultException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        ModifyDataSourceRequest req = zsc.elementToJaxb(request);
        String id = req.getId();
        if (null == id) {
            throw ServiceException.INVALID_REQUEST("missing required attribute: " + AdminConstants.E_ID, null);
        }
        Account account = prov.get(AccountBy.id, id, zsc.getAuthToken());
        defendAgainstAccountOrCalendarResourceHarvesting(account, AccountBy.id, id, zsc,
                Admin.R_adminLoginAs, Admin.R_adminLoginCalendarResourceAs);

        DataSourceInfo dataSource = req.getDataSource();
        Map<String, Object> attrs = dataSource.getAttrsAsOldMultimap();

        String dsId = dataSource.getId();
        DataSource ds = prov.get(account, Key.DataSourceBy.id, dsId);
        if (ds == null) {
            throw ServiceException.INVALID_REQUEST("Cannot find data source with id=" + dsId, null);
        }

        DataSourceType type = ds.getType();

        // Note: isDomainAdminOnly *always* returns false for pure ACL based AccessManager
        if (isDomainAdminOnly(zsc)) {
            // yuck, can't really integrate into AdminDocumentHandler methods
            // have to check separately here
            AttributeClass klass = ModifyDataSource.getAttributeClassFromType(type);
            checkModifyAttrs(zsc, klass, attrs);
        }

        ZimbraLog.addDataSourceNameToContext(ds.getName());

        prov.modifyDataSource(account, dsId, attrs);

        Element response = zsc.createElement(AdminConstants.MODIFY_DATA_SOURCE_RESPONSE);
        return response;
    }

    static AttributeClass getAttributeClassFromType(DataSourceType type) {
        if (type == DataSourceType.pop3)
            return AttributeClass.pop3DataSource;
        else if (type == DataSourceType.imap)
            return AttributeClass.imapDataSource;
        else if (type == DataSourceType.rss)
            return AttributeClass.rssDataSource;
        else if (type == DataSourceType.gal)
            return AttributeClass.galDataSource;
        else if (type == DataSourceType.oauth2contact || type == DataSourceType.oauth2calendar)
            return AttributeClass.oauth2DataSource;
        else
            return AttributeClass.dataSource;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_adminLoginAs);
        relatedRights.add(Admin.R_adminLoginCalendarResourceAs);
        notes.add(AdminRightCheckPoint.Notes.ADMIN_LOGIN_AS);
    }
}
