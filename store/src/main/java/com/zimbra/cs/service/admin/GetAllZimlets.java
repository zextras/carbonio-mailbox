// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class GetAllZimlets extends AdminDocumentHandler {

  public boolean domainAuthSufficient(Map<String, Object> context) {
    return true;
  }

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    String exclude = request.getAttribute(AdminConstants.A_EXCLUDE, AdminConstants.A_NONE);
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    List<Zimlet> zimlets = prov.listAllZimlets();

    AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);

    Element response = zsc.createElement(AdminConstants.GET_ALL_ZIMLETS_RESPONSE);
    if (AdminConstants.A_EXTENSION.equalsIgnoreCase(exclude)) {
      for (Zimlet zimlet : zimlets) {
        if (!zimlet.isExtension()) {
          if (aac.hasRightsToList(zimlet, Admin.R_listZimlet, null))
            GetZimlet.encodeZimlet(response, zimlet, null, aac.getAttrRightChecker(zimlet));
        }
      }
    } else if (AdminConstants.A_MAIL.equalsIgnoreCase(exclude)) {
      for (Zimlet zimlet : zimlets) {
        if (zimlet.isExtension()) {
          if (aac.hasRightsToList(zimlet, Admin.R_listZimlet, null))
            GetZimlet.encodeZimlet(response, zimlet, null, aac.getAttrRightChecker(zimlet));
        }
      }
    } else {
      for (Zimlet zimlet : zimlets) {
        if (aac.hasRightsToList(zimlet, Admin.R_listZimlet, null))
          GetZimlet.encodeZimlet(response, zimlet, null, aac.getAttrRightChecker(zimlet));
      }
    }
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_listZimlet);
    relatedRights.add(Admin.R_getZimlet);

    notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
  }
}
