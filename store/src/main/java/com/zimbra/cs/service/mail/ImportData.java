// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.soap.ZimbraSoapContext;


public class ImportData extends MailDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        Account account = getRequestedAccount(zsc);

        for (Element elem : request.listElements()) {
            DataSource ds;

            String name, id = elem.getAttribute(MailConstants.A_ID, null);
            if (id != null) {
                ds = prov.get(account, Key.DataSourceBy.id, id);
                if (ds == null) {
                    throw ServiceException.INVALID_REQUEST("Could not find Data Source with id " + id, null);
                }
            } else if ((name = elem.getAttribute(MailConstants.A_NAME, null)) != null) {
                ds = prov.get(account, Key.DataSourceBy.name, name);
                if (ds == null) {
                    throw ServiceException.INVALID_REQUEST("Could not find Data Source with name " + name, null);
                }
            } else {
                throw ServiceException.INVALID_REQUEST("must specify either 'id' or 'name'", null);
            }

            ZimbraLog.addDataSourceNameToContext(ds.getName());
            DataSourceManager.asyncImportData(ds);
        }

        Element response = zsc.createElement(MailConstants.IMPORT_DATA_RESPONSE);
        return response;
    }

}
