// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author schemers
 */
public class GetConfig extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    Element a = request.getElement(AdminConstants.E_A);
    String name = a.getAttribute(AdminConstants.A_N);

    Config config = prov.getConfig();

    AdminAccessControl aac = checkRight(zsc, context, config, AdminRight.PR_ALWAYS_ALLOW);

    Element response = zsc.createElement(AdminConstants.GET_CONFIG_RESPONSE);

    Set<String> reqAttrs = new HashSet<String>();
    reqAttrs.add(name);
    GetAllConfig.encodeConfig(response, config, reqAttrs, aac.getAttrRightChecker(config));

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add("Need get attr right for the specified attribute.");
  }
}
