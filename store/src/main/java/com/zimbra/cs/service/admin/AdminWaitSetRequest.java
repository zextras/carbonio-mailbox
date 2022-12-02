// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.service.mail.WaitSetRequest;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AdminWaitSetResponse;

public class AdminWaitSetRequest extends AdminDocumentHandler {
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        com.zimbra.soap.admin.message.AdminWaitSetRequest req = zsc.elementToJaxb(request);
        AdminWaitSetResponse resp = new AdminWaitSetResponse();
        WaitSetRequest.staticHandle(req, context, resp, true);
        return zsc.jaxbToElement(resp);
    }

    @Override
    public void preProxy(Element request, Map<String, Object> context) throws ServiceException {
        setProxyTimeout(WaitSetRequest.getTimeoutMillis(request, true) + 10 * Constants.MILLIS_PER_SECOND);
        super.preProxy(request, context);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add("If the waitset is on all accounts, " + AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
        notes.add("Otherwise, must be the owner of the specified waitset");
    }
}
