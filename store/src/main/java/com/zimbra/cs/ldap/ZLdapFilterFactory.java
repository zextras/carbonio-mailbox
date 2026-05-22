// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.google.common.collect.Lists;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.unboundid.UBIDLdapException;
import java.util.Collection;
import java.util.List;

public class ZLdapFilterFactory extends ZLdapElement {

  private static ZLdapFilterFactory SINGLETON;

  public static synchronized void setInstance(ZLdapFilterFactory factory) {
    SINGLETON = factory;
  }

  public static ZLdapFilterFactory getInstance() {
    return SINGLETON;
  }

  @Override
  public void debug() {}

  /*
   * canned filters
   */
  private static Filter FILTER_ALL_ACCOUNTS; // including calendar resources
  private static Filter FILTER_ALL_ACCOUNTS_ONLY; // excluding calendar resources
  private static Filter FILTER_ALL_ADMIN_ACCOUNTS;
  private static Filter FILTER_ALL_ALIASES;
  private static Filter FILTER_ALL_CALENDAR_RESOURCES;
  private static Filter FILTER_ALL_COSES;
  private static Filter FILTER_ALL_DATASOURCES;
  private static Filter FILTER_ALL_DISTRIBUTION_LISTS;
  private static Filter FILTER_ALL_DOMAINS;
  private static Filter FILTER_ALL_DYNAMIC_GROUPS;
  private static Filter FILTER_ALL_DYNAMIC_GROUP_DYNAMIC_UNITS;
  private static Filter FILTER_ALL_DYNAMIC_GROUP_STATIC_UNITS;
  private static Filter FILTER_ALL_GROUPS;
  private static Filter FILTER_ALL_IDENTITIES;
  private static Filter FILTER_ALL_MIME_ENTRIES;
  private static Filter FILTER_ALL_NON_SYSTEM_ACCOUNTS;
  private static Filter FILTER_ALL_NON_SYSTEM_ARCHIVING_ACCOUNTS;
  private static Filter FILTER_ALL_NON_SYSTEM_INTERNAL_ACCOUNTS;
  private static Filter FILTER_ALL_SERVERS;
  private static Filter FILTER_ALL_SHARE_LOCATORS;
  private static Filter FILTER_ALL_SIGNATURES;
  private static Filter FILTER_ALL_ZIMLETS;
  private static Filter FILTER_ANY_ENTRY;
  private static Filter FILTER_DOMAIN_LABEL;
  private static Filter FILTER_HAS_SUBORDINATES;
  private static Filter FILTER_IS_ARCHIVING_ACCOUNT;
  private static Filter FILTER_IS_EXTERNAL_ACCOUNT;
  private static Filter FILTER_IS_SYSTEM_RESOURCE;
  private static Filter FILTER_NOT_SYSTEM_RESOURCE;
  private static Filter FILTER_PUBLIC_SHARE;
  private static Filter FILTER_ALLAUTHED_SHARE;
  private static Filter FILTER_NOT_EXCLUDED_FROM_CMB_SEARCH;
  private static Filter FILTER_WITH_ARCHIVE;
  private static Filter FILTER_ALL_INTERNAL_ACCOUNTS;
  private static Filter FILTER_ALL_ADDRESS_LISTS;

  public static synchronized void initialize() throws LdapException {

    try {
      _initialize();
    } catch (LDAPException e) {
      throw UBIDLdapException.mapToLdapException(e);
    }
  }

  /**
   * initialize canned filters
   */
  private static void _initialize() throws LDAPException {

    /*
     * self-defined filters
     */
    FILTER_ALL_ACCOUNTS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraAccount);

    FILTER_ALL_ALIASES =
        Filter.createEqualityFilter(LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraAlias);

    FILTER_ALL_CALENDAR_RESOURCES =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraCalendarResource);

    FILTER_ALL_COSES =
        Filter.createEqualityFilter(LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraCOS);

    FILTER_ALL_DATASOURCES =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraDataSource);

    FILTER_ALL_DISTRIBUTION_LISTS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraDistributionList);

    FILTER_ALL_DOMAINS =
        Filter.createEqualityFilter(LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraDomain);

    FILTER_ALL_DYNAMIC_GROUPS =
        Filter.createEqualityFilter(LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraGroup);

    FILTER_ALL_DYNAMIC_GROUP_DYNAMIC_UNITS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraGroupDynamicUnit);

    FILTER_ALL_DYNAMIC_GROUP_STATIC_UNITS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraGroupStaticUnit);

    FILTER_ALL_IDENTITIES =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraIdentity);

    FILTER_ALL_MIME_ENTRIES =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraMimeEntry);

    FILTER_ALL_SERVERS =
        Filter.createEqualityFilter(LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraServer);

    FILTER_ALL_SHARE_LOCATORS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraShareLocator);

    FILTER_ALL_SIGNATURES =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraSignature);

    FILTER_ALL_ZIMLETS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraZimletEntry);

    FILTER_ANY_ENTRY = Filter.createPresenceFilter(LdapConstants.ATTR_objectClass);

    FILTER_DOMAIN_LABEL =
        Filter.createEqualityFilter(LdapConstants.ATTR_objectClass, LdapConstants.OC_dcObject);

    FILTER_HAS_SUBORDINATES =
        Filter.createEqualityFilter(LdapConstants.ATTR_hasSubordinates, LdapConstants.LDAP_TRUE);

    FILTER_IS_ARCHIVING_ACCOUNT =
        Filter.createPresenceFilter(Provisioning.A_amavisArchiveQuarantineTo);

    FILTER_IS_EXTERNAL_ACCOUNT =
        Filter.createEqualityFilter(
            Provisioning.A_zimbraIsExternalVirtualAccount, LdapConstants.LDAP_TRUE);

    FILTER_IS_SYSTEM_RESOURCE =
        Filter.createEqualityFilter(Provisioning.A_zimbraIsSystemResource, LdapConstants.LDAP_TRUE);

    FILTER_NOT_SYSTEM_RESOURCE = Filter.createNOTFilter(FILTER_IS_SYSTEM_RESOURCE);

    FILTER_PUBLIC_SHARE =
        Filter.createSubstringFilter(
            Provisioning.A_zimbraSharedItem, null, new String[] {"granteeType:pub"}, null);

    FILTER_ALLAUTHED_SHARE =
        Filter.createSubstringFilter(
            Provisioning.A_zimbraSharedItem, null, new String[] {"granteeType:all"}, null);

    FILTER_NOT_EXCLUDED_FROM_CMB_SEARCH =
        Filter.createORFilter(
            Filter.createNOTFilter(
                Filter.createPresenceFilter(Provisioning.A_zimbraExcludeFromCMBSearch)),
            Filter.createEqualityFilter(Provisioning.A_zimbraExcludeFromCMBSearch, "FALSE"));

    FILTER_WITH_ARCHIVE = Filter.createPresenceFilter(Provisioning.A_zimbraArchiveAccount);

    /*
     * filters built on top of other filters
     */
    FILTER_ALL_ACCOUNTS_ONLY =
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS, Filter.createNOTFilter(FILTER_ALL_CALENDAR_RESOURCES));

    FILTER_ALL_ADMIN_ACCOUNTS =
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS,
            Filter.createORFilter(
                Filter.createEqualityFilter(
                    Provisioning.A_zimbraIsAdminAccount, LdapConstants.LDAP_TRUE),
                Filter.createEqualityFilter(
                    Provisioning.A_zimbraIsDelegatedAdminAccount, LdapConstants.LDAP_TRUE),
                Filter.createEqualityFilter(
                    Provisioning.A_zimbraIsDomainAdminAccount, LdapConstants.LDAP_TRUE)));

    FILTER_ALL_NON_SYSTEM_ACCOUNTS =
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY, Filter.createNOTFilter(FILTER_IS_SYSTEM_RESOURCE));

    FILTER_ALL_NON_SYSTEM_ARCHIVING_ACCOUNTS =
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY,
            Filter.createNOTFilter(FILTER_IS_SYSTEM_RESOURCE),
            FILTER_IS_ARCHIVING_ACCOUNT);

    FILTER_ALL_NON_SYSTEM_INTERNAL_ACCOUNTS =
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS,
            Filter.createNOTFilter(FILTER_IS_SYSTEM_RESOURCE),
            Filter.createNOTFilter(FILTER_ALL_CALENDAR_RESOURCES),
            Filter.createNOTFilter(FILTER_IS_EXTERNAL_ACCOUNT));

    FILTER_ALL_GROUPS =
        Filter.createORFilter(FILTER_ALL_DYNAMIC_GROUPS, FILTER_ALL_DISTRIBUTION_LISTS);

    FILTER_ALL_INTERNAL_ACCOUNTS =
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS, Filter.createNOTFilter(FILTER_IS_EXTERNAL_ACCOUNT));

    FILTER_ALL_ADDRESS_LISTS =
        Filter.createEqualityFilter(
            LdapConstants.ATTR_objectClass, AttributeClass.OC_zimbraAddressList);
  }

  public enum FilterId {
    ACCOUNT_BY_ID(SINGLETON.accountById("{ACCOUNT-ID}")),
    ACCOUNT_BY_FOREIGN_PRINCIPAL(SINGLETON.accountByForeignPrincipal("{FOREIGN-PRINCIPAL}")),
    ACCOUNT_BY_MEMBEROF(SINGLETON.accountByMemberOf("{DYNAMIC-GROUP-ID}")),
    ACCOUNT_BY_NAME(SINGLETON.accountByName("{ACCOUNT-NAME}")),
    ACCOUNTS_BY_GRANTS(
        SINGLETON.accountsByGrants(
            Lists.newArrayList("{GRANTEE-ID-1}", "{GRANTEE-ID-2}", "..."), true, true)),
    ACCOUNTS_HOMED_ON_SERVER(SINGLETON.accountsHomedOnServer("{SERVER-SERVICE-HOSTNAME}")),
    ACCOUNTS_HOMED_ON_SERVER_ACCOUNTS_ONLY(
        SINGLETON.accountsHomedOnServerAccountsOnly("{SERVER-SERVICE-HOSTNAME}")),
    ACCOUNTS_ON_SERVER_AND_COS_HAS_SUBORDINATES(
        SINGLETON.accountsOnServerAndCosHasSubordinates("{SERVER-SERVICE-HOSTNAME}", "{COS-ID}")),
    ADDRS_EXIST(SINGLETON.addrsExist(new String[] {"{ADDR-1}", "{ADDR-2}", "..."})),
    ADMIN_ACCOUNT_BY_RDN(SINGLETON.adminAccountByRDN("{NAMING-RDN-ATTR}", "{NAME}")),
    ALL_ACCOUNTS(SINGLETON.allAccounts()),
    ALL_ACCOUNTS_ONLY(SINGLETON.allAccountsOnly()),
    ALL_ACCOUNTS_ONLY_BY_COS(SINGLETON.allAccountsOnlyByCos("{COS-ID}")),
    ALL_ADMIN_ACCOUNTS(SINGLETON.allAdminAccounts()),
    ALL_ALIASES(SINGLETON.allAliases()),
    ALL_CALENDAR_RESOURCES(SINGLETON.allCalendarResources()),
    ALL_COSES(SINGLETON.allCoses()),
    ALL_DATA_SOURCES(SINGLETON.allDataSources()),
    ALL_DISTRIBUTION_LISTS(SINGLETON.allDistributionLists()),
    ALL_DOMAINS(SINGLETON.allDomains()),
    ALL_GROUPS(SINGLETON.allGroups()),
    ALL_IDENTITIES(SINGLETON.allIdentities()),
    ALL_MIME_ENTRIES(SINGLETON.allMimeEntries()),
    ALL_NON_SYSTEM_ACCOUNTS(SINGLETON.allNonSystemAccounts()),
    ALL_NON_SYSTEM_ARCHIVING_ACCOUNTS(SINGLETON.allNonSystemArchivingAccounts()),
    ALL_NON_SYSTEM_INTERNAL_ACCOUNTS(SINGLETON.allNonSystemInternalAccounts()),
    ALL_SERVERS(SINGLETON.allServers()),
    ALL_SIGNATURES(SINGLETON.allSignatures()),
    ALL_ZIMLETS(SINGLETON.allZimlets()),
    ANY_ENTRY(SINGLETON.anyEntry()),
    CALENDAR_RESOURCE_BY_FOREIGN_PRINCIPAL(
        SINGLETON.calendarResourceByForeignPrincipal("{FOREIGN-PRINCIPAL}")),
    CALENDAR_RESOURCE_BY_ID(SINGLETON.calendarResourceById("{CALENDAR-RESOURCE-ID}")),
    CALENDAR_RESOURCE_BY_NAME(SINGLETON.calendarResourceByName("{CALENDAR-RESOURCE-NAME}")),
    CALENDAR_RESOURCES_HOMED_ON_SERVER(
        SINGLETON.calendarResourcesHomedOnServer("{SERVER-SERVICE-HOSTNAME}")),

    CMB_SEARCH_ACCOUNTS_ONLY(SINGLETON.CMBSearchAccountsOnly()),
    CMB_SEARCH_ACCOUNTS_ONLY_WITH_ARCHIVE(SINGLETON.CMBSearchAccountsOnlyWithArchive()),
    CMB_SEARCH_NON_SYSTEM_RESOURCE_ACCOUNTS_ONLY(
        SINGLETON.CMBSearchNonSystemResourceAccountsOnly()),

    COS_BY_ID(SINGLETON.cosById("{COS-ID}")),
    COSES_BY_MAILHOST_POOL(SINGLETON.cosesByMailHostPool("{SERVER-ID}")),
    CREATED_LATEROREQUAL(SINGLETON.createdLaterOrEqual("{GENERALIZED_TIME}")),
    DATA_SOURCE_BY_ID(SINGLETON.dataSourceById("{DATA-SOURCE-ID}")),
    DATA_SOURCE_BY_NAME(SINGLETON.dataSourceByName("{DATA-SOURCE-NAME}")),
    DISTRIBUTION_LIST_BY_ID(SINGLETON.distributionListById("{DISTRIBUTION-LIST-ID}")),
    DISTRIBUTION_LIST_BY_NAME(SINGLETON.distributionListByName("{DISTRIBUTION-LIST-NAME}")),
    DISTRIBUTION_LISTS_BY_MEMBER_ADDRS(
        SINGLETON.distributionListsByMemberAddrs(new String[] {"{ADDR-1}", "ADDR-2", "..."})),

    DN_SUBTREE_MATCH(SINGLETON.dnSubtreeMatch("dn1", "dn2")),

    DOMAIN_ALIASES(SINGLETON.domainAliases("{DOMAIN-ID}")),
    DOMAIN_BY_ID(SINGLETON.domainById("{DOMAIN-ID}")),
    DOMAINS_BY_IDS(
        SINGLETON.dynamicGroupByIds(new String[] {"{DOMAIN-ID-1}", "DOMAIN-ID-2", "..."})),
    DOMAIN_BY_NAME(SINGLETON.domainByName("{DOMAIN-NAME}")),
    DOMAIN_BY_KRB5_REALM(SINGLETON.domainByKrb5Realm("{DOMAIN-KRB5-REALM}")),
    DOMAIN_BY_VIRTUAL_HOSTNAME(SINGLETON.domainByVirtualHostame("{DOMAIN-VIRTUAL-HOSTNAME}")),
    DOMAIN_BY_FOREIGN_NAME(SINGLETON.domainByForeignName("{DOMAIN-FOREIGN-NAME}")),
    DOMAIN_LABEL(SINGLETON.domainLabel()),
    DOMAIN_LOCKED_FOR_AUTO_PROVISION(SINGLETON.domainLockedForEagerAutoProvision()),
    ALL_DYNAMIC_GROUPS(SINGLETON.allDynamicGroups()),
    DYNAMIC_GROUP_BY_ID(SINGLETON.dynamicGroupById("{DYNAMIC-GROUP-ID}")),
    DYNAMIC_GROUP_BY_IDS(
        SINGLETON.dynamicGroupByIds(new String[] {"{GROUP-ID-1}", "GROUP-ID-2", "..."})),
    DYNAMIC_GROUP_BY_NAME(SINGLETON.dynamicGroupByName("{DYNAMIC-GROUP-NAME}")),
    DYNAMIC_GROUP_DYNAMIC_UNIT_BY_MAIL_ADDR(SINGLETON.dynamicGroupDynamicUnitByMailAddr("{ADDR}")),
    DYNAMIC_GROUPS_STATIC_UNIT_BY_MEMBER_ADDR(
        SINGLETON.dynamicGroupsStaticUnitByMemberAddr("{ADDR}")),

    EXTERNAL_ACCOUNTS_HOMED_ON_SERVER(
        SINGLETON.externalAccountsHomedOnServer("{SERVER-SERVICE-HOSTNAME}")),
    GLOBAL_CONFIG(SINGLETON.globalConfig()),
    GROUP_BY_ID(SINGLETON.groupById("{GROUP-ID}")),
    GROUP_BY_NAME(SINGLETON.groupByName("{GROUP-NAME}")),
    HAS_SUBORDINATES(SINGLETON.hasSubordinates()),
    HOMED_ON_SERVER(SINGLETON.homedOnServer("{SERVER-SERVICE-HOSTNAME}")),
    IDENTITY_BY_NAME(SINGLETON.identityByName("{IDENTITY-NAME}")),
    MEMBER_OF(SINGLETON.memberOf("{DN-OF-GROUP}")),
    MIME_ENTRY_BY_MIME_TYPE(SINGLETON.mimeEntryByMimeType("{MIME-TYPE}")),

    SERVER_BY_ID(SINGLETON.serverById("{SERVER-ID}")),
    SERVER_BY_SERVICE(SINGLETON.serverByService("{SERVICE}")),
    SHARE_LOCATOR_BY_ID(SINGLETON.shareLocatorById("{SHARE-LOCATOR-ID}")),
    SIGNATURE_BY_ID(SINGLETON.signatureById("{SIGNATURE-ID}")),

    // filters only used in the Velodrome DIT
    VELODROME_ALL_ACCOUNTS_BY_DOMAIN(SINGLETON.velodromeAllAccountsByDomain("{DOMAIN-NAME}")),
    VELODROME_ALL_ACCOUNTS_ONLY_BY_DOMAIN(
        SINGLETON.velodromeAllAccountsOnlyByDomain("{DOMAIN-NAME}")),
    VELODROME_ALL_CALENDAR_RESOURCES_BY_DOMAIN(
        SINGLETON.velodromeAllCalendarResourcesByDomain("{DOMAIN-NAME}")),
    VELODROME_ALL_ACCOUNTS_BY_DOMAIN_AND_SERVER(
        SINGLETON.velodromeAllAccountsByDomainAndServer(
            "{DOMAIN-NAME}", "{SERVER-SERVICE-HOSTNAME}")),
    VELODROME_ALL_ACCOUNTS_ONLY_BY_DOMAIN_AND_SERVER(
        SINGLETON.velodromeAllAccountsOnlyByDomainAndServer(
            "{DOMAIN-NAME}", "{SERVER-SERVICE-HOSTNAME}")),
    VELODROME_ALL_CALENDAR_RESOURCES_BY_DOMAIN_AND_SERVER(
        SINGLETON.velodromeAllCalendarResourcesByDomainAndServer(
            "{DOMAIN-NAME}", "{SERVER-SERVICE-HOSTNAME}")),
    VELODROME_ALL_DISTRIBUTION_LISTS_BY_DOMAIN(
        SINGLETON.velodromeAllDistributionListsByDomain("{DOMAIN-NAME}")),
    VELODROME_ALL_GROUPS_BY_DOMAIN(SINGLETON.velodromeAllGroupsByDomain("{DOMAIN-NAME}")),

    // address lists
    ALL_ADDRESS_LISTS(SINGLETON.allAddressLists()),
    ADDRESS_LIST_BY_ID(SINGLETON.addressListById("{ADDRESS-LIST-ID}")),
    ADDRESS_LIST_BY_NAME(SINGLETON.addressListByName("{ADDRESS-LIST-NAME}")),

    //
    // =====================================
    // FilterId for fromFilterString() calls
    // =====================================
    //
    ACCOUNT_BY_SSL_CLENT_CERT_PRINCIPAL_MAP(
        SINGLETON.allAccounts()
            + " AND "
            + "filter in "
            + Provisioning.A_zimbraMailSSLClientCertPrincipalMap),
    ADMIN_SEARCH("Admin search"),
    AUTO_PROVISION_ADMIN_SEARCH("Admin entered filter"),
    AUTO_PROVISION_SEARCH("Filter in " + Provisioning.A_zimbraAutoProvLdapSearchFilter),
    AUTO_PROVISION_SEARCH_LATER_THAN_EQUAL(
        "Filter in "
            + Provisioning.A_zimbraAutoProvLdapSearchFilter
            + " AND "
            + SINGLETON.createdLaterOrEqual("{GENERALIZED_TIME}")),
    EXTERNAL_GROUP("Filter in " + Provisioning.A_zimbraExternalGroupLdapSearchFilter),
    GAL_SEARCH("GAL search"),
    LDAP_AUTHENTICATE("Filter in " + Provisioning.A_zimbraAuthLdapSearchFilter),
    NGINX_GET_DOMAIN_BY_SERVER_IP("Filter in "),
    NGINX_GET_PORT_BY_MAILHOST("Filter in "),
    NGINX_GET_MAILHOST("Filter in " + Provisioning.A_zimbraReverseProxyMailHostQuery),
    RENAME_DOMAIN("Search entries during RenameDomain"),
    SEARCH_ALIAS_TARGET("Search alias target entry"),
    SEARCH_GRANTEE("Search grantee for revoking orphan grants"),

    UNITTEST("UNITTEST"),
    LDAP_UPGRADE("LDAP_UPGRADE"),
    ZMCONFIGD("ZMCONFIGD"),

    TODO("TODO");

    private final String template;

    FilterId(ZLdapFilter template) {
      this(template.toFilterString());
    }

    FilterId(String template) {
      this.template = template;
    }

    public String getStatString() {
      return LdapOp.SEARCH.name() + "_" + name();
    }
  }

  public String encodeValue(String value) {
    return Filter.encodeValue(value);
  }

  protected String encloseFilterIfNot(String filterString) {
    if (filterString.startsWith("(") && filterString.endsWith(")")) {
      return filterString;
    } else {
      return "(" + filterString + ")";
    }
  }

  private Filter homedOnServerFilter(String serverServiceHostname) {
    return Filter.createEqualityFilter(Provisioning.A_zimbraMailHost, serverServiceHostname);
  }

  public String presenceFilter(String attr) {
    return String.format(
        "(%s%s%s)", attr, LdapConstants.FILTER_TYPE_EQUAL, LdapConstants.FILTER_VALUE_ANY);
  }

  public String equalityFilter(String attr, String value, boolean valueIsRaw) {
    return String.format(
        "(%s%s%s)", attr, LdapConstants.FILTER_TYPE_EQUAL, valueIsRaw ? encodeValue(value) : value);
  }

  public String greaterOrEqualFilter(String attr, String value, boolean valueIsRaw) {
    return String.format(
        "(%s%s%s)",
        attr, LdapConstants.FILTER_TYPE_GREATER_OR_EQUAL, valueIsRaw ? encodeValue(value) : value);
  }

  public String lessOrEqualFilter(String attr, String value, boolean valueIsRaw) {
    return String.format(
        "(%s%s%s)",
        attr, LdapConstants.FILTER_TYPE_LESS_OR_EQUAL, valueIsRaw ? encodeValue(value) : value);
  }

  public String startsWithFilter(String attr, String value, boolean valueIsRaw) {
    return String.format(
        "(%s%s%s%s)",
        attr,
        LdapConstants.FILTER_TYPE_EQUAL,
        valueIsRaw ? encodeValue(value) : value,
        LdapConstants.FILTER_VALUE_ANY);
  }

  public String endsWithFilter(String attr, String value, boolean valueIsRaw) {
    return String.format(
        "(%s%s%s%s)",
        attr,
        LdapConstants.FILTER_TYPE_EQUAL,
        LdapConstants.FILTER_VALUE_ANY,
        valueIsRaw ? encodeValue(value) : value);
  }

  public String substringFilter(String attr, String value, boolean valueIsRaw) {
    return String.format(
        "(%s%s%s%s%s)",
        attr,
        LdapConstants.FILTER_TYPE_EQUAL,
        LdapConstants.FILTER_VALUE_ANY,
        valueIsRaw ? encodeValue(value) : value,
        LdapConstants.FILTER_VALUE_ANY);
  }

  /*
   * operational
   */
  public ZLdapFilter hasSubordinates() {
    return new ZLdapFilter(FilterId.HAS_SUBORDINATES, FILTER_HAS_SUBORDINATES);
  }

  public ZLdapFilter createdLaterOrEqual(String generalizedTime) {
    return new ZLdapFilter(
        FilterId.CREATED_LATEROREQUAL,
        Filter.createGreaterOrEqualFilter(LdapConstants.ATTR_createTimestamp, generalizedTime));
  }

  public ZLdapFilter timeLaterOrEqual(String column, String generalizedTime) {
    return new ZLdapFilter(
            FilterId.CREATED_LATEROREQUAL,
            Filter.createGreaterOrEqualFilter(column, generalizedTime));
  }

  /*
   * general
   */
  public ZLdapFilter anyEntry() {
    return new ZLdapFilter(FilterId.ANY_ENTRY, FILTER_ANY_ENTRY);
  }

  public ZLdapFilter fromFilterString(FilterId filterId, String filterString) throws LdapException {
    try {
      return new ZLdapFilter(filterId, Filter.create(encloseFilterIfNot(filterString)));
    } catch (LDAPException e) {
      throw UBIDLdapException.mapToLdapException(filterString, e);
    }
  }

  public ZLdapFilter andWith(ZLdapFilter filter, ZLdapFilter otherFilter) {
    ZLdapFilter andedFilter = null;
    try {
      andedFilter =
          new ZLdapFilter(
              filter.getFilterId(),
              Filter.createANDFilter(
                  filter.getNative(),
                  fromFilterString(FilterId.DN_SUBTREE_MATCH, otherFilter.toFilterString())
                      .getNative()));
    } catch (LdapException e) {
      ZimbraLog.ldap.warn("filter error", e);
      assert (false);
    }
    return andedFilter;
  }

  public ZLdapFilter negate(ZLdapFilter filter) {
    return new ZLdapFilter(
        filter.getFilterId(), Filter.createNOTFilter(filter.getNative()));
  }

  /*
   * Mail target (accounts and groups)
   */
  public ZLdapFilter addrsExist(String[] addrs) {
    List<Filter> filters = Lists.newArrayList();
    for (String addr : addrs) {
      filters.add(Filter.createEqualityFilter(Provisioning.A_zimbraMailDeliveryAddress, addr));
      filters.add(Filter.createEqualityFilter(Provisioning.A_zimbraMailAlias, addr));
    }

    return new ZLdapFilter(FilterId.ADDRS_EXIST, Filter.createORFilter(filters));
  }

  /*
   * account
   */
  public ZLdapFilter allAccounts() {
    return new ZLdapFilter(FilterId.ALL_ACCOUNTS, FILTER_ALL_ACCOUNTS);
  }

  public ZLdapFilter allAccountsOnly() {
    return new ZLdapFilter(FilterId.ALL_ACCOUNTS_ONLY, FILTER_ALL_ACCOUNTS_ONLY);
  }

  public ZLdapFilter allAccountsOnlyByCos(String cosId) {
    return new ZLdapFilter(
        FilterId.ALL_ACCOUNTS_ONLY_BY_COS,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY,
            Filter.createORFilter(
                Filter.createANDFilter(
                    Filter.createNOTFilter(FILTER_IS_EXTERNAL_ACCOUNT),
                    Filter.createNOTFilter(
                        Filter.createPresenceFilter(Provisioning.A_zimbraCOSId))),
                Filter.createEqualityFilter(Provisioning.A_zimbraCOSId, cosId))));
  }

  public ZLdapFilter allAdminAccounts() {
    return new ZLdapFilter(FilterId.ALL_ADMIN_ACCOUNTS, FILTER_ALL_ADMIN_ACCOUNTS);
  }

  public ZLdapFilter allNonSystemAccounts() {
    return new ZLdapFilter(FilterId.ALL_NON_SYSTEM_ACCOUNTS, FILTER_ALL_NON_SYSTEM_ACCOUNTS);
  }

  public ZLdapFilter allNonSystemArchivingAccounts() {
    return new ZLdapFilter(
        FilterId.ALL_NON_SYSTEM_ARCHIVING_ACCOUNTS, FILTER_ALL_NON_SYSTEM_ARCHIVING_ACCOUNTS);
  }

  public ZLdapFilter allNonSystemInternalAccounts() {
    return new ZLdapFilter(
        FilterId.ALL_NON_SYSTEM_INTERNAL_ACCOUNTS, FILTER_ALL_NON_SYSTEM_INTERNAL_ACCOUNTS);
  }

  public ZLdapFilter accountByForeignPrincipal(String foreignPrincipal) {
    return new ZLdapFilter(
        FilterId.ACCOUNT_BY_FOREIGN_PRINCIPAL,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraForeignPrincipal, foreignPrincipal),
            FILTER_ALL_ACCOUNTS));
  }

  public ZLdapFilter accountById(String id) {
    return new ZLdapFilter(
        FilterId.ACCOUNT_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_ACCOUNTS));
  }

  public ZLdapFilter accountByMemberOf(String dynGroupId) {
    return new ZLdapFilter(
        FilterId.ACCOUNT_BY_MEMBEROF,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraMemberOf, dynGroupId),
            FILTER_ALL_INTERNAL_ACCOUNTS));
  }

  public ZLdapFilter accountByName(String name) {
    return new ZLdapFilter(
        FilterId.ACCOUNT_BY_NAME,
        Filter.createANDFilter(
            Filter.createORFilter(
                Filter.createEqualityFilter(Provisioning.A_zimbraMailDeliveryAddress, name),
                Filter.createEqualityFilter(Provisioning.A_zimbraMailAlias, name),
                Filter.createEqualityFilter(Provisioning.A_zimbraOldMailAddress, name)),
            FILTER_ALL_ACCOUNTS));
  }

  public ZLdapFilter adminAccountByRDN(String namingRdnAttr, String name) {
    return new ZLdapFilter(
        FilterId.ADMIN_ACCOUNT_BY_RDN,
        Filter.createANDFilter(
            Filter.createEqualityFilter(namingRdnAttr, name), FILTER_ALL_ACCOUNTS));
  }

  public ZLdapFilter accountsHomedOnServer(String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.ACCOUNTS_HOMED_ON_SERVER,
        Filter.createANDFilter(FILTER_ALL_ACCOUNTS, homedOnServerFilter(serverServiceHostname)));
  }

  public ZLdapFilter accountsHomedOnServerAccountsOnly(String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.ACCOUNTS_HOMED_ON_SERVER_ACCOUNTS_ONLY,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY, homedOnServerFilter(serverServiceHostname)));
  }

  public ZLdapFilter homedOnServer(String serverServiceHostname) {
    return new ZLdapFilter(FilterId.HOMED_ON_SERVER, homedOnServerFilter(serverServiceHostname));
  }

  public ZLdapFilter accountsOnServerAndCosHasSubordinates(
      String serverServiceHostname, String cosId) {
    return new ZLdapFilter(
        FilterId.ACCOUNTS_ON_SERVER_AND_COS_HAS_SUBORDINATES,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS,
            homedOnServerFilter(serverServiceHostname),
            FILTER_HAS_SUBORDINATES,
            Filter.createORFilter(
                Filter.createNOTFilter(Filter.createPresenceFilter(Provisioning.A_zimbraCOSId)),
                Filter.createEqualityFilter(Provisioning.A_zimbraCOSId, cosId))));
  }

  public ZLdapFilter externalAccountsHomedOnServer(String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.EXTERNAL_ACCOUNTS_HOMED_ON_SERVER,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY,
            FILTER_IS_EXTERNAL_ACCOUNT,
            homedOnServerFilter(serverServiceHostname)));
  }

  public ZLdapFilter accountsByGrants(
      List<String> granteeIds, boolean includePublicShares, boolean includeAllAuthedShares) {

    List<Filter> filters = Lists.newArrayList();
    for (String granteeId : granteeIds) {
      filters.add(
          Filter.createSubstringFilter(
              Provisioning.A_zimbraSharedItem, "granteeId:" + granteeId, null, null));
    }

    if (includePublicShares) {
      filters.add(FILTER_PUBLIC_SHARE);
    }

    if (includeAllAuthedShares) {
      filters.add(FILTER_ALLAUTHED_SHARE);
    }

    return new ZLdapFilter(
        FilterId.ACCOUNTS_BY_GRANTS,
        Filter.createANDFilter(FILTER_ALL_ACCOUNTS, Filter.createORFilter(filters)));
  }

  public ZLdapFilter CMBSearchAccountsOnly() {
    return new ZLdapFilter(
        FilterId.CMB_SEARCH_ACCOUNTS_ONLY,
        Filter.createANDFilter(FILTER_ALL_ACCOUNTS_ONLY, FILTER_NOT_EXCLUDED_FROM_CMB_SEARCH));
  }

  public ZLdapFilter CMBSearchAccountsOnlyWithArchive() {
    return new ZLdapFilter(
        FilterId.CMB_SEARCH_ACCOUNTS_ONLY_WITH_ARCHIVE,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY, FILTER_WITH_ARCHIVE, FILTER_NOT_EXCLUDED_FROM_CMB_SEARCH));
  }

  public ZLdapFilter CMBSearchNonSystemResourceAccountsOnly() {
    return new ZLdapFilter(
        FilterId.CMB_SEARCH_NON_SYSTEM_RESOURCE_ACCOUNTS_ONLY,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY,
            FILTER_NOT_SYSTEM_RESOURCE,
            FILTER_NOT_EXCLUDED_FROM_CMB_SEARCH));
  }

  /*
   * alias
   */
  public ZLdapFilter allAliases() {
    return new ZLdapFilter(FilterId.ALL_ALIASES, FILTER_ALL_ALIASES);
  }

  /*
   * calendar resource
   */
  public ZLdapFilter allCalendarResources() {
    return new ZLdapFilter(FilterId.ALL_CALENDAR_RESOURCES, FILTER_ALL_CALENDAR_RESOURCES);
  }

  public ZLdapFilter calendarResourceByForeignPrincipal(String foreignPrincipal) {
    return new ZLdapFilter(
        FilterId.CALENDAR_RESOURCE_BY_FOREIGN_PRINCIPAL,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraForeignPrincipal, foreignPrincipal),
            FILTER_ALL_CALENDAR_RESOURCES));
  }

  public ZLdapFilter calendarResourceById(String id) {
    return new ZLdapFilter(
        FilterId.CALENDAR_RESOURCE_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id),
            FILTER_ALL_CALENDAR_RESOURCES));
  }

  public ZLdapFilter calendarResourceByName(String name) {
    return new ZLdapFilter(
        FilterId.CALENDAR_RESOURCE_BY_NAME,
        Filter.createANDFilter(
            Filter.createORFilter(
                Filter.createEqualityFilter(Provisioning.A_zimbraMailDeliveryAddress, name),
                Filter.createEqualityFilter(Provisioning.A_zimbraMailAlias, name)),
            FILTER_ALL_CALENDAR_RESOURCES));
  }

  public ZLdapFilter calendarResourcesHomedOnServer(String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.CALENDAR_RESOURCES_HOMED_ON_SERVER,
        Filter.createANDFilter(
            FILTER_ALL_CALENDAR_RESOURCES, homedOnServerFilter(serverServiceHostname)));
  }

  /*
   * cos
   */
  public ZLdapFilter allCoses() {
    return new ZLdapFilter(FilterId.ALL_COSES, FILTER_ALL_COSES);
  }

  public ZLdapFilter cosById(String id) {
    return new ZLdapFilter(
        FilterId.COS_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_COSES));
  }

  public ZLdapFilter cosesByMailHostPool(String serverId) {
    return new ZLdapFilter(
        FilterId.COSES_BY_MAILHOST_POOL,
        Filter.createANDFilter(
            FILTER_ALL_COSES,
            Filter.createEqualityFilter(Provisioning.A_zimbraMailHostPool, serverId)));
  }

  /*
   * data source
   */
  public ZLdapFilter allDataSources() {
    return new ZLdapFilter(FilterId.ALL_DATA_SOURCES, FILTER_ALL_DATASOURCES);
  }

  public ZLdapFilter dataSourceById(String id) {
    return new ZLdapFilter(
        FilterId.DATA_SOURCE_BY_ID,
        Filter.createANDFilter(
            FILTER_ALL_DATASOURCES,
            Filter.createEqualityFilter(Provisioning.A_zimbraDataSourceId, id)));
  }

  public ZLdapFilter dataSourceByName(String name) {
    return new ZLdapFilter(
        FilterId.DATA_SOURCE_BY_NAME,
        Filter.createANDFilter(
            FILTER_ALL_DATASOURCES,
            Filter.createEqualityFilter(Provisioning.A_zimbraDataSourceName, name)));
  }

  /*
   * distribution list
   */
  public ZLdapFilter allDistributionLists() {
    return new ZLdapFilter(FilterId.ALL_DISTRIBUTION_LISTS, FILTER_ALL_DISTRIBUTION_LISTS);
  }

  public ZLdapFilter distributionListById(String id) {
    return new ZLdapFilter(
        FilterId.DISTRIBUTION_LIST_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id),
            FILTER_ALL_DISTRIBUTION_LISTS));
  }

  public ZLdapFilter distributionListByName(String name) {
    return new ZLdapFilter(
        FilterId.DISTRIBUTION_LIST_BY_NAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraMailAlias, name),
            FILTER_ALL_DISTRIBUTION_LISTS));
  }

  public ZLdapFilter distributionListsByMemberAddrs(String[] memberAddrs) {
    List<Filter> filters = Lists.newArrayList();
    for (String memberAddr : memberAddrs) {
      filters.add(
          Filter.createEqualityFilter(Provisioning.A_zimbraMailForwardingAddress, memberAddr));
    }

    return new ZLdapFilter(
        FilterId.DISTRIBUTION_LISTS_BY_MEMBER_ADDRS,
        Filter.createANDFilter(FILTER_ALL_DISTRIBUTION_LISTS, Filter.createORFilter(filters)));
  }

  /*
   * dynamic group
   */
  public ZLdapFilter allDynamicGroups() {
    return new ZLdapFilter(FilterId.ALL_DYNAMIC_GROUPS, FILTER_ALL_DYNAMIC_GROUPS);
  }

  public ZLdapFilter dynamicGroupById(String id) {
    return new ZLdapFilter(
        FilterId.DYNAMIC_GROUP_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_DYNAMIC_GROUPS));
  }

  public ZLdapFilter dynamicGroupByIds(String[] ids) {
    List<Filter> filters = Lists.newArrayList();
    for (String id : ids) {
      filters.add(Filter.createEqualityFilter(Provisioning.A_zimbraId, id));
    }
    return new ZLdapFilter(
        FilterId.DYNAMIC_GROUP_BY_IDS,
        Filter.createANDFilter(
            FILTER_ALL_DYNAMIC_GROUPS, Filter.createORFilter(Filter.createORFilter(filters))));
  }

  public ZLdapFilter dynamicGroupByName(String name) {
    return new ZLdapFilter(
        FilterId.DYNAMIC_GROUP_BY_NAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraMailAlias, name),
            FILTER_ALL_DYNAMIC_GROUPS));
  }

  public ZLdapFilter dynamicGroupDynamicUnitByMailAddr(String mailAddr) {
    return new ZLdapFilter(
        FilterId.DYNAMIC_GROUP_DYNAMIC_UNIT_BY_MAIL_ADDR,
        Filter.createANDFilter(
            FILTER_ALL_DYNAMIC_GROUP_DYNAMIC_UNITS,
            Filter.createEqualityFilter(Provisioning.A_mail, mailAddr)));
  }

  public ZLdapFilter dynamicGroupsStaticUnitByMemberAddr(String memberAddr) {
    return new ZLdapFilter(
        FilterId.DYNAMIC_GROUPS_STATIC_UNIT_BY_MEMBER_ADDR,
        Filter.createANDFilter(
            FILTER_ALL_DYNAMIC_GROUP_STATIC_UNITS,
            Filter.createEqualityFilter(Provisioning.A_zimbraMailForwardingAddress, memberAddr)));
  }

  /*
   * group (distribution list or dynamic group)
   */
  public ZLdapFilter allGroups() {
    return new ZLdapFilter(FilterId.ALL_GROUPS, FILTER_ALL_GROUPS);
  }

  public ZLdapFilter groupById(String id) {
    return new ZLdapFilter(
        FilterId.GROUP_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_GROUPS));
  }

  public ZLdapFilter groupByName(String name) {
    return new ZLdapFilter(
        FilterId.GROUP_BY_NAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraMailAlias, name), FILTER_ALL_GROUPS));
  }

  /*
   * domain
   */
  public ZLdapFilter allDomains() {
    return new ZLdapFilter(FilterId.ALL_DOMAINS, FILTER_ALL_DOMAINS);
  }

  public ZLdapFilter domainAliases(String id) {
    return new ZLdapFilter(
        FilterId.DOMAIN_ALIASES,
        Filter.createANDFilter(
            Filter.createEqualityFilter(
                Provisioning.A_zimbraDomainAliasTargetId,
                id),
            FILTER_ALL_DOMAINS,
            Filter.createEqualityFilter(
                Provisioning.A_zimbraDomainType, Provisioning.DomainType.alias.name())));
  }

  public ZLdapFilter domainById(String id) {
    return new ZLdapFilter(
        FilterId.DOMAIN_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_DOMAINS));
  }

  public ZLdapFilter domainsByIds(Collection<String> ids) {
    List<Filter> filters = Lists.newArrayList();
    for (String id : ids) {
      filters.add(Filter.createEqualityFilter(Provisioning.A_zimbraId, id));
    }
    return new ZLdapFilter(
        FilterId.DOMAINS_BY_IDS,
        Filter.createANDFilter(
            FILTER_ALL_DOMAINS, Filter.createORFilter(Filter.createORFilter(filters))));
  }

  public ZLdapFilter domainByName(String name) {
    return new ZLdapFilter(
        FilterId.DOMAIN_BY_NAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraDomainName, name),
            FILTER_ALL_DOMAINS));
  }

  public ZLdapFilter domainByKrb5Realm(String krb5Realm) {
    return new ZLdapFilter(
        FilterId.DOMAIN_BY_KRB5_REALM,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraAuthKerberos5Realm, krb5Realm),
            FILTER_ALL_DOMAINS));
  }

  public ZLdapFilter domainByVirtualHostame(String virtualHostname) {
    return new ZLdapFilter(
        FilterId.DOMAIN_BY_VIRTUAL_HOSTNAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraVirtualHostname, virtualHostname),
            FILTER_ALL_DOMAINS));
  }

  public ZLdapFilter domainByForeignName(String foreignName) {
    return new ZLdapFilter(
        FilterId.DOMAIN_BY_FOREIGN_NAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraForeignName, foreignName),
            FILTER_ALL_DOMAINS));
  }

  public ZLdapFilter domainLabel() {
    return new ZLdapFilter(FilterId.DOMAIN_LABEL, FILTER_DOMAIN_LABEL);
  }

  public ZLdapFilter domainLockedForEagerAutoProvision() {
    return new ZLdapFilter(
        FilterId.DOMAIN_LOCKED_FOR_AUTO_PROVISION,
        Filter.createNOTFilter(Filter.createPresenceFilter(Provisioning.A_zimbraAutoProvLock)));
  }

  /*
   * global config
   */
  public ZLdapFilter globalConfig() {
    return new ZLdapFilter(
        FilterId.GLOBAL_CONFIG, Filter.createEqualityFilter(Provisioning.A_cn, "config"));
  }

  /*
   * identity
   */
  public ZLdapFilter allIdentities() {
    return new ZLdapFilter(FilterId.ALL_IDENTITIES, FILTER_ALL_IDENTITIES);
  }

  public ZLdapFilter identityByName(String name) {
    return new ZLdapFilter(
        FilterId.IDENTITY_BY_NAME,
        Filter.createANDFilter(
            FILTER_ALL_IDENTITIES,
            Filter.createEqualityFilter(Provisioning.A_zimbraPrefIdentityName, name)));
  }

  /*
   * mime entry
   */
  public ZLdapFilter allMimeEntries() {
    return new ZLdapFilter(FilterId.ALL_MIME_ENTRIES, FILTER_ALL_MIME_ENTRIES);
  }

  public ZLdapFilter mimeEntryByMimeType(String mimeType) {
    return new ZLdapFilter(
        FilterId.MIME_ENTRY_BY_MIME_TYPE,
        Filter.createEqualityFilter(Provisioning.A_zimbraMimeType, mimeType));
  }

  /*
   * server
   */
  public ZLdapFilter allServers() {
    return new ZLdapFilter(FilterId.ALL_SERVERS, FILTER_ALL_SERVERS);
  }

  public ZLdapFilter serverById(String id) {
    return new ZLdapFilter(
        FilterId.SERVER_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_SERVERS));
  }

  public ZLdapFilter serverByService(String service) {
    return new ZLdapFilter(
        FilterId.SERVER_BY_SERVICE,
        Filter.createANDFilter(
            FILTER_ALL_SERVERS,
            Filter.createEqualityFilter(Provisioning.A_zimbraServiceEnabled, service)));
  }

  /*
   * share locator
   */
  public ZLdapFilter shareLocatorById(String id) {
    return new ZLdapFilter(
        FilterId.SHARE_LOCATOR_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_cn, id), FILTER_ALL_SHARE_LOCATORS));
  }

  /*
   * signature
   */
  public ZLdapFilter allSignatures() {
    return new ZLdapFilter(FilterId.ALL_SIGNATURES, FILTER_ALL_SIGNATURES);
  }

  public ZLdapFilter signatureById(String id) {
    return new ZLdapFilter(
        FilterId.SIGNATURE_BY_ID,
        Filter.createANDFilter(
            FILTER_ALL_SIGNATURES,
            Filter.createEqualityFilter(Provisioning.A_zimbraSignatureId, id)));
  }

  /*
   * zimlet
   */
  public ZLdapFilter allZimlets() {
    return new ZLdapFilter(FilterId.ALL_ZIMLETS, FILTER_ALL_ZIMLETS);
  }

  /*
   * AD
   */
  public ZLdapFilter memberOf(String dnOfGroup) {
    return new ZLdapFilter(
        FilterId.MEMBER_OF, Filter.createEqualityFilter(LdapConstants.ATTR_memberOf, dnOfGroup));
  }

  /*
   * Velodrome
   */
  private Filter velodromePrimaryEmailOnDomainFilter(String domainName) {
    return Filter.createSubstringFilter(
        Provisioning.A_zimbraMailDeliveryAddress, null, null, "@" + domainName);
  }

  private Filter velodromeMailOrZimbraMailAliasOnDomainFilter(String domainName) {
    return Filter.createORFilter(
        Filter.createSubstringFilter(Provisioning.A_mail, null, null, "@" + domainName),
        Filter.createSubstringFilter(Provisioning.A_zimbraMailAlias, null, null, "@" + domainName));
  }

  public ZLdapFilter velodromeAllAccountsByDomain(String domainName) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_ACCOUNTS_BY_DOMAIN,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS, velodromePrimaryEmailOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllAccountsOnlyByDomain(String domainName) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_ACCOUNTS_ONLY_BY_DOMAIN,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY, velodromePrimaryEmailOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllCalendarResourcesByDomain(String domainName) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_CALENDAR_RESOURCES_BY_DOMAIN,
        Filter.createANDFilter(
            FILTER_ALL_CALENDAR_RESOURCES, velodromePrimaryEmailOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllAccountsByDomainAndServer(
      String domainName, String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_ACCOUNTS_BY_DOMAIN_AND_SERVER,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS,
            homedOnServerFilter(serverServiceHostname),
            velodromePrimaryEmailOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllAccountsOnlyByDomainAndServer(
      String domainName, String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_ACCOUNTS_ONLY_BY_DOMAIN_AND_SERVER,
        Filter.createANDFilter(
            FILTER_ALL_ACCOUNTS_ONLY,
            homedOnServerFilter(serverServiceHostname),
            velodromePrimaryEmailOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllCalendarResourcesByDomainAndServer(
      String domainName, String serverServiceHostname) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_CALENDAR_RESOURCES_BY_DOMAIN_AND_SERVER,
        Filter.createANDFilter(
            FILTER_ALL_CALENDAR_RESOURCES,
            homedOnServerFilter(serverServiceHostname),
            velodromePrimaryEmailOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllDistributionListsByDomain(String domainName) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_DISTRIBUTION_LISTS_BY_DOMAIN,
        Filter.createANDFilter(
            FILTER_ALL_DISTRIBUTION_LISTS,
            velodromeMailOrZimbraMailAliasOnDomainFilter(domainName)));
  }

  public ZLdapFilter velodromeAllGroupsByDomain(String domainName) {
    return new ZLdapFilter(
        FilterId.VELODROME_ALL_GROUPS_BY_DOMAIN,
        Filter.createANDFilter(
            FILTER_ALL_GROUPS, velodromeMailOrZimbraMailAliasOnDomainFilter(domainName)));
  }

  public ZLdapFilter dnSubtreeMatch(String... dns) {
    List<Filter> filters = Lists.newArrayList();
    for (String dn : dns) {
      filters.add(
          Filter.createExtensibleMatchFilter(
              LdapConstants.DN_SUBTREE_MATCH_ATTR,
              LdapConstants.DN_SUBTREE_MATCH_MATCHING_RULE,
              false,
              dn));
    }

    return new ZLdapFilter(FilterId.DN_SUBTREE_MATCH, Filter.createORFilter(filters));
  }

  /*
   * address lists
   */
  public ZLdapFilter allAddressLists() {
    return new ZLdapFilter(FilterId.ALL_ADDRESS_LISTS, FILTER_ALL_ADDRESS_LISTS);
  }

  public ZLdapFilter addressListById(String id) {
    return new ZLdapFilter(
        FilterId.ADDRESS_LIST_BY_ID,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_zimbraId, id), FILTER_ALL_ADDRESS_LISTS));
  }

  public ZLdapFilter addressListByName(String name) {
    return new ZLdapFilter(
        FilterId.ADDRESS_LIST_BY_NAME,
        Filter.createANDFilter(
            Filter.createEqualityFilter(Provisioning.A_uid, name), FILTER_ALL_ADDRESS_LISTS));
  }
}
