// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

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
import java.util.List;
import java.util.Map;

/**
 * @author pshao
 */
public class DeleteUCService extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    String id = request.getElement(AdminConstants.E_ID).getText();

    UCService ucService = prov.get(UCServiceBy.id, id);
    if (ucService == null) {
      throw AccountServiceException.NO_SUCH_UC_SERVICE(id);
    }

    checkRight(zsc, context, ucService, Admin.R_deleteUCService);

    prov.deleteUCService(ucService.getId());

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {
              "cmd", "DeleteUCService", "name", ucService.getName(), "id", ucService.getId()
            }));

    Element response = zsc.createElement(AdminConstants.DELETE_UC_SERVICE_RESPONSE);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_deleteUCService);
  }
}
