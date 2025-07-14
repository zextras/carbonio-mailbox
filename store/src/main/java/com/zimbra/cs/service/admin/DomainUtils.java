package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.common.soap.Element;

import java.util.*;

public class DomainUtils {
    private DomainUtils() {
        // Utility class
    }

    private static final String DUPLICATE_VIRTUAL_HOSTNAME_WARNING_TEMPLATE = 
        "Virtual hostname conflicts detected. The following virtual hostnames are already in use by other domains: %s. This may cause routing issues.";

    private static final String CONSOLE_WARNING_PREFIX = "WARNING: ";

    private static final String DUPLICATE_VIRTUAL_HOSTNAME_WARNING_TYPE = "duplicate_virtual_hostname";

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
     * @return Map of conflicting domain names to their conflicting virtual hostnames
     * @throws ServiceException if something goes wrong during the check
     */
    public static Map<String, Set<String>> getDomainsWithConflictingVHosts(Domain currentDomain, String[] newVirtualHostnames, Provisioning prov) throws ServiceException {
        if (newVirtualHostnames == null || newVirtualHostnames.length == 0) {
            return new HashMap<>();
        }
        
        Set<String> newVHostnames = new HashSet<>(Arrays.asList(newVirtualHostnames));
        Map<String, Set<String>> conflictingDomains = new HashMap<>();

        List<Domain> allDomains = prov.getAllDomains();
        for (Domain otherDomain : allDomains) {
            if (otherDomain.getId().equals(currentDomain.getId())) {
                continue;
            }

            String[] otherVHostnames = otherDomain.getVirtualHostname();
            if (otherVHostnames != null) {
                Set<String> conflicts = new HashSet<>();
                for (String otherVHostname : otherVHostnames) {
                    if (otherVHostname != null && !otherVHostname.isEmpty() && newVHostnames.contains(otherVHostname)) {
                        conflicts.add(otherVHostname);
                    }
                }
                if (!conflicts.isEmpty()) {
                    conflictingDomains.put(otherDomain.getName(), conflicts);
                }
            }
        }

        return conflictingDomains;
    }

    /**
     * Creates a warning element for duplicate virtual hostnames.
     *
     * @param response the response element to add the warning to
     * @param conflictingDomains map of domains to their conflicting virtual hostnames
     */
    public static void addDuplicateVirtualHostnameWarning(Element response, Map<String, Set<String>> conflictingDomains) {
        if (response == null || conflictingDomains == null || conflictingDomains.isEmpty()) {
            return;
        }
        
        Set<String> allConflictingVHosts = new HashSet<>();
        for (Set<String> vHosts : conflictingDomains.values()) {
            allConflictingVHosts.addAll(vHosts);
        }
        
        Element warning = response.addElement("warning");
        warning.addAttribute("type", DUPLICATE_VIRTUAL_HOSTNAME_WARNING_TYPE);
        warning.addAttribute("message", String.format(DUPLICATE_VIRTUAL_HOSTNAME_WARNING_TEMPLATE, String.join(", ", conflictingDomains.keySet())));
    }

    /**
     * Creates a console warning message for duplicate virtual hostnames.
     *
     * @param domain the domain being modified
     * @param conflictingDomains map of domains to their conflicting virtual hostnames
     * @return the warning message string
     */
    public static String getDuplicateVirtualHostnameWarningMessage(Domain domain, Map<String, Set<String>> conflictingDomains) {
        if (domain == null || conflictingDomains == null || conflictingDomains.isEmpty()) {
            return "";
        }
        
        Set<String> allConflictingVHosts = new HashSet<>();
        for (Set<String> vHosts : conflictingDomains.values()) {
            allConflictingVHosts.addAll(vHosts);
        }

        return CONSOLE_WARNING_PREFIX + String.format(DUPLICATE_VIRTUAL_HOSTNAME_WARNING_TEMPLATE, String.join(", ", conflictingDomains.keySet()));
    }
}
