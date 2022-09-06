// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.CalendarResourceBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyCalendarResourceRequest;
import java.util.List;
import java.util.Map;

/**
 * @author jhahm
 */
public class ModifyCalendarResource extends AdminDocumentHandler {

  private static final String[] TARGET_RESOURCE_PATH = new String[] {AdminConstants.E_ID};

  @Override
  protected String[] getProxiedResourcePath() {
    return TARGET_RESOURCE_PATH;
  }

  /**
   * must be careful and only allow modifies to calendar resources/attrs domain admin has access to
   */
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
    ModifyCalendarResourceRequest req = zsc.elementToJaxb(request);
    String id = req.getId();
    if (null == id) {
      throw ServiceException.INVALID_REQUEST(
          "missing required attribute: " + AdminConstants.E_ID, null);
    }
    Map<String, Object> attrs = req.getAttrsAsOldMultimap();

    CalendarResource resource = prov.get(CalendarResourceBy.id, id);
    defendAgainstCalResourceHarvesting(resource, CalendarResourceBy.id, id, zsc, attrs);

    String newServer = ModifyAccount.getStringAttrNewValue(Provisioning.A_zimbraMailHost, attrs);
    if (newServer != null) {
      defendAgainstServerNameHarvesting(
          Provisioning.getInstance().getServerByName(newServer),
          Key.ServerBy.name,
          newServer,
          zsc,
          Admin.R_listServer);
    }

    // pass in true to checkImmutable
    prov.modifyAttrs(resource, attrs, true);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "ModifyCalendarResource", "name", resource.getName()}, attrs));

    Element response = zsc.createElement(AdminConstants.MODIFY_CALENDAR_RESOURCE_RESPONSE);
    ToXML.encodeCalendarResource(response, resource, true);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY,
            Admin.R_modifyCalendarResource.getName(),
            "calendar resource"));
  }
}
