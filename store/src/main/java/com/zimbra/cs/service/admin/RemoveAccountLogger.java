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
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * Removes a custom logger from the given account.
 * 
 * @author bburtin
 */
public class RemoveAccountLogger extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        
        Server localServer = Provisioning.getInstance().getLocalServer();
        checkRight(zsc, context, localServer, Admin.R_manageAccountLogger);
        
        // Look up account, if specified.
        Account account = null;
        String accountName = null;
        if (request.getOptionalElement(AdminConstants.E_ID) != null ||
            request.getOptionalElement(AdminConstants.E_ACCOUNT) != null) {
            account = AddAccountLogger.getAccountFromLoggerRequest(request);
            accountName = account.getName();
        }
        
        // Look up log category, if specified.
        Element eLogger = request.getOptionalElement(AdminConstants.E_LOGGER);
        String category = null;
        if (eLogger != null) {
            category = eLogger.getAttribute(AdminConstants.A_CATEGORY);
            if (category.equalsIgnoreCase(AddAccountLogger.CATEGORY_ALL)) {
                category = null;
            } else if (!LogFactory.logExists(category)) {
                throw ServiceException.INVALID_REQUEST("Log category " + category + " does not exist.", null);
            }
        }

        // Do the work.
        for (Log log : LogFactory.getAllLoggers()) {
            if (category == null || log.getCategory().equals(category)) {
                if (accountName != null) {
                    boolean removed = log.removeAccountLogger(accountName);
                    if (removed) {
                        ZimbraLog.misc.info("Removed logger for account %s from category %s.",
                            accountName, log.getCategory());
                    }
                } else {
                    int count = log.removeAccountLoggers();
                    if (count > 0) {
                        ZimbraLog.misc.info("Removed %d custom loggers from category %s.",
                            count, log.getCategory());
                    }
                }
            }
        }
        
        // Send response.
        Element response = zsc.createElement(AdminConstants.REMOVE_ACCOUNT_LOGGER_RESPONSE);
        return response;
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_manageAccountLogger);
    }
}
