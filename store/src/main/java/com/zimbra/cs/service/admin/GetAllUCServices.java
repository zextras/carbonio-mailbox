// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
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
public class GetAllUCServices extends AdminDocumentHandler {

  public static final String BY_NAME = "name";
  public static final String BY_ID = "id";

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    List<UCService> ucServices = prov.getAllUCServices();

    AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);

    Element response = zsc.createElement(AdminConstants.GET_ALL_UC_SERVICES_RESPONSE);
    for (UCService ucSservice : ucServices) {
      if (aac.hasRightsToList(ucSservice, Admin.R_listUCService, null)) {
        GetUCService.encodeUCService(
            response, ucSservice, null, aac.getAttrRightChecker(ucSservice));
      }
    }

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_listUCService);
    relatedRights.add(Admin.R_getUCService);

    notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
  }
}
