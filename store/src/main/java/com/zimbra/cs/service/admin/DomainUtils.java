package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;

import java.util.*;

public class DomainUtils {
    private DomainUtils() {
        // Utility class
    }

    /**
     * Returns virtual hostnames from request attributes. Virtual hostnames can be one or many
     * depending on received request.
     *
     * @param attrs attributes as from {@link com.zimbra.soap.admin.message.ModifyDomainRequest}
     * @return array of virtualHostnames provided in attrs
     */
    public static String[] getVirtualHostnamesFromAttributes(Map<String, Object> attrs) {
      final Object vHostNames = attrs.get(Provisioning.A_zimbraVirtualHostname);
      if (vHostNames instanceof String) {
        return new String[] {(String) vHostNames};
      }
      if (vHostNames instanceof String[]) {
        return (String[]) vHostNames;
      }
      return null;
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
    public static Set<String> getDomainsWithConflictingVHosts(Domain currentDomain, String[] newVirtualHostnames, Provisioning prov) throws ServiceException {
      Set<String> newVHostnames = new HashSet<>(Arrays.asList(newVirtualHostnames));
      Set<String> conflictingDomains = new HashSet<>();

      List<Domain> allDomains = prov.getAllDomains();
      for (Domain otherDomain : allDomains) {
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
}
