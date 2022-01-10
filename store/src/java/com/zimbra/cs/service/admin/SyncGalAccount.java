// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DataSource.DataImport;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.gal.GalGroup;
import com.zimbra.cs.gal.GalImport;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.type.DataSourceType;

public final class SyncGalAccount extends AdminDocumentHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_ACCOUNT, AdminConstants.A_ID };

    @Override
    protected String[] getProxiedAccountPath() {
        return TARGET_ACCOUNT_PATH;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.TODO); //TODO
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        ZimbraLog.addToContext(ZimbraLog.C_ANAME, getAuthenticatedAccount(zsc).getName());

        for (Element accountEl : request.listElements(AdminConstants.E_ACCOUNT)) {
            String accountId = accountEl.getAttribute(AdminConstants.A_ID);
            Account account = prov.getAccountById(accountId);
            if (account == null) {
                throw AccountServiceException.NO_SUCH_ACCOUNT(accountId);
            }
            ZimbraLog.addToContext(ZimbraLog.C_NAME, account.getName());

            for (Element dsEl : accountEl.listElements(AdminConstants.E_DATASOURCE)) {
                String by = dsEl.getAttribute(AdminConstants.A_BY);
                String name = dsEl.getText();
                DataSource ds = by.equals("id") ? account.getDataSourceById(name) : account.getDataSourceByName(name);
                if (ds == null) {
                    throw AccountServiceException.NO_SUCH_DATA_SOURCE(name);
                }
                if (!ds.getType().equals(DataSourceType.gal)) {
                    continue;
                }
                boolean fullSync = dsEl.getAttributeBool(AdminConstants.A_FULLSYNC, false);
                boolean reset = dsEl.getAttributeBool(AdminConstants.A_RESET, false);
                int fid = ds.getFolderId();

                DataImport dataImport = DataSourceManager.getInstance().getDataImport(ds);
                if (dataImport instanceof GalImport) {
                    ((GalImport) dataImport).importGal(fid, (reset ? reset : fullSync), reset);
                }
                //flush domain gal group cache
                Domain domain = prov.getDomain(account);
                GalGroup.flushCache(domain);
            }
        }

        return zsc.createElement(AdminConstants.SYNC_GAL_ACCOUNT_RESPONSE);
    }

}
