// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

// use LinkedHashSet to preserve the order and uniqueness of entries,
// not that order/uniqueness matters to LDAP server, just cleaner this way
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Provisioning;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author pshao
 */
public class LdapObjectClass {

  /*
   * as of bug 60444, our default person OC for accounts is changed
   * from organizationalPerson to inetOrgPerson
   */
  public static String ZIMBRA_DEFAULT_PERSON_OC = "inetOrgPerson";

  private static void addExtraObjectClasses(Set<String> ocs, Provisioning prov, String extraOCAttr)
      throws ServiceException {
    String[] extraObjectClasses = prov.getConfig().getMultiAttr(extraOCAttr);
    ocs.addAll(Arrays.asList(extraObjectClasses));

    /*
    if (additionalObjectClasses != null) {
        for (int i = 0; i < additionalObjectClasses.length; i++)
            ocs.add(additionalObjectClasses[i]);
    }
    */
  }

  public static Set<String> getAccountObjectClasses(Provisioning prov, boolean zimbraDefaultOnly)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add(ZIMBRA_DEFAULT_PERSON_OC);
    ocs.add(AttributeClass.OC_zimbraAccount);

    if (!zimbraDefaultOnly)
      addExtraObjectClasses(ocs, prov, Provisioning.A_zimbraAccountExtraObjectClass);
    return ocs;
  }

  public static Set<String> getAccountObjectClasses(Provisioning prov) throws ServiceException {
    return getAccountObjectClasses(prov, false);
  }

  public static Set<String> getCalendarResourceObjectClasses(Provisioning prov)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add(AttributeClass.OC_zimbraCalendarResource);

    addExtraObjectClasses(ocs, prov, Provisioning.A_zimbraCalendarResourceExtraObjectClass);
    return ocs;
  }

  public static Set<String> getCosObjectClasses(Provisioning prov) throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add(AttributeClass.OC_zimbraCOS);

    addExtraObjectClasses(ocs, prov, Provisioning.A_zimbraCosExtraObjectClass);
    return ocs;
  }

  public static Set<String> getDomainObjectClasses(Provisioning prov) throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add("dcObject");
    ocs.add("organization");
    ocs.add(AttributeClass.OC_zimbraDomain);

    addExtraObjectClasses(ocs, prov, Provisioning.A_zimbraDomainExtraObjectClass);
    return ocs;
  }

  public static Set<String> getDistributionListObjectClasses(Provisioning prov)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();
    ocs.add(AttributeClass.OC_zimbraDistributionList);
    ocs.add(AttributeClass.OC_zimbraMailRecipient);
    return ocs;
  }

  public static Set<String> getGroupObjectClasses(Provisioning prov)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add("groupOfURLs");
    ocs.add("dgIdentityAux");
    ocs.add(AttributeClass.OC_zimbraGroup);
    // ocs.add(AttributeClass.OC_zimbraMailRecipient);  // should we?
    return ocs;
  }

  public static Set<String> getGroupDynamicUnitObjectClasses(Provisioning prov)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add("groupOfURLs");
    ocs.add("dgIdentityAux");
    ocs.add(AttributeClass.OC_zimbraGroupDynamicUnit);

    return ocs;
  }

  public static Set<String> getGroupStaticUnitObjectClasses(Provisioning prov)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add(AttributeClass.OC_zimbraGroupStaticUnit);

    return ocs;
  }

  public static Set<String> getServerObjectClasses(Provisioning prov) throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();

    ocs.add(AttributeClass.OC_zimbraServer);

    addExtraObjectClasses(ocs, prov, Provisioning.A_zimbraServerExtraObjectClass);
    return ocs;
  }

  public static Set<String> getShareLocatorObjectClasses(Provisioning prov)
      throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();
    ocs.add(AttributeClass.OC_zimbraShareLocator);
    return ocs;
  }

  public static Set<String> getOrganizationUnitObjectClasses() throws ServiceException {
    Set<String> ocs = new LinkedHashSet<>();
    ocs.add("top");
    ocs.add("organizationalUnit");
    return ocs;
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws ServiceException {
    Provisioning prov = Provisioning.getInstance();
    Set<String> ocs = LdapObjectClass.getCalendarResourceObjectClasses(prov);

    for (String oc : ocs) {
      System.out.println(oc);
    }
  }
}
