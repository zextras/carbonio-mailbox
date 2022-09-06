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
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class GetRight extends RightDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    boolean expandAllAtrts = request.getAttributeBool(AdminConstants.A_EXPAND_ALL_ATTRS, false);
    Element eRight = request.getElement(AdminConstants.E_RIGHT);
    String value = eRight.getText();

    Right right = RightManager.getInstance().getRight(value);

    Element response = zsc.createElement(AdminConstants.GET_RIGHT_RESPONSE);
    RightCommand.rightToXML(response, right, expandAllAtrts, account.getLocale());

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
  }
}
