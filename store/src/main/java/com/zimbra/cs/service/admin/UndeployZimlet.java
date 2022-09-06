// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.zimlet.ZimletUtil;
import com.zimbra.cs.zimlet.ZimletUtil.ZimletSoapUtil;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class UndeployZimlet extends AdminDocumentHandler {

  private static class UndeployThread implements Runnable {
    final Server server;
    String name;
    ZAuthToken auth;

    public UndeployThread(Server s, String na, ZAuthToken au) {
      server = s;
      name = na;
      auth = au;
    }

    @Override
    public void run() {
      try {
        ZimletSoapUtil soapUtil = new ZimletSoapUtil(auth);
        soapUtil.undeployZimletRemotely(server, name);
      } catch (Exception e) {
        ZimbraLog.zimlet.info("undeploy", e);
      }
    }
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    String name = request.getAttribute(AdminConstants.A_NAME);
    String action = request.getAttribute(AdminConstants.A_ACTION, null);
    ZAuthToken auth = null;
    if (action == null) {
      auth = zsc.getRawAuthToken();
    }

    Element response = zsc.createElement(AdminConstants.UNDEPLOY_ZIMLET_RESPONSE);
    // undeploy on local server and LDAP anyway
    ZimletUtil.undeployZimletLocally(name);
    if (AdminConstants.A_DEPLOYALL.equals(action)) {
      // undeploy on remote servers
      for (Server server : Provisioning.getInstance().getAllServers()) {
        if (!server.isLocalServer()) {
          checkRight(zsc, context, server, Admin.R_deployZimlet);
          new Thread(new UndeployThread(server, name, auth)).start();
        }
      }
    }

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_deployZimlet);
    notes.add("Need the " + Admin.R_deployZimlet.getName() + " right on all servers.");
  }
}
