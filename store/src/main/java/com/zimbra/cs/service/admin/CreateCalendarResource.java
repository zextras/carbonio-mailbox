// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CreateCalendarResourceRequest;
import java.util.List;
import java.util.Map;

/**
 * @author jhahm
 */
public class CreateCalendarResource extends AdminDocumentHandler {

  /** must be careful and only create calendar recources for the domain admin! */
  @Override
  public boolean domainAuthSufficient(Map context) {
    return true;
  }

  /**
   * @return true - which means accept responsibility for measures to prevent account harvesting by
   *     delegate admins
   */
  @Override
  public boolean defendsAgainstDelegateAdminAccountHarvesting() {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();
    CreateCalendarResourceRequest req = zsc.elementToJaxb(request);

    String name = req.getName().toLowerCase();
    checkDomainRightByEmail(zsc, name, Admin.R_createCalendarResource);

    Map<String, Object> attrs = req.getAttrsAsOldMultimap(true);
    checkSetAttrsOnCreate(zsc, TargetType.calresource, name, attrs);

    String password = req.getPassword();
    CalendarResource resource = prov.createCalendarResource(name, password, attrs);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(new String[] {"cmd", "CreateCalendarResource", "name", name}, attrs));

    Element response = zsc.createElement(AdminConstants.CREATE_CALENDAR_RESOURCE_RESPONSE);
    ToXML.encodeCalendarResource(response, resource, true);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_createCalendarResource);
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY,
            Admin.R_modifyCalendarResource.getName(),
            "calendar resource"));
  }
}
