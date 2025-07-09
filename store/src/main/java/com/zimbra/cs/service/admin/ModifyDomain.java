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
import com.zimbra.cs.account.Config;
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
import java.util.Set;
import java.util.HashSet;

/**
 * @author schemers
 */
public class ModifyDomain extends AdminDocumentHandler {

  public boolean domainAuthSufficient(Map<String, Object> context) {
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

    AdminAccessControl adminAccessControl = checkDomainRight(zsc, domain, attrs);

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

    if (!hasAdminRightAndCanModifyConfig(adminAccessControl, prov.getConfig())) {
      if (!Objects.isNull(gotPublicServiceHostname)
          && !(isPublicServiceHostnameCompliant(domain, gotPublicServiceHostname))) {
        throw ServiceException.FAILURE(
            "Public service hostname must be a valid FQDN and compatible with current domain "
                + "(or its aliases).");
      }
      final String[] gotVirtualHostNames = getVirtualHostnamesFromAttributes(attrs);
      if (!(Objects.isNull(gotVirtualHostNames))
          && !(Arrays.equals(gotVirtualHostNames, new String[] {""}))
          && !(areVirtualHostnamesCompliant(
              domain, Arrays.stream(gotVirtualHostNames).collect(Collectors.toList())))) {
        throw ServiceException.FAILURE(
            "Virtual hostnames must be valid FQDNs and compatible with current domain "
                + "(or its aliases).");
      }
    }
    // Note: Admin users can set any virtual hostname without restrictions

    // Check for duplicate virtual hostnames across all domains (for all users)
    final String[] gotVirtualHostNames = getVirtualHostnamesFromAttributes(attrs);
    if (!Objects.isNull(gotVirtualHostNames) && !Arrays.equals(gotVirtualHostNames, new String[] {""})) {
      checkForDuplicateVirtualHostnames(domain, gotVirtualHostNames, prov);
    }

    // pass in true to checkImmutable
    prov.modifyAttrs(domain, attrs, true);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "ModifyDomain", "name", domain.getName()}, attrs));

    Element response = zsc.createElement(AdminConstants.MODIFY_DOMAIN_RESPONSE);
    GetDomain.encodeDomain(response, domain);
    
    // Add warning about duplicate virtual hostnames if any (for all users)
    if (!Objects.isNull(gotVirtualHostNames) && !Arrays.equals(gotVirtualHostNames, new String[] {""})) {
      Set<String> conflictingDomains = getConflictingDomains(domain, gotVirtualHostNames, prov);
      if (!conflictingDomains.isEmpty()) {
        Element warning = response.addElement("warning");
        warning.addAttribute("type", "duplicate_virtual_hostname");
        warning.addAttribute("message", 
            "Virtual hostname modification for domain '" + domain.getName() + 
            "' conflicts with existing virtual hostnames in domains: " + 
            String.join(", ", conflictingDomains) + ". This may cause routing issues.");
        warning.addAttribute("conflicting_domains", String.join(",", conflictingDomains));
      }
    }
    
    return response;
  }

  /**
   * Checks if an authenticated user has right to modify global config.
   *
   * <p><Note> Global admin and delegated admin with the granted right Admin.R_modifyGlobalConfig
   * should be able to set an arbitrary value on zimbraPublicServiceHostname and(or)
   * zimbraVirtualHostname. </Note>
   *
   * @param adminAccessControl {@link com.zimbra.cs.service.admin.AdminAccessControl}
   * @param config the global config {@link com.zimbra.cs.account.Config}
   * @return true if an authenticated user has right to modify global config, false otherwise.
   * @throws ServiceException if something goes wrong during the checks.
   * @author Yuliya Aheeva
   * @since 23.6.0
   */
  protected boolean hasAdminRightAndCanModifyConfig(AdminAccessControl adminAccessControl, Config config)
      throws ServiceException {
    return adminAccessControl.isGlobalAdmin()
        || adminAccessControl.hasRight(config, Admin.R_modifyGlobalConfig);
  }

  /**
   * Checks that given publicServiceHostname is valid for a domain.
   *
   * @param domain domain being updated
   * @param publicServiceHostname value to check against domain
   */
  private boolean isPublicServiceHostnameCompliant(Domain domain, String publicServiceHostname) {
    return isFQDNCompliant(domain.getDomainName(), publicServiceHostname);
  }

  /**
   * Checks if a given FQDN is compliant for a domain. FQDN can be equal to domain or must be in
   * subdomain-fashion style (e.g.: domain test.com -> fqdn web.test.com)
   *
   * @param domainName name of the domain
   * @param fqdn fqdn to test against domain
   * @return id public service hostname compliant
   */
  private boolean isFQDNCompliant(String domainName, String fqdn) {
    return Objects.equals(domainName, fqdn) || fqdn.endsWith("." + domainName);
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

  /**
   * Checks for duplicate virtual hostnames across all domains and logs a warning if found.
   *
   * @param currentDomain the domain being modified
   * @param newVirtualHostnames the new virtual hostnames to check
   * @param prov the provisioning instance
   * @throws ServiceException if something goes wrong during the check
   */
  private void checkForDuplicateVirtualHostnames(Domain currentDomain, String[] newVirtualHostnames, Provisioning prov) throws ServiceException {
    Set<String> conflictingDomains = getConflictingDomains(currentDomain, newVirtualHostnames, prov);
    
    if (!conflictingDomains.isEmpty()) {
      ZimbraLog.security.warn(
          "Virtual hostname modification for domain '%s' conflicts with existing virtual hostnames in domains: %s. " +
          "This may cause routing issues.", 
          currentDomain.getName(), 
          String.join(", ", conflictingDomains));
    }
  }

  /**
   * Gets the set of domains that have conflicting virtual hostnames with the new virtual hostnames.
   *
   * @param currentDomain the domain being modified
   * @param newVirtualHostnames the new virtual hostnames to check
   * @param prov the provisioning instance
   * @return Set of domain names that have conflicting virtual hostnames
   * @throws ServiceException if something goes wrong during the check
   */
  private Set<String> getConflictingDomains(Domain currentDomain, String[] newVirtualHostnames, Provisioning prov) throws ServiceException {
    Set<String> newVHostnames = new HashSet<>(Arrays.asList(newVirtualHostnames));
    Set<String> conflictingDomains = new HashSet<>();
    
    List<Domain> allDomains = prov.getAllDomains();
    for (Domain otherDomain : allDomains) {
      // Skip the current domain being modified
      if (otherDomain.getId().equals(currentDomain.getId())) {
        continue;
      }
      
      String[] otherVHostnames = otherDomain.getVirtualHostname();
      if (otherVHostnames != null) {
        for (String otherVHostname : otherVHostnames) {
          if (otherVHostname != null && !otherVHostname.isEmpty() && newVHostnames.contains(otherVHostname)) {
            conflictingDomains.add(otherDomain.getName());
          }
        }
      }
    }
    
    return conflictingDomains;
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
