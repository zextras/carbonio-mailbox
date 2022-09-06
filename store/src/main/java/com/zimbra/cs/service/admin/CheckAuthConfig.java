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
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class CheckAuthConfig extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    String name = request.getAttribute(AdminConstants.E_NAME).toLowerCase();
    String password = request.getAttribute(AdminConstants.E_PASSWORD);
    Map attrs = AdminService.getAttrs(request, true);

    Element response = zsc.createElement(AdminConstants.CHECK_AUTH_CONFIG_RESPONSE);
    Provisioning.Result r = Provisioning.getInstance().checkAuthConfig(attrs, name, password);

    response.addElement(AdminConstants.E_CODE).addText(r.getCode());
    String message = r.getMessage();
    if (message != null) response.addElement(AdminConstants.E_MESSAGE).addText(message);
    response.addElement(AdminConstants.E_BINDDN).addText(r.getComputedDn());

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
  }
}
