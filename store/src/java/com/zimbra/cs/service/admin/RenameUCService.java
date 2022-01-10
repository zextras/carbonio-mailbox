// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.UCServiceBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.UCService;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

public class RenameUCService extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        String id = request.getElement(AdminConstants.E_ID).getText();
        String newName = request.getElement(AdminConstants.E_NEW_NAME).getText();

        UCService ucService = prov.get(UCServiceBy.id, id);
        if (ucService == null) {
            throw AccountServiceException.NO_SUCH_UC_SERVICE(id);
        }

        // check if the admin can rename the uc service
        checkRight(lc, context, ucService, Admin.R_renameUCService);

        String oldName = ucService.getName();

        prov.renameUCService(id, newName);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] { "cmd", "RenameUCService", "name", oldName, "newName", newName }));

        // get again with new name...

        ucService = prov.get(Key.UCServiceBy.id, id);
        if (ucService == null) {
            throw ServiceException.FAILURE("unabled to get renamed uc service: " + id, null);
        }
        Element response = lc.createElement(AdminConstants.RENAME_UC_SERVICE_RESPONSE);
        GetUCService.encodeUCService(response, ucService, null, null);
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_renameUCService);
    }

}
