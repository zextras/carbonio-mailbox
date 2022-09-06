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
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.RemoveDistributionListAliasRequest;
import com.zimbra.soap.admin.message.RemoveDistributionListAliasResponse;
import java.util.List;
import java.util.Map;

public class RemoveDistributionListAlias extends DistributionListDocumentHandler {

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
    String id = request.getAttribute(AdminConstants.E_ID, null);
    if (id != null) {
      return Provisioning.getInstance().getGroup(DistributionListBy.id, id);
    } else {
      return null;
    }
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();
    RemoveDistributionListAliasRequest req = zsc.elementToJaxb(request);

    String id = req.getId();
    String alias = req.getAlias();
    Group group = getGroupFromContext(context);

    String dlName = "";
    try {
      defendAgainstGroupHarvesting(
          group,
          DistributionListBy.id,
          id,
          zsc,
          Admin.R_removeGroupAlias,
          Admin.R_removeDistributionListAlias);
    } catch (AccountServiceException ase) {
      // still may want to remove the alias, even if it doesn't point at anything
      // note: if we got a permission denied instead of AccountServiceException,
      //       means we don't have the rights so shouldn't get any further
    }

    if (group != null) {
      dlName = group.getName();
    }

    // if the admin can remove an alias in the domain
    checkDomainRightByEmail(zsc, alias, Admin.R_deleteAlias);

    // even if dl is null, we still invoke removeAlias and throw an exception afterwards.
    // this is so dangling aliases can be cleaned up as much as possible
    prov.removeGroupAlias(group, alias);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "RemoveDistributionListAlias", "name", dlName, "alias", alias}));

    if (group == null) {
      throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(id);
    }

    return zsc.jaxbToElement(new RemoveDistributionListAliasResponse());
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_removeDistributionListAlias);
    relatedRights.add(Admin.R_removeGroupAlias);
    relatedRights.add(Admin.R_deleteAlias);
  }
}
