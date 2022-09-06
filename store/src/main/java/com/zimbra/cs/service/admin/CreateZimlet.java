// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class CreateZimlet extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext lc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    String name = request.getAttribute(AdminConstants.E_NAME).toLowerCase();
    Map<String, Object> attrs = AdminService.getAttrs(request, true);

    checkRight(lc, context, null, Admin.R_createZimlet);
    checkSetAttrsOnCreate(lc, TargetType.zimlet, name, attrs);

    Zimlet zimlet = prov.createZimlet(name, attrs);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(new String[] {"cmd", "CreateZimlet", "name", name}, attrs));

    Element response = lc.createElement(AdminConstants.CREATE_ZIMLET_RESPONSE);
    GetZimlet.encodeZimlet(response, zimlet);

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_createZimlet);
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY, Admin.R_modifyZimlet.getName(), "zimlet"));
  }
}
