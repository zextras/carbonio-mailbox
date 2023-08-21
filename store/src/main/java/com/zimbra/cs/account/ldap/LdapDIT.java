// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.EmailUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Alias;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.GlobalGrant;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.ldap.LdapUtil;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.util.Zimbra;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author pshao
 */
public class LdapDIT {
  /*
   * This is our default ldap DIT.  All DNs/RDNs location is hardcoded to avoid
   * mis-configuration.
   *
   * To customize the DIT to a different layout, set the zimbra_class_provisioning
   * localconfig key to com.zimbra.cs.account.ldap.custom.CustomLdapProvisioning,
   * which will use the CustomLdapDIT class that can be customized by a set of
   * localconfig keys.
   *
   */

  /*
   * the Ldap provisioning instance that uses this DIT
   */
  protected LdapProv mProv;

  /*
   * Variable Naming Conventions:
   *
   *              RDN : {attr-name}={attr-value}
   *               DN : List of comma (,) seperated RDNs
   *          DEFAULT : Means the variable has a hardcoded value, which can be referred to
   *                    from subclasses, but cannot be changed.  If a subclass need to use
   *                    different values it has to define it's own variables.
   *
   *         BASE_RDN : A relative RDN under that entries of the same kind reside.
   * DEFAULT_BASE_RDN : A hardcoded BASE_RDN that cannot be changed in subclasses.
   *  NAMING_RDN_ATTR : The attribute for the left-most RDN of an entry. each entry type must have a NAMING_RDN_ATTR.
   *          BASE_DN : An absolute DN under that a left-most RDN resides.
   */

  protected static final String ROOT_DN = LdapConstants.DN_ROOT_DSE;

  /*
   * Defaults tht can be used in subclasses but cannot be changed in subclasses.
   * If a subclass need to use different values it has to define it's own variables.
   */
  protected final String DEFAULT_CONFIG_BASE_DN = "cn=zimbra";
  protected final String DEFAULT_MAIL_BASE_DN = ROOT_DN;

  protected final String DEFAULT_BASE_RDN_ADMIN = "cn=admins";
  protected final String DEFAULT_BASE_RDN_APPADMIN = "cn=appaccts";
  protected final String DEFAULT_BASE_RDN_ACCOUNT = "ou=people";
  protected final String DEFAULT_BASE_RDN_COS = "cn=cos";
  protected final String DEFAULT_BASE_RDN_DYNAMICGROUP = "cn=groups";
  protected final String DEFAULT_BASE_RDN_GLOBAL_DYNAMICGROUP = "cn=groups";
  protected final String DEFAULT_BASE_RDN_MIME = "cn=mime";
  protected final String DEFAULT_BASE_RDN_SERVER = "cn=servers";
  protected final String DEFAULT_BASE_RDN_SHARE_LOCATOR = "cn=sharelocators";
  protected final String DEFAULT_BASE_RDN_XMPPCOMPONENT = "cn=xmppcomponents";

  protected final String DEFAULT_NAMING_RDN_ATTR_USER = "uid";
  protected final String DEFAULT_NAMING_RDN_ATTR_COS = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_DYNAMICGROUP = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_GLOBALCONFIG = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_GLOBALGRANT = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_MIME = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_SERVER = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_SHARE_LOCATOR = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_XMPPCOMPONENT = "cn";
  protected final String DEFAULT_NAMING_RDN_ATTR_HAB = "cn";

  /*
   * Variables that has to be set in the init method
   */
  protected String BASE_DN_ZIMBRA;
  protected String BASE_DN_CONFIG_BRANCH;
  protected String BASE_DN_MAIL_BRANCH;

  protected String BASE_RDN_ACCOUNT;
  protected String BASE_RDN_DYNAMICGROUP;

  protected String BASE_DN_ADMIN;
  protected String BASE_DN_APPADMIN;
  protected String BASE_DN_ACCOUNT;
  protected String BASE_DN_COS;
  protected String BASE_DN_GLOBAL_DYNAMICGROUP;
  protected String BASE_DN_MIME;
  protected String BASE_DN_SERVER;
  protected String BASE_DN_SHARE_LOCATOR;
  protected String BASE_DN_XMPPCOMPONENT;

  protected String NAMING_RDN_ATTR_USER;
  protected String NAMING_RDN_ATTR_COS;
  protected String NAMING_RDN_ATTR_GLOBALCONFIG;
  protected String NAMING_RDN_ATTR_GLOBALGRANT;
  protected String NAMING_RDN_ATTR_DYNAMICGROUP;
  protected String NAMING_RDN_ATTR_MIME;
  protected String NAMING_RDN_ATTR_SERVER;
  protected String NAMING_RDN_ATTR_SHARE_LOCATOR;
  protected String NAMING_RDN_ATTR_XMPPCOMPONENT;
  protected String NAMING_RDN_ATTR_HAB;

  protected String DN_GLOBALCONFIG;
  protected String DN_GLOBALGRANT;

  public LdapDIT(LdapProv prov) {
    // our Provisioning instance
    mProv = prov;

    init();
    verify();
  }

  protected void init() {
    BASE_DN_CONFIG_BRANCH = DEFAULT_CONFIG_BASE_DN;
    BASE_DN_MAIL_BRANCH = ROOT_DN;

    BASE_RDN_ACCOUNT = DEFAULT_BASE_RDN_ACCOUNT;
    BASE_RDN_DYNAMICGROUP = DEFAULT_BASE_RDN_DYNAMICGROUP;

    NAMING_RDN_ATTR_USER = DEFAULT_NAMING_RDN_ATTR_USER;
    NAMING_RDN_ATTR_COS = DEFAULT_NAMING_RDN_ATTR_COS;
    NAMING_RDN_ATTR_DYNAMICGROUP = DEFAULT_NAMING_RDN_ATTR_DYNAMICGROUP;
    NAMING_RDN_ATTR_GLOBALCONFIG = DEFAULT_NAMING_RDN_ATTR_GLOBALCONFIG;
    NAMING_RDN_ATTR_GLOBALGRANT = DEFAULT_NAMING_RDN_ATTR_GLOBALGRANT;
    NAMING_RDN_ATTR_MIME = DEFAULT_NAMING_RDN_ATTR_MIME;
    NAMING_RDN_ATTR_SERVER = DEFAULT_NAMING_RDN_ATTR_SERVER;
    NAMING_RDN_ATTR_SHARE_LOCATOR = DEFAULT_NAMING_RDN_ATTR_SHARE_LOCATOR;
    NAMING_RDN_ATTR_XMPPCOMPONENT = DEFAULT_NAMING_RDN_ATTR_XMPPCOMPONENT;
    NAMING_RDN_ATTR_HAB = DEFAULT_NAMING_RDN_ATTR_HAB;

    DN_GLOBALCONFIG = NAMING_RDN_ATTR_GLOBALCONFIG + "=config" + "," + BASE_DN_CONFIG_BRANCH;
    DN_GLOBALGRANT = NAMING_RDN_ATTR_GLOBALGRANT + "=globalgrant" + "," + BASE_DN_CONFIG_BRANCH;

    BASE_DN_ADMIN = DEFAULT_BASE_RDN_ADMIN + "," + BASE_DN_CONFIG_BRANCH;
    BASE_DN_APPADMIN = DEFAULT_BASE_RDN_APPADMIN + "," + BASE_DN_CONFIG_BRANCH;
    BASE_DN_COS = DEFAULT_BASE_RDN_COS + "," + BASE_DN_CONFIG_BRANCH;
    BASE_DN_GLOBAL_DYNAMICGROUP =
        DEFAULT_BASE_RDN_GLOBAL_DYNAMICGROUP + "," + BASE_DN_CONFIG_BRANCH;
    BASE_DN_MIME = DEFAULT_BASE_RDN_MIME + "," + DN_GLOBALCONFIG;
    BASE_DN_SERVER = DEFAULT_BASE_RDN_SERVER + "," + BASE_DN_CONFIG_BRANCH;
    BASE_DN_SHARE_LOCATOR = DEFAULT_BASE_RDN_SHARE_LOCATOR + "," + BASE_DN_CONFIG_BRANCH;
    BASE_DN_XMPPCOMPONENT = DEFAULT_BASE_RDN_XMPPCOMPONENT + "," + BASE_DN_CONFIG_BRANCH;

    BASE_DN_ZIMBRA = ROOT_DN;
  }

  private void verify() {
    if (BASE_DN_ZIMBRA == null
        || BASE_DN_CONFIG_BRANCH == null
        || BASE_DN_MAIL_BRANCH == null
        || BASE_RDN_ACCOUNT == null
        || BASE_RDN_DYNAMICGROUP == null
        || NAMING_RDN_ATTR_USER == null
        || NAMING_RDN_ATTR_COS == null
        || NAMING_RDN_ATTR_DYNAMICGROUP == null
        || NAMING_RDN_ATTR_GLOBALCONFIG == null
        || NAMING_RDN_ATTR_GLOBALGRANT == null
        || NAMING_RDN_ATTR_MIME == null
        || NAMING_RDN_ATTR_SERVER == null
        || NAMING_RDN_ATTR_SHARE_LOCATOR == null
        || BASE_DN_ADMIN == null
        || BASE_DN_APPADMIN == null
        || BASE_DN_COS == null
        || BASE_DN_GLOBAL_DYNAMICGROUP == null
        || BASE_DN_MIME == null
        || BASE_DN_SERVER == null
        || BASE_DN_XMPPCOMPONENT == null
        || DN_GLOBALCONFIG == null
        || DN_GLOBALGRANT == null) Zimbra.halt("Unable to initialize LDAP DIT");
  }

  public static boolean isZimbraDefault(LdapDIT dit) {
    return dit.getClass() == LdapDIT.class;
  }

  /*
   * Zimbra root
   */
  public String zimbraBaseDN() {
    return BASE_DN_ZIMBRA;
  }

  /*
   * config branch
   */
  public String configBranchBaseDN() {
    return BASE_DN_CONFIG_BRANCH;
  }

  /*
   * mail branch
   */
  public String mailBranchBaseDN() {
    return BASE_DN_MAIL_BRANCH;
  }

  /*
   * ===========
   *   account
   * ===========
   */
  public String accountNamingRdnAttr() {
    return NAMING_RDN_ATTR_USER;
  }

  protected String emailToDN(String localPart, String domain) throws ServiceException {
    return NAMING_RDN_ATTR_USER
        + "="
        + LdapUtil.escapeRDNValue(localPart)
        + ","
        + domainToAccountBaseDN(domain);
  }

  protected String emailToDN(String email) throws ServiceException {
    String[] parts = EmailUtil.getLocalPartAndDomain(email);
    return emailToDN(parts[0], parts[1]);
  }

  public String accountDNCreate(String baseDn, IAttributes attrs, String localPart, String domain)
      throws ServiceException {
    // sanity check, the default DIT does not support a supplied base
    if (baseDn != null)
      throw ServiceException.INVALID_REQUEST(
          "base dn is not supported in DIT impl " + getClass().getCanonicalName(), null);

    return emailToDN(localPart, domain);
  }

  public String accountDNRename(String oldDn, String newLocalPart, String newDomain)
      throws ServiceException {
    return emailToDN(newLocalPart, newDomain);
  }

  public String dnToEmail(String dn, IAttributes attrs) throws ServiceException {
    return dnToEmail(dn, null, attrs);
  }

  /*
   * Given a dn like "uid=foo,ou=people,dc=widgets,dc=com", return the string "foo@widgets.com".
   *
   * If namingAttr is not null, use the provided namingAttr for the localpart.
   * If namingAttr is null, first try using the account/dl rdn, if no such attrs,
   * then try the dynamci group rdn.
   *
   * Param attrs is not used in this implementation of DIT
   */
  public String dnToEmail(String dn, String namingAttr, IAttributes attrs) throws ServiceException {
    String[] parts = dn.split(",");
    StringBuffer domain = new StringBuffer(dn.length());

    String alternateNamingAttr = null;

    if (namingAttr != null) {
      namingAttr = namingAttr + "=";
    } else {
      namingAttr = accountNamingRdnAttr() + "=";
      alternateNamingAttr = dynamicGroupNamingRdnAttr() + "=";
    }

    String namingAttrValue = null;

    for (int i = 0; i < parts.length; i++) {
      if (parts[i].startsWith("dc=")) {
        if (domain.length() > 0) domain.append(".");
        domain.append(LdapUtil.unescapeRDNValue(parts[i].substring(3)));
      } else if (i == 0 && parts[i].startsWith(namingAttr)) {
        namingAttrValue = LdapUtil.unescapeRDNValue(parts[i].substring(namingAttr.length()));
      } else if (alternateNamingAttr != null
          && i == 0
          && parts[i].startsWith(alternateNamingAttr)) {
        namingAttrValue =
            LdapUtil.unescapeRDNValue(parts[i].substring(alternateNamingAttr.length()));
      }
    }
    if (namingAttrValue == null) {
      throw ServiceException.FAILURE("unable to map dn [" + dn + "] to email", null);
    }
    if (domain.length() == 0) {
      return namingAttrValue;
    }
    return new StringBuffer(namingAttrValue).append('@').append(domain).toString();
  }

  /*
   * =================
   *   admin account
   * =================
   */
  public String adminBaseDN() {
    return BASE_DN_ADMIN;
  }

  public String appAdminBaseDN() {
    return BASE_DN_APPADMIN;
  }

  /*
  public String adminNameToDN(String name) {
      return NAMING_RDN_ATTR_USER + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_ADMIN;
  }
  */

  /*
   * ==========
   *   alias
   * ==========
   */
  public String aliasDN(
      String targetDn, String targetDomain, String aliasLocalPart, String aliasDomain)
      throws ServiceException {
    return emailToDN(aliasLocalPart, aliasDomain);
  }

  public String aliasDNRename(String targetNewDn, String targetNewDomain, String newAliasEmail)
      throws ServiceException {
    return emailToDN(newAliasEmail);
  }

  /* =================
   * calendar resource
   * =================
   */

  /*
   * =======
   *   COS
   * =======
   */
  public String cosBaseDN() {
    return BASE_DN_COS;
  }

  public String cosNametoDN(String name) {
    return NAMING_RDN_ATTR_COS + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_COS;
  }

  /*
   * =====================
   *   distribution list
   * =====================
   */
  public String distributionListDNCreate(
      String baseDn, IAttributes attrs, String localPart, String domain) throws ServiceException {
    // sanity check, the default DIT does not support a supplied base
    if (baseDn != null)
      throw ServiceException.INVALID_REQUEST(
          "base dn is not supported in DIT impl " + getClass().getCanonicalName(), null);

    return emailToDN(localPart, domain);
  }

  public String distributionListDNRename(String oldDn, String newLocalPart, String newDomain)
      throws ServiceException {
    return emailToDN(newLocalPart, newDomain);
  }

  /*
   * =====================
   *   hab group
   * =====================
   */
  public String habGroupDNCreate(String orgUnitDN, String localPart) throws ServiceException {
    if (localPart == null || orgUnitDN == null) {
      throw ServiceException.INVALID_REQUEST("localPart and orgUnitDN cannot be null", null);
    }
    return NAMING_RDN_ATTR_HAB + "=" + LdapUtil.escapeRDNValue(localPart) + "," + orgUnitDN;
  }

  /*
   * ==========
   *   domain
   * ==========
   */
  public String domainBaseDN() {
    return mailBranchBaseDN();
  }

  public String domainNameToDN(String domainName) {
    String parts[] = domainName.split("\\.");
    String dns[] = domainToDNs(parts);
    return dns[0];
  }

  /**
   * Given a domain like foo.com, return an array of dns that work their way up the tree: [0] =
   * dc=foo,dc=com [1] = dc=com
   *
   * @return the array of DNs
   */
  public String[] domainToDNs(String[] parts) {
    return domainToDNsInternal(parts, null);
  }

  protected String[] domainToDNsInternal(String[] parts, String base) {
    String dns[] = new String[parts.length];
    for (int i = parts.length - 1; i >= 0; i--) {
      dns[i] = LdapUtil.domainToDN(parts, i);
      if (base != null) {
        dns[i] = dns[i] + "," + base;
      }
    }
    return dns;
  }

  // account base search dn
  public String domainToAccountSearchDN(String domain) throws ServiceException {
    return domainDNToAccountBaseDN(LdapUtil.domainToDN(domain));
  }

  // account base search dn
  public String domainDNToAccountSearchDN(String domainDN) throws ServiceException {
    return domainDNToAccountBaseDN(domainDN);
  }

  // only used internally
  private String domainToAccountBaseDN(String domain) throws ServiceException {
    return domainDNToAccountBaseDN(LdapUtil.domainToDN(domain));
  }

  // account base dn for create/delete domain
  public String domainDNToAccountBaseDN(String domainDN) throws ServiceException {
    if (BASE_RDN_ACCOUNT.length() == 0) return domainDN;
    else return BASE_RDN_ACCOUNT + "," + domainDN;
  }

  // dynamic group base dn for create/delete domain
  public String domainDNToDynamicGroupsBaseDN(String domainDN) throws ServiceException {
    if (BASE_RDN_DYNAMICGROUP.length() == 0) return domainDN;
    else return BASE_RDN_DYNAMICGROUP + "," + domainDN;
  }

  /*
   * ==============
   *   dynamic group
   * ==============
   */
  public String globalDynamicGroupBaseDN() {
    return BASE_DN_GLOBAL_DYNAMICGROUP;
  }

  public String dynamicGroupNamingRdnAttr() {
    return NAMING_RDN_ATTR_DYNAMICGROUP;
  }

  public String dynamicGroupNameLocalPartToDN(String name, String domainDN)
      throws ServiceException {
    return NAMING_RDN_ATTR_DYNAMICGROUP
        + "="
        + LdapUtil.escapeRDNValue(name)
        + ","
        + domainDNToDynamicGroupsBaseDN(domainDN);
  }

  public String dynamicGroupUnitNameToDN(String unitName, String parentDN) throws ServiceException {
    return NAMING_RDN_ATTR_DYNAMICGROUP + "=" + LdapUtil.escapeRDNValue(unitName) + "," + parentDN;
  }

  public String filterDynamicGroupsByDomain(Domain domain, boolean includeObjectClass) {
    if (includeObjectClass) return "(objectclass=zimbraGroup)";
    else return "";
  }

  public String dynamicGroupDNRename(String oldDn, String newLocalPart, String newDomain)
      throws ServiceException {
    String newDomainDN = domainNameToDN(newDomain);
    return dynamicGroupNameLocalPartToDN(newLocalPart, newDomainDN);
  }

  /*
   * ==============
   *   group (static and dynamic neutral)
   * ==============
   */
  public String domainDNToGroupsBaseDN(String domainDN) throws ServiceException {
    return domainDN;
  }

  /*
  public String filterGroupsByDomain(Domain domain, boolean includeObjectClass) {
      if (includeObjectClass)
          return "(|(objectclass=zimbraGroup)(objectclass=zimbraDistributionList))";
      else
          return "";
  }
  */

  /*
   * ==============
   *   globalconfig
   * ==============
   */
  public String configDN() {
    return DN_GLOBALCONFIG;
  }

  /*
   * =====================
   *   globalgrant
   * =====================
   */
  public String globalGrantDN() {
    return DN_GLOBALGRANT;
  }

  /*
   * ========
   *   mime
   * ========
   */
  /*
  public String mimeConfigToDN(String name) {
      name = LdapUtil.escapeRDNValue(name);
      return NAMING_RDN_ATTR_MIME + "=" + name + "," + BASE_DN_MIME;
  }
  */

  public String mimeBaseDN() {
    return BASE_DN_MIME;
  }

  /*
   * ==========
   *   server
   * ==========
   */
  public String serverBaseDN() {
    return BASE_DN_SERVER;
  }

  public String serverNameToDN(String name) {
    return NAMING_RDN_ATTR_SERVER + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_SERVER;
  }

  /*
   * ==========
   *   share locator
   * ==========
   */
  public String shareLocatorBaseDN() {
    return BASE_DN_SHARE_LOCATOR;
  }

  public String shareLocatorIdToDN(String id) {
    return NAMING_RDN_ATTR_SHARE_LOCATOR
        + "="
        + LdapUtil.escapeRDNValue(id)
        + ","
        + BASE_DN_SHARE_LOCATOR;
  }

  /*
   * ==========
   *   xmppcomponent
   * ==========
   */
  public String xmppcomponentBaseDN() {
    return BASE_DN_XMPPCOMPONENT;
  }

  public String xmppcomponentNameToDN(String name) {
    return NAMING_RDN_ATTR_XMPPCOMPONENT
        + "="
        + LdapUtil.escapeRDNValue(name)
        + ","
        + BASE_DN_XMPPCOMPONENT;
  }

  /*
   * ==========
   *   filters
   * ==========
   */
  /*
   * returns the search filter for getting all accounts/calendar resources
   * on the specified domain and server.
   *
   * domain parameter is not used in default DIT because the search base is
   * restricted to the domain dn.
   *
   * In the custom DIT, extra filters can be generated based on the domain,
   * because the custom DIT is not organized by domain.
   */
  public ZLdapFilter filterAccountsByDomainAndServer(Domain domain, Server server) {
    return ZLdapFilterFactory.getInstance().accountsHomedOnServer(server.getServiceHostname());
  }

  public ZLdapFilter filterAccountsOnlyByDomainAndServer(Domain domain, Server server) {
    return ZLdapFilterFactory.getInstance()
        .accountsHomedOnServerAccountsOnly(server.getServiceHostname());
  }

  public ZLdapFilter filterCalendarResourceByDomainAndServer(Domain domain, Server server) {
    return ZLdapFilterFactory.getInstance()
        .calendarResourcesHomedOnServer(server.getServiceHostname());
  }

  public ZLdapFilter filterAccountsOnlyByDomain(Domain domain) {
    return ZLdapFilterFactory.getInstance().allAccountsOnly();
  }

  public ZLdapFilter filterCalendarResourcesByDomain(Domain domain) {
    return ZLdapFilterFactory.getInstance().allCalendarResources();
  }

  public ZLdapFilter filterDistributionListsByDomain(Domain domain) {
    return ZLdapFilterFactory.getInstance().allDistributionLists();
  }

  public ZLdapFilter filterGroupsByDomain(Domain domain) {
    return ZLdapFilterFactory.getInstance().allGroups();
  }

  public ZLdapFilter filterHabGroupsByDn() {
    return ZLdapFilterFactory.getInstance().allHabGroups();
  }

  /*
   * ========================================================================================
   */
  public SpecialAttrs handleSpecialAttrs(Map<String, Object> attrs) throws ServiceException {
    SpecialAttrs specialAttrs = new SpecialAttrs();
    if (attrs != null) {
      specialAttrs.handleZimbraId(attrs);

      // default is don't support pseudo attrs
      // if the pseudo attr is present and not handled here, a NamingExeption will be thrown
      // when the entry is being created

    }
    return specialAttrs;
  }

  public String getNamingRdnAttr(Entry entry) throws ServiceException {
    if (entry instanceof Account || entry instanceof DistributionList || entry instanceof Alias)
      return NAMING_RDN_ATTR_USER;
    else if (entry instanceof Cos) return NAMING_RDN_ATTR_COS;
    else if (entry instanceof Config) return NAMING_RDN_ATTR_GLOBALCONFIG;
    else if (entry instanceof DataSource) return Provisioning.A_zimbraDataSourceName;
    else if (entry instanceof Domain) return Provisioning.A_dc;
    else if (entry instanceof Identity) return Provisioning.A_zimbraPrefIdentityName;
    else if (entry instanceof GlobalGrant) return NAMING_RDN_ATTR_GLOBALGRANT;
    else if (entry instanceof DynamicGroup) return NAMING_RDN_ATTR_DYNAMICGROUP;
    else if (entry instanceof Server) return NAMING_RDN_ATTR_SERVER;
    else
      throw ServiceException.FAILURE(
          "entry type "
              + entry.getClass().getCanonicalName()
              + " is not supported by getNamingRdnAttr",
          null);
  }

  /**
   * returns if dn is under parentDn
   *
   * @param parentDn
   * @param dn
   * @return
   */
  public boolean isUnder(String parentDn, String dn) {

    if (!parentDn.equals(ROOT_DN)) {
      return dn.toLowerCase().endsWith(parentDn.toLowerCase());
    }
    return true;
  }

  private void addBase(Set<String> bases, String base) {
    boolean add = true;
    for (String b : bases) {
      if (isUnder(b, base)) {
        add = false;
        break;
      }
    }
    if (add) {
      bases.add(base);
    }
  }

  public String[] getSearchBases(int flags) {
    Set<String> bases = new HashSet<String>();

    boolean accounts = (flags & Provisioning.SD_ACCOUNT_FLAG) != 0;
    boolean aliases = (flags & Provisioning.SD_ALIAS_FLAG) != 0;
    boolean lists = (flags & Provisioning.SD_DISTRIBUTION_LIST_FLAG) != 0;
    boolean dynamicgroups = (flags & Provisioning.SD_DYNAMIC_GROUP_FLAG) != 0;
    boolean calendarResources = (flags & Provisioning.SD_CALENDAR_RESOURCE_FLAG) != 0;
    boolean domains = (flags & Provisioning.SD_DOMAIN_FLAG) != 0;
    boolean coses = (flags & Provisioning.SD_COS_FLAG) != 0;
    boolean servers = (flags & Provisioning.SD_SERVER_FLAG) != 0;
    boolean habgroups = (flags & Provisioning.SD_HAB_FLAG) != 0;

    if (accounts || aliases || lists || dynamicgroups || calendarResources) {
      addBase(bases, mailBranchBaseDN());
    }

    if (accounts) {
      addBase(bases, adminBaseDN());
    }

    if (domains || habgroups) {
      addBase(bases, domainBaseDN());
    }

    if (coses) {
      addBase(bases, cosBaseDN());
    }

    if (servers) {
      addBase(bases, serverBaseDN());
    }

    return bases.toArray(new String[0]);
  }
}
