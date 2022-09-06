// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

/**
 * @since Jun 17, 2004
 * @author schemers
 */
public final class ModifyServer extends AdminDocumentHandler {

  private static final String[] TARGET_SERVER_PATH = new String[] {AdminConstants.E_ID};

  @Override
  protected String[] getProxiedServerPath() {
    return TARGET_SERVER_PATH;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    String id = request.getAttribute(AdminConstants.E_ID);
    Map<String, Object> attrs = AdminService.getAttrs(request);

    Server server = prov.get(Key.ServerBy.id, id);
    if (server == null) {
      throw AccountServiceException.NO_SUCH_SERVER(id);
    }
    checkRight(zsc, context, server, attrs);

    // pass in true to checkImmutable
    prov.modifyAttrs(server, attrs, true);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "ModifyServer", "name", server.getName()}, attrs));

    Element response = zsc.createElement(AdminConstants.MODIFY_SERVER_RESPONSE);
    GetServer.encodeServer(response, server);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY, Admin.R_modifyServer.getName(), "server"));
  }
}
