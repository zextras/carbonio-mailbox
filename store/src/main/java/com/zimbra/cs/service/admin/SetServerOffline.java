// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.zookeeper.CuratorManager;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public final class SetServerOffline extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    checkRight(zsc, context, null, AdminRight.PR_SYSTEM_ADMIN_ONLY);
    Element response = zsc.createElement(AdminConstants.SET_SERVER_OFFLINE_RESPONSE);
    CuratorManager curator = CuratorManager.getInstance();
    if (curator == null) {
      return response;
    }

    Element d = request.getElement(AdminConstants.E_SERVER);
    String method = d.getAttribute(AdminConstants.A_BY);
    String name = d.getText();

    if (name == null || name.equals(""))
      throw ServiceException.INVALID_REQUEST("must specify a value for a server", null);

    Server server = Provisioning.getInstance().get(Key.ServerBy.fromString(method), name);
    try {
      curator.unregisterService(server.getId());
    } catch (Exception e) {
      throw ServiceException.FAILURE("error while unregistering the server", e);
    }
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
  }
}
