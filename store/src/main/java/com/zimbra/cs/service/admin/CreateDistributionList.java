// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CreateDistributionListRequest;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class CreateDistributionList extends AdminDocumentHandler {

  /**
   * must be careful and only allow access to domain if domain admin
   */
  @Override
  public boolean domainAuthSufficient(Map<String, Object> context) {
    return true;
  }

  /**
   * @return true - which means accept responsibility for measures to prevent account harvesting by
   * delegate admins
   */
  @Override
  public boolean defendsAgainstDelegateAdminAccountHarvesting() {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context)
      throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();
    CreateDistributionListRequest req = zsc.elementToJaxb(request);

    if (StringUtils.isEmpty(req.getName())) {
      throw ServiceException.INVALID_REQUEST(String.format("missing %s", AdminConstants.E_NAME),
          null);
    }

    String name = req.getName().toLowerCase();

    Map<String, Object> attrs = req.getAttrsAsOldMultimap(true);

    boolean dynamic = Boolean.TRUE.equals(req.getDynamic());

    if (dynamic) {
      // see issue CO-526
      if (zsc.getAuthToken().isDelegatedAdmin()) {
        throw ServiceException.INVALID_REQUEST(
            "Delegated Admins are not allowed to create Dynamic Distribution Lists", null);
      }
      checkDomainRightByEmail(zsc, name, AdminRights.R_createGroup);
      checkSetAttrsOnCreate(zsc, TargetType.group, name, attrs);
    } else {
      checkDomainRightByEmail(zsc, name, AdminRights.R_createDistributionList);
      checkSetAttrsOnCreate(zsc, TargetType.dl, name, attrs);
    }

    Group group = prov.createGroup(name, attrs, dynamic);

    ZimbraLog.security.info(ZimbraLog.encodeAttrs(
        new String[]{"cmd", "CreateDistributionList", "name", name}, attrs));

    Element response = getResponseElement(zsc);

    GetDistributionList.encodeDistributionList(response, group);

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(AdminRights.R_createDistributionList);
    relatedRights.add(AdminRights.R_createGroup);
    notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY,
        AdminRights.R_modifyDistributionList.getName(), "distribution list"));
    notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY,
        AdminRights.R_modifyGroup.getName(), "group"));
  }

  @Override
  protected Element getResponseElement(ZimbraSoapContext zsc) {
    return zsc.createElement(AdminConstants.CREATE_DISTRIBUTION_LIST_RESPONSE);
  }
}
