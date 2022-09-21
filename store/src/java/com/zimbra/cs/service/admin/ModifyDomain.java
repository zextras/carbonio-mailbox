// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author schemers
 */
public class ModifyDomain extends AdminDocumentHandler {

  public boolean domainAuthSufficient(Map context) {
    return true;
  }

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    String id = request.getAttribute(AdminConstants.E_ID);
    Map<String, Object> attrs = AdminService.getAttrs(request);

    Domain domain = prov.get(Key.DomainBy.id, id);
    if (domain == null) throw AccountServiceException.NO_SUCH_DOMAIN(id);

    if (domain.isShutdown())
      throw ServiceException.PERM_DENIED(
          "can not access domain, domain is in " + domain.getDomainStatusAsString() + " status");

    checkDomainRight(zsc, domain, attrs);

    // check to see if domain default cos is being changed, need right on new cos
    checkCos(zsc, attrs, Provisioning.A_zimbraDomainDefaultCOSId);
    checkCos(zsc, attrs, Provisioning.A_zimbraDomainDefaultExternalUserCOSId);

    final String gotPubServiceHostname =
        (String) attrs.get(Provisioning.A_zimbraPublicServiceHostname);
    final String gotDomainName = (String) attrs.get(Provisioning.A_zimbraDomainName);
    if (!Objects.isNull(gotPubServiceHostname)) {
      checkPublicServiceHostname(domain, gotPubServiceHostname);
    }
    if (!Objects.isNull(gotDomainName)) {
      attrs.putAll(getAttributesToUpdateFromNewDomain(domain, gotDomainName));
    }

    // pass in true to checkImmutable
    prov.modifyAttrs(domain, attrs, true);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "ModifyDomain", "name", domain.getName()}, attrs));

    Element response = zsc.createElement(AdminConstants.MODIFY_DOMAIN_RESPONSE);
    GetDomain.encodeDomain(response, domain);
    return response;
  }

  /**
   * Checks that given publicServiceHostname is valid for a domain.
   *
   * @param domain domain being updated
   * @param publicServiceHostname value to check against domain
   * @throws ServiceException exception if public service hostname not valid
   */
  private void checkPublicServiceHostname(Domain domain, String publicServiceHostname)
      throws ServiceException {
    final String domainName = domain.getDomainName();
    if (!publicServiceHostname.endsWith(domainName)) {
      throw ServiceException.FAILURE(
          "Public service hostname "
              + publicServiceHostname
              + " must be subdomain of "
              + domainName
              + ".");
    }
  }

  /**
   * Returns a {@link Map} of attributes to be updated based on new given domain name. Returned
   * attributes include only {@link ZAttrProvisioning#A_zimbraPublicServiceHostname} and {@link
   * ZAttrProvisioning#A_zimbraVirtualHostname}.
   *
   * @param domain domain that is being updated
   * @param newDomainName new domain name
   * @return a map of attributes to update from new domain name
   */
  private Map<String, Object> getAttributesToUpdateFromNewDomain(
      Domain domain, String newDomainName) {
    final HashMap<String, Object> attrsToUpdate = new HashMap<>();
    final String oldPubServiceHostname = domain.getPublicServiceHostname();
    final String oldDomainName = domain.getDomainName();
    final String newPubServiceHostname =
        oldPubServiceHostname.substring(0, oldPubServiceHostname.lastIndexOf(oldDomainName))
            + newDomainName;
    attrsToUpdate.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
    final List<String> newVirtualHostnames = new ArrayList<>();
    for (String virtualHostname : domain.getVirtualHostname()) {
      newVirtualHostnames.add(
          virtualHostname.substring(0, virtualHostname.lastIndexOf(oldDomainName)) + newDomainName);
    }
    if (!newVirtualHostnames.isEmpty()) {
      attrsToUpdate.put(ZAttrProvisioning.A_zimbraVirtualHostname, newVirtualHostnames);
    }
    return attrsToUpdate;
  }

  private void checkCos(
      ZimbraSoapContext zsc, Map<String, Object> attrs, String defaultCOSIdAttrName)
      throws ServiceException {
    String newDomainCosId = ModifyAccount.getStringAttrNewValue(defaultCOSIdAttrName, attrs);
    if (newDomainCosId == null) return; // not changing it

    Provisioning prov = Provisioning.getInstance();
    if (newDomainCosId.equals("")) {
      // they are unsetting it, no problem
      return;
    }

    Cos cos = prov.get(Key.CosBy.id, newDomainCosId);
    if (cos == null) {
      throw AccountServiceException.NO_SUCH_COS(newDomainCosId);
    }

    // call checkRight instead of checkCosRight, because:
    // 1. no domain based access manager backward compatibility issue
    // 2. we only want to check right if we are using pure ACL based access manager.
    checkRight(zsc, cos, Admin.R_assignCos);
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY, Admin.R_modifyDomain.getName(), "domain"));

    notes.add(
        "Notes on "
            + Provisioning.A_zimbraDomainDefaultCOSId
            + ": "
            + "If setting "
            + Provisioning.A_zimbraDomainDefaultCOSId
            + ", needs the "
            + Admin.R_assignCos.getName()
            + " right on the cos.");
  }
}
