// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    if (domain == null) {
      throw AccountServiceException.NO_SUCH_DOMAIN(id);
    }

    if (domain.isShutdown()) {
      throw ServiceException.PERM_DENIED(
          "can not access domain, domain is in " + domain.getDomainStatusAsString() + " status");
    }

    checkDomainRight(zsc, domain, attrs);

    // check to see if domain default cos is being changed, need right on new cos
    checkCos(zsc, attrs, Provisioning.A_zimbraDomainDefaultCOSId);
    checkCos(zsc, attrs, Provisioning.A_zimbraDomainDefaultExternalUserCOSId);

    final String gotPublicServiceHostname =
        (String) attrs.get(Provisioning.A_zimbraPublicServiceHostname);
    final String receivedDomainName = (String) attrs.get(Provisioning.A_zimbraDomainName);
    if (!Objects.isNull(receivedDomainName)) {
      throw ServiceException.INVALID_REQUEST(
          Provisioning.A_zimbraDomainName + " cannot be changed.", null);
    }
    if (!Objects.isNull(gotPublicServiceHostname)
        && !(isPublicServiceHostnameCompliant(domain, gotPublicServiceHostname))) {
      throw ServiceException.FAILURE(
          "Public service hostname must be a valid FQDN and compatible with current domain (or its"
              + " aliases).");
    }
    final String[] gotVirtualHostNames = getVirtualHostnamesFromAttributes(attrs);
    if (!(Objects.isNull(gotVirtualHostNames))
        && !(areVirtualHostnamesCompliant(
            domain, Arrays.stream(gotVirtualHostNames).collect(Collectors.toList())))) {
      throw ServiceException.FAILURE(
          "Virtual hostnames must be valid FQDNs and compatible with current domain (or its"
              + " aliases).");
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
  private boolean isPublicServiceHostnameCompliant(Domain domain, String publicServiceHostname) {
    return isFQDNCompliant(domain.getDomainName(), publicServiceHostname);
  }

  /**
   * Checks if a given FQDN is compliant for a domain.
   *
   * @param domainName name of the domain
   * @param fqdn fqdn to test against domain
   * @return id public service hostname compliant
   */
  private boolean isFQDNCompliant(String domainName, String fqdn) {
    return fqdn.endsWith(domainName);
  }

  /**
   * Checks if given virtual hostnames are compliant for domain.
   *
   * @param domain domain to check against
   * @param virtualHostnames given virtualHostnames
   * @return if compliant
   */
  private boolean areVirtualHostnamesCompliant(Domain domain, List<String> virtualHostnames) {
    final String domainName = domain.getDomainName();
    return virtualHostnames.stream()
        .allMatch(virtualHostname -> isFQDNCompliant(domainName, virtualHostname));
  }

  /**
   * Returns virtual hostnames from request attributes. Virtual hostnames can be one or many
   * depending on received request.
   *
   * @param attrs attributes as from {@link com.zimbra.soap.admin.message.ModifyDomainRequest}
   * @return array of virtualHostnames provided in attrs
   */
  private String[] getVirtualHostnamesFromAttributes(Map<String, Object> attrs) {
    final Object vHostNames = attrs.get(Provisioning.A_zimbraVirtualHostname);
    if (vHostNames instanceof String) {
      return new String[] {(String) vHostNames};
    }
    if (vHostNames instanceof String[]) {
      return (String[]) vHostNames;
    }
    return null;
  }

  private void checkCos(
      ZimbraSoapContext zsc, Map<String, Object> attrs, String defaultCOSIdAttrName)
      throws ServiceException {
    String newDomainCosId = ModifyAccount.getStringAttrNewValue(defaultCOSIdAttrName, attrs);
    if (newDomainCosId == null) {
      return; // not changing it
    }

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
