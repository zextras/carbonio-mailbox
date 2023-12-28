// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.*;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.SearchLdapOptions.SearchLdapVisitor;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.soap.admin.type.CountObjectsType;
import java.util.*;

/**
 * Validators class consists of {@link DomainAccountValidator} and {@link DomainMaxAccountsValidator}.
 */
public final class Validators {

  private Validators() {}

  private static String getDomainName(String emailAddress) {
    String domainName = null;
    int index = emailAddress.indexOf('@');
    if (index != -1){
      domainName = emailAddress.substring(index + 1);
    }
    return domainName;
  }

  private static boolean isSystemProperty(Map<String, Object> attrs) {
    if (attrs == null) {
      return false;
    }

    Object isSystemResource = attrs.get(Provisioning.A_zimbraIsSystemResource);
    if (isSystemResource != null && "true".equalsIgnoreCase(isSystemResource.toString())) {
      return true; // is system resource, do not check
    }

    // if we are restoring, the OC array would be empty and
    // all object classes will be in the attr map.
    // Skip license check if we are restoring a calendar resource
    isSystemResource = attrs.get(Provisioning.A_objectClass);
    if (isSystemResource instanceof String[]) {
      Set<String> objectClasses = new HashSet<>(Arrays.asList((String[]) isSystemResource));
      return objectClasses.contains(AttributeClass.OC_zimbraCalendarResource);
    }

    return false;
  }

  private static boolean isExternalVirtualAccount(Map<String, Object> attrs) {
    if (attrs == null) {
      return false;
    }
    Object isExternalVirtualAccount = attrs.get(Provisioning.A_zimbraIsExternalVirtualAccount);
    return isExternalVirtualAccount != null && "true".equalsIgnoreCase(isExternalVirtualAccount.toString());
  }

  /**
   * DomainAccountValidator validates maximum number of accounts allowed in a domain.
   * Caches the result for 1 min ({@value #LDAP_CHECK_INTERVAL}) unless the count is within
   * 5 ({@value #NUM_ACCT_THRESHOLD) of the limit.
   */
  public static class DomainAccountValidator implements Provisioning.ProvisioningValidator {
    private static final long LDAP_CHECK_INTERVAL = 60 * 1000;
    private static final long NUM_ACCT_THRESHOLD = 5;

    private long mNextCheck;
    private long mLastUserCount;

    @Override
    public void refresh() {
      setNextCheck(0);
    }

    private synchronized void setNextCheck(long nextCheck) {
      mNextCheck = nextCheck;
    }

    private synchronized long getNextCheck() {
      return mNextCheck;
    }

    /**
     * Validates maximum number of accounts allowed in a domain for the actions
     * {@value #CREATE_ACCOUNT} and {@value #RENAME_ACCOUNT}.
     *
     * @param prov {@link Provisioning}
     * @param action provided action
     * @param args consists of {@link Provisioning.ProvisioningValidator}, email address,
     *            String[] additionalObjectClasses, Map<String, Object> origAttrs
     * @throws ServiceException in case if LDAP is not responding or is responding slowly;
     * unable to count users for setting zimbraDomainMaxAccounts; the limit of maximum number of
     * accounts is reached.
     *
     */
    @Override
    public void validate(Provisioning prov, String action, Object... args) throws ServiceException {
      if (args.length < 1) {
        return;
      }
      if (!(action.equals(CREATE_ACCOUNT) || action.equals(RENAME_ACCOUNT))
          || !(args[0] instanceof String)) {
        return;
      }

      if (args.length > 1
          && args[1] instanceof String[]
          && Arrays.asList((String[]) args[1]).contains(AttributeClass.OC_zimbraCalendarResource)) {
        return; // as in LicenseManager, don't want to count calendar resources
      }

      if (args.length > 2 && args[2] instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> acctAttrs = (Map<String, Object>) args[2];
        if (isSystemProperty(acctAttrs)) {
          return;
        }
        if (isExternalVirtualAccount(acctAttrs)) {
          return;
        }
      }

      String emailAddress = (String) args[0];

      String domainName = getDomainName(emailAddress);
      if (domainName == null) {
        return;
      }

      Domain domain = prov.get(Key.DomainBy.name, domainName);
      if (domain == null) {
        return;
      }

      String domainMaxAccountLimit = domain.getAttr(Provisioning.A_zimbraDomainMaxAccounts);
      if (domainMaxAccountLimit == null || "0".equals(domainMaxAccountLimit)) {
        return;
      }

      long maxAccountLimit = Long.parseLong(domainMaxAccountLimit);
      long now = System.currentTimeMillis();
      if (now > getNextCheck()) {
        try {
          mLastUserCount = prov.countObjects(CountObjectsType.internalUserAccount, domain);
        } catch (ServiceException e) {
          if (e.getCause() != null
              && e.getCause().getMessage() != null
              && e.getCause().getMessage().contains("timeout")) {
            throw ServiceException.FAILURE(
                "The directory may not be responding or is responding slowly.  The directory may"
                    + " need tuning or the LDAP read timeout may need to be raised.  Otherwise,"
                    + " removing the zimbraDomainMaxAccounts restriction will avoid this check.",
                e);
          } else {
            throw ServiceException.FAILURE(
                "Unable to count users for setting zimbraDomainMaxAccounts="
                    + domainMaxAccountLimit
                    + " in domain "
                    + domain.getName(),
                e);
          }
        }
        long nextCheck =
            (maxAccountLimit - mLastUserCount) > NUM_ACCT_THRESHOLD ? LDAP_CHECK_INTERVAL : 0;
        setNextCheck(nextCheck);
      }

      if (maxAccountLimit <= mLastUserCount)
        throw AccountServiceException.TOO_MANY_ACCOUNTS(
            "domain=" + domainName + " (" + maxAccountLimit + ")");
    }
  }

  /**
   * DomainMaxAccountsValidator validates that we are not exceeding max feature and cos counts
   * for the given domain.
   *
   * @author pfnguyen
   */
  public static class DomainMaxAccountsValidator implements Provisioning.ProvisioningValidator {
    @Override
    public void refresh() {
      // do nothing
    }

    /**
     * Validates zimbraDomainCOSMaxAccounts(maximum number of accounts allowed to be assigned to
     * specified COSes in a domain) and zimbraDomainFeatureMaxAccounts (maximum number of accounts
     * allowed to have specified features in a domain).
     * Possible actions are {@value #CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE},
     * {@value #RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE},
     * {@value #MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE}.
     *
     * @param prov {@link Provisioning}
     * @param action provided action
     * @param args consists of {@link Provisioning.ProvisioningValidator}, email address,
     *             String[] additionalObjectClasses, Map<String, Object> origAttrs and
     *             {@link Account} (in case of {@value #MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE}
     * @throws ServiceException when action {@value #MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE}
     * but account is not provided; the limit of maximum number of features or maximum number of
     * cos accounts is reached for the given domain.
     */
    @Override
    public void validate(Provisioning prov, String action, Object... args) throws ServiceException {

      if (!CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE.equals(action)
          && !RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE.equals(action)
          && !MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE.equals(action)) {
        return;
      }

      if (args.length < 2) {
        return;
      }

      HashMap<String, Integer> cosCountMap = new HashMap<>();
      HashMap<String, Integer> cosLimitMap = new HashMap<>();
      HashMap<String, Integer> featureCountMap = new HashMap<>();
      HashMap<String, Integer> featureLimitMap = new HashMap<>();
      HashMap<String, Set<String>> cosFeatureMap = new HashMap<>();

      String emailAddress = (String) args[0];
      if (emailAddress == null) {
        return;
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> attrs = (Map<String, Object>) args[1];
      if (isSystemProperty(attrs)) {
        return;
      }
      if (isExternalVirtualAccount(attrs)) {
        return;
      }

      Account account = null;
      if (args.length == 3) {
        account = (Account) args[2];
      }

      String domainName = getDomainName(emailAddress);
      if (domainName == null) {
        return;
      }

      Domain domain = prov.get(Key.DomainBy.name, domainName);
      if (domain == null) {
        return;
      }

      String defaultCosId = domain.getAttr(Provisioning.A_zimbraDomainDefaultCOSId);
      if (defaultCosId == null) {
        Cos defaultCos = prov.get(Key.CosBy.name, Provisioning.DEFAULT_COS_NAME);
        if (defaultCos != null) {
          defaultCosId = defaultCos.getId();
        }
      }

      Set<String> cosLimit = domain.getMultiAttrSet(Provisioning.A_zimbraDomainCOSMaxAccounts);
      Set<String> featureLimit =
          domain.getMultiAttrSet(Provisioning.A_zimbraDomainFeatureMaxAccounts);

      if (cosLimit.isEmpty() && featureLimit.isEmpty()) {
        return;
      }

      for (String limit : cosLimit) {
        parseLimit(cosLimitMap, limit);
      }
      for (String limit : featureLimit) {
        parseLimit(featureLimitMap, limit);
      }

      // populate count maps with the cos and features we are interested in
      for (Map.Entry<String, Integer> e : cosLimitMap.entrySet()) {
        cosCountMap.put(e.getKey(), 0);
      }
      for (Map.Entry<String, Integer> e : featureLimitMap.entrySet()){
        featureCountMap.put(e.getKey(), 0);
      }

      boolean isModifyingCosId = (attrs != null && attrs.get(Provisioning.A_zimbraCOSId) != null);
      boolean isCreatingEntry = CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE.equals(action);
      boolean isRenamingEntry = RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE.equals(action);

      String desiredCosId = null;

      if (isModifyingCosId || isCreatingEntry || isRenamingEntry) {
        if (attrs != null) {
          desiredCosId = (String) attrs.get(Provisioning.A_zimbraCOSId);
        }
        if (desiredCosId == null) {
          desiredCosId = defaultCosId;
        }
      } else {
        // action is modify, but we are not modifying cos, so account must not be null
        if (account != null) {
          desiredCosId = account.getCOS().getId();
        } else {
          throw ServiceException.FAILURE("account object is null", null);
        }
      }

      Set<String> cosFeatures = getCosFeatures(prov, cosFeatureMap, desiredCosId, defaultCosId);
      Set<String> desiredFeatures = new HashSet<>();
      // add all new requested features
      if (attrs != null) {
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
          String k = entry.getKey();
          if (featureLimitMap.containsKey(k)
              && "true".equalsIgnoreCase(entry.getValue().toString())) {
            desiredFeatures.add(k);
          }
        }
      }
      // add all features in new cos
      if (cosFeatures != null) {
        for (String feature : cosFeatures) {
          if (featureLimitMap.containsKey(feature)) {
            desiredFeatures.add(feature);
          }
        }
      }
      if (ZimbraLog.account.isDebugEnabled()) {
        ZimbraLog.account.debug(
            "Desired features (incl. cos): %s + %s", desiredFeatures, cosFeatures);
      }
      String originalCosId = null;
      // remove all features in old cos
      if (account != null) {
        originalCosId = account.getAttr(Provisioning.A_zimbraCOSId);
        // be sure to fall back to default cos ID if none is set
        // spurious counts will occur otherwise
        if (originalCosId == null) {
          originalCosId = defaultCosId;
        }
        Set<String> features = getCosFeatures(prov, cosFeatureMap, originalCosId, defaultCosId);
        if (features != null) {
          desiredFeatures.removeAll(features);
        }
      }
      // remove all features in old account
      if (!desiredFeatures.isEmpty()) {
        if (account != null) {
          Map<String, Object> acctAttrs = account.getAttrs(false);
          desiredFeatures.removeIf(feature -> acctAttrs.containsKey(feature)
              && "true".equalsIgnoreCase(acctAttrs.get(feature).toString()));
        }
      }
      if ((desiredCosId != null
              && !desiredCosId.equals(originalCosId)
              && cosLimitMap.containsKey(desiredCosId))
          || !desiredFeatures.isEmpty()) {
        if (ZimbraLog.account.isDebugEnabled()) {
          ZimbraLog.account.debug(
              "COS change info [%s:%s], desired features %s",
              originalCosId, desiredCosId, desiredFeatures);
        }

        buildDomainCounts(
            prov, domainName, defaultCosId, cosCountMap, featureCountMap, cosFeatureMap);

        if (ZimbraLog.account.isDebugEnabled()) {
          ZimbraLog.account.debug("COS/Feature limits: %s + %s", cosLimitMap, featureLimitMap);
        }
        if (desiredCosId != null
            && !desiredCosId.equals(originalCosId)
            && cosLimitMap.containsKey(desiredCosId)) {
          if (cosCountMap.containsKey(desiredCosId)
              && cosCountMap.get(desiredCosId) >= cosLimitMap.get(desiredCosId)) {
            throw AccountServiceException.TOO_MANY_ACCOUNTS(
                String.format(
                    "domain=%s[cos=%s,count=%d,limit=%d]",
                    domainName,
                    desiredCosId,
                    cosCountMap.get(desiredCosId),
                    cosLimitMap.get(desiredCosId)));
          }
        }
        if (!desiredFeatures.isEmpty()) {
          for (String feature : desiredFeatures) {
            if (featureCountMap.containsKey(feature)
                && featureCountMap.get(feature) >= featureLimitMap.get(feature)) {
              throw AccountServiceException.TOO_MANY_ACCOUNTS(
                  String.format(
                      "domain=%s[%s,count=%d,limit=%d]",
                      domainName,
                      feature,
                      featureCountMap.get(feature),
                      featureLimitMap.get(feature)));
            }
          }
        }
      }
    }

    private static Set<String> getCosFeatures(
        Provisioning prov,
        Map<String, Set<String>> cosFeatureMap,
        String cosId,
        String defaultCosId)
        throws ServiceException {
      if (!cosFeatureMap.containsKey(cosId)) {
        Cos cos = null;
        if (cosId != null){
          cos = prov.get(Key.CosBy.id, cosId);
        }
        if (cos == null) {
          if (defaultCosId != null) {
            ZimbraLog.account.debug("COS id %s not found, reverting to %s", cosId, defaultCosId);
            return getCosFeatures(prov, cosFeatureMap, defaultCosId, null);
          } else {
            ZimbraLog.account.debug("COS %s not found, bailing!", cosId);
            return null;
          }
        }
        Map<String, Object> cosAttrs = cos.getAttrs(true);
        Set<String> features = new HashSet<>();
        for (Map.Entry<String, Object> entry : cosAttrs.entrySet()) {
          String name = entry.getKey();
          if (name.toLowerCase().startsWith("zimbrafeature")
              && name.toLowerCase().endsWith("enabled")) {
            Object value = entry.getValue();
            if (value != null && "true".equalsIgnoreCase(value.toString())) {
              features.add(name);
            }
          }
        }
        cosFeatureMap.put(cosId, features);
      }
      return cosFeatureMap.get(cosId);
    }

    private static void parseLimit(HashMap<String, Integer> map, String limit) {
      String[] parts = limit.split(":");
      int max = -1;
      try {
        max = Integer.parseInt(parts[1]);
      } catch (NumberFormatException e) {
        ZimbraLog.account.debug("can not parse limit value for " + parts[1]);
      }
      if (max < 0) {
        return;
      }
      map.put(parts[0], max);
    }

    private static class BuildDomainCounts extends SearchLdapVisitor {

      private final Provisioning prov;
      private final String domain;
      private final String defaultCos;
      private final Map<String, Integer> cosCount;
      private final Map<String, Integer> featureCount;
      private final Map<String, Set<String>> cosFeatureMap;

      private BuildDomainCounts(
          Provisioning prov,
          String domain,
          String defaultCos,
          Map<String, Integer> cosCount,
          Map<String, Integer> featureCount,
          Map<String, Set<String>> cosFeatureMap) {
        this.prov = prov;
        this.domain = domain;
        this.defaultCos = defaultCos;
        this.cosCount = cosCount;
        this.featureCount = featureCount;
        this.cosFeatureMap = cosFeatureMap;
      }

      void search() throws ServiceException {
        LdapProv ldapProv = (LdapProv) prov;
        String searchBaseDN = ldapProv.getDIT().domainToAccountSearchDN(domain);
        ZLdapFilter query = ZLdapFilterFactory.getInstance().allNonSystemAccounts();

        ldapProv.searchLdapOnReplica(searchBaseDN, query, null, this);
        ZimbraLog.account.debug("COS/Feature counts: %s + %s", cosCount, featureCount);
      }

      @Override
      public void visit(String dn, Map<String, Object> attrs, IAttributes ldapAttrs) {
        try {
          visitInternal(dn, attrs, ldapAttrs);
        } catch (ServiceException e) {
          ZimbraLog.account.error("encountered error, entry skipped ", e);
        }
      }

      private void visitInternal(String dn, Map<String, Object> attrs, IAttributes ldapAttrs)
          throws ServiceException {

        List<String> objectClass =
            ldapAttrs.getMultiAttrStringAsList(
                Provisioning.A_objectClass, IAttributes.CheckBinary.NOCHECK);
        if (objectClass == null || objectClass.isEmpty()) {
          ZimbraLog.account.error("DN: " + dn + ": does not have objectclass!");
          return;
        }

        if (objectClass.contains(AttributeClass.OC_zimbraAccount)) {
          String cosId = ldapAttrs.getAttrString(Provisioning.A_zimbraCOSId);
          if (cosId == null) {
            cosId = defaultCos;
          }

          // invalid COS id will revert to default COS id, however, this counter will count
          // the invalid ID and not count the reverted default ID.  i.e. 100 accounts with
          // invalid IDs will be counted as 100 accounts with invalid IDs and not properly
          // counted as 100 accounts in the default COS
          incrementCount(cosCount, cosId);
          Set<String> cosFeatures = getCosFeatures(prov, cosFeatureMap, cosId, defaultCos);

          Set<String> acctFeatures = getAccountFeatures(attrs);
          if (cosFeatures != null) {
            acctFeatures.addAll(cosFeatures);
          }
          for (String feature : acctFeatures) {
            incrementCount(featureCount, feature);
          }
        }
      }

      private Set<String> getAccountFeatures(Map<String, Object> attrs) {
        Set<String> acctFeatures = new HashSet<>();
        for (Map.Entry<String, Object> attr : attrs.entrySet()) {
          String attrName = attr.getKey();
          Object attrValue = attr.getValue();

          String value = null;
          if (attrValue instanceof String) {
            value = (String) attrValue;
          }

          if (attrName.toLowerCase().startsWith("zimbrafeature")
              && attrName.toLowerCase().endsWith("enabled")
              && "true".equalsIgnoreCase(value)) {
            acctFeatures.add(attrName);
          }
        }
        return acctFeatures;
      }
    }

    private static void incrementCount(Map<String, Integer> map, String key) {
      if (key == null || !map.containsKey(key)) {
        return;
      }
      map.put(key, map.get(key) + 1);
    }

    // search LDAP
    private void buildDomainCounts(
        Provisioning prov,
        String domain,
        String defaultCos,
        Map<String, Integer> cosCount,
        Map<String, Integer> featureCount,
        Map<String, Set<String>> cosFeatureMap)
        throws ServiceException {
      BuildDomainCounts counts =
          new BuildDomainCounts(prov, domain, defaultCos, cosCount, featureCount, cosFeatureMap);
      counts.search();
    }
  }
}
