// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.RevokeRightRequest;
import java.util.List;
import java.util.Map;

public class RevokeRight extends RightDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    RevokeRightRequest rrReq = zsc.elementToJaxb(request);
    RightModifier rightModifier = GrantRight.getRightModifier(rrReq.getRight());

    // right checking is done in RightCommand

    RightCommand.revokeRight(
        Provisioning.getInstance(),
        getAuthenticatedAccount(zsc),
        rrReq.getTarget(),
        rrReq.getGrantee(),
        rrReq.getRight().getValue(),
        rightModifier);

    Element response = zsc.createElement(AdminConstants.REVOKE_RIGHT_RESPONSE);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        "Grantor must have the same or more rights on the same target or on a larger target set.");
  }
}
