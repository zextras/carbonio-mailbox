// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class GetAllRights extends RightDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    String targetType = request.getAttribute(AdminConstants.A_TARGET_TYPE, null);
    boolean expandAllAtrts = request.getAttributeBool(AdminConstants.A_EXPAND_ALL_ATTRS, false);
    String rightClass = request.getAttribute(AdminConstants.A_RIGHT_CLASS, null);

    List<Right> rights = RightCommand.getAllRights(targetType, rightClass);

    Element response = zsc.createElement(AdminConstants.GET_ALL_RIGHTS_RESPONSE);
    for (Right right : rights)
      RightCommand.rightToXML(response, right, expandAllAtrts, account.getLocale());

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
  }
}
