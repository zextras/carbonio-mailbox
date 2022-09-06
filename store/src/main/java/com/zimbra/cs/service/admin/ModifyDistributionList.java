// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyDistributionListRequest;
import java.util.List;
import java.util.Map;

public class ModifyDistributionList extends DistributionListDocumentHandler {

  /** must be careful and only allow access to domain if domain admin */
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
  protected Group getGroup(Element request) throws ServiceException {
    String id = request.getAttribute(AdminConstants.E_ID);
    return Provisioning.getInstance().getGroup(DistributionListBy.id, id);
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();
    ModifyDistributionListRequest req = zsc.elementToJaxb(request);
    String id = req.getId();
    Group group = getGroupFromContext(context);

    Map<String, Object> attrs = req.getAttrsAsOldMultimap();
    defendAgainstGroupHarvesting(group, DistributionListBy.id, id, zsc, attrs, attrs);

    // pass in true to checkImmutable
    prov.modifyAttrs(group, attrs, true);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "ModifyDistributionList", "name", group.getName()}, attrs));

    Element response = zsc.createElement(AdminConstants.MODIFY_DISTRIBUTION_LIST_RESPONSE);
    GetDistributionList.encodeDistributionList(response, group);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY,
            Admin.R_modifyDistributionList.getName(),
            "distribution list"));
  }
}
