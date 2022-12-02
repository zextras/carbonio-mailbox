// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.ldap.Check;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CheckHostnameResolveRequest;
import com.zimbra.soap.admin.message.CheckHostnameResolveResponse;

/**
 * @author schemers
 */
public class CheckHostnameResolve extends AdminDocumentHandler {

    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        CheckHostnameResolveRequest req = zsc.elementToJaxb(request);
        String host = req.getHostname().toLowerCase();

        Provisioning.Result r = Check.checkHostnameResolve(host);

        return zsc.jaxbToElement(CheckHostnameResolveResponse.fromCodeMessage(r.getCode(), r.getMessage()));
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
    }
}
