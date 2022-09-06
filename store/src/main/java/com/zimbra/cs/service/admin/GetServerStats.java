// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.stats.ZimbraPerf;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GetServerStats extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Server localServer = Provisioning.getInstance().getLocalServer();
    checkRight(zsc, context, localServer, Admin.R_getServerStats);

    // Assemble list of requested stat names.
    List<Element> eStats = request.listElements(AdminConstants.E_STAT);
    Set<String> requestedNames = new HashSet<String>();
    for (int i = 0; i < eStats.size(); i++) {
      requestedNames.add(eStats.get(i).getAttribute(AdminConstants.A_NAME));
    }

    // Get latest values.
    Map<String, Object> allStats = ZimbraPerf.getStats();
    Map<String, Object> returnedStats = new TreeMap<String, Object>();
    boolean returnAllStats = (requestedNames.size() == 0);

    for (String name : allStats.keySet()) {
      if (returnAllStats || requestedNames.contains(name)) {
        returnedStats.put(name, allStats.get(name));
        requestedNames.remove(name);
      }
    }

    if (requestedNames.size() != 0) {
      StringBuilder buf = new StringBuilder("Invalid stat name");
      if (requestedNames.size() > 1) {
        buf.append("s");
      }
      buf.append(": ").append(StringUtil.join(", ", requestedNames));
      throw ServiceException.FAILURE(buf.toString(), null);
    }

    // Send response.
    Element response = zsc.createElement(AdminConstants.GET_SERVER_STATS_RESPONSE);
    for (String name : returnedStats.keySet()) {
      String stringVal = toString(returnedStats.get(name));
      Element eStat =
          response
              .addElement(AdminConstants.E_STAT)
              .addAttribute(AdminConstants.A_NAME, name)
              .setText(stringVal);

      String description = ZimbraPerf.getDescription(name);
      if (description != null) {
        eStat.addAttribute(AdminConstants.A_DESCRIPTION, description);
      }
    }

    return response;
  }

  private static String toString(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Double || value instanceof Float) {
      return String.format("%.2f", value);
    } else {
      return value.toString();
    }
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_getServerStats);
  }
}
