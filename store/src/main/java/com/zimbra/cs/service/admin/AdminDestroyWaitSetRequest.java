// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.service.mail.DestroyWaitSet;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class AdminDestroyWaitSetRequest extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(AdminConstants.ADMIN_DESTROY_WAIT_SET_RESPONSE);
    return DestroyWaitSet.staticHandle(request, context, response);
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        "If the waitset is on all accounts, " + AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
    notes.add("Otherwise, must be the owner of the specified waitset");
  }
}
