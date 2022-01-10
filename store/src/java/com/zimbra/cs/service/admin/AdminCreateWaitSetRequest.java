// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.service.mail.CreateWaitSet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AdminCreateWaitSetResponse;

public class AdminCreateWaitSetRequest extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        com.zimbra.soap.admin.message.AdminCreateWaitSetRequest req = zsc.elementToJaxb(request);
        AdminCreateWaitSetResponse resp = new AdminCreateWaitSetResponse();
        CreateWaitSet.staticHandle(this, req, context, resp);
        return zsc.jaxbToElement(resp);  /* MUST use zsc variant NOT JaxbUtil */
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add("If allAccounts is specified, " + AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
        notes.add("Otherwise, for each requested account, " + AdminRightCheckPoint.Notes.ADMIN_LOGIN_AS);
    }

}
