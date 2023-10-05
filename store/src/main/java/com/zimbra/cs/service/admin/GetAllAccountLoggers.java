// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.AccountLogger;
import com.zimbra.common.util.LogManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetAllAccountLoggers extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Server localServer = Provisioning.getInstance().getLocalServer();
    checkRight(zsc, context, localServer, Admin.R_manageAccountLogger);

    Provisioning prov = Provisioning.getInstance();
    Map<String, Element> accountElements = new HashMap<String, Element>();

    Element response = zsc.createElement(AdminConstants.GET_ALL_ACCOUNT_LOGGERS_RESPONSE);
    for (AccountLogger al : LogManager.getAllAccountLoggers()) {
      // Look up account
      Account account = prov.get(AccountBy.name, al.getAccountName(), zsc.getAuthToken());
      if (account == null) {
        ZimbraLog.misc.info(
            "GetAllAccountLoggers: unable to find account '%s'.  Ignoring account logger.",
            al.getAccountName());
        continue;
      }

      // Add elements
      Element eAccountLogger = accountElements.get(account.getId());
      if (eAccountLogger == null) {
        eAccountLogger = response.addElement(AdminConstants.E_ACCOUNT_LOGGER);
        accountElements.put(account.getId(), eAccountLogger);
      }
      eAccountLogger.addAttribute(AdminConstants.A_ID, account.getId());
      eAccountLogger.addAttribute(AdminConstants.A_NAME, account.getName());

      Element eLogger = eAccountLogger.addElement(AdminConstants.E_LOGGER);
      eLogger.addAttribute(AdminConstants.A_CATEGORY, al.getCategory());
      eLogger.addAttribute(AdminConstants.A_LEVEL, al.getLevel().toString());
    }

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_manageAccountLogger);
  }
}
