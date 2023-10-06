// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.zextras.mailbox.midlewarepojo.GrantInput;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.generated.UserRights;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.util.AccountUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;

public final class ACLHelper {
  private static final String ACL_CACHE_KEY = "ENTRY.ACL_CACHE";
  private final AccountUtil accountUtil;
  private final Supplier<Long> timeNowSupplier;

  public ACLHelper() {
    timeNowSupplier = System::currentTimeMillis;
    accountUtil = new AccountUtil();
  }

  @Inject
  public ACLHelper(AccountUtil accountUtil, Supplier<Long> timeNowSupplier) {
    this.accountUtil = accountUtil;
    this.timeNowSupplier = timeNowSupplier;
  }

  /**
   * Returns all ACEs granted on the entry.
   *
   * @param entry the entry on which rights are granted
   * @return all ACEs granted on the entry.
   */
  public static List<ZimbraACE> getAllACEs(Entry entry) throws ServiceException {
    ZimbraACL acl = getACL(entry);
    return acl != null ? acl.getAllACEs() : null;
  }

  public static Set<ZimbraACE> getAllowedNotDelegableACEs(Entry entry) throws ServiceException {
    ZimbraACL acl = getACL(entry);
    return acl != null ? acl.getAllowedNotDelegableACEs() : null;
  }

  public static Set<ZimbraACE> getAllowedDelegableACEs(Entry entry) throws ServiceException {
    ZimbraACL acl = getACL(entry);
    return acl != null ? acl.getAllowedDelegableACEs() : null;
  }

  public static Set<ZimbraACE> getDeniedACEs(Entry entry) throws ServiceException {
    ZimbraACL acl = getACL(entry);
    return acl != null ? acl.getDeniedACEs() : null;
  }

  /**
   * Returns a Set of ACEs with the specified rights granted on the entry.
   *
   * @param entry the entry on which rights are granted
   * @param rights rights of interest
   * @return a Set of ACEs with the specified rights granted on the entry.
   */
  public static List<ZimbraACE> getACEs(Entry entry, Set<? extends Right> rights)
      throws ServiceException {
    ZimbraACL acl = getACL(entry);
    return acl != null ? acl.getACEs(rights) : null;
  }

  private static Multimap<Right, Entry> getGrantedRights(Account grantee, Set<String> fetchAttrs)
      throws ServiceException {
    SearchGrants search =
        new SearchGrants(
            grantee.getProvisioning(),
            EnumSet.of(TargetType.account),
            RightBearer.Grantee.getGrantee(grantee, false).getIdAndGroupIds());
    search.addFetchAttribute(fetchAttrs);
    Set<SearchGrants.GrantsOnTarget> results = search.doSearch().getResults();
    Multimap<Right, Entry> map = HashMultimap.create();
    for (SearchGrants.GrantsOnTarget grants : results) {
      ZimbraACL acl = grants.getAcl();
      for (ZimbraACE ace : acl.getAllACEs()) {
        if (ace.getGrantee().equals(grantee.getId())) {
          map.put(ace.getRight(), grants.getTargetEntry());
        }
      }
    }
    return map;
  }

  /** Returns {@link UserRights#R_sendOnBehalfOf} rights granted to the grantee. */
  public static List<Identity> getSendOnBehalfOf(Account grantee) throws ServiceException {
    Multimap<Right, Entry> rights =
        getGrantedRights(grantee, Collections.singleton(Provisioning.A_displayName));
    ImmutableList.Builder<Identity> result = ImmutableList.<Identity>builder();
    for (Entry entry : rights.get(UserRights.R_sendOnBehalfOf)) {
      Account grantor = (Account) entry;
      String mail = grantor.getName();
      String name = MoreObjects.firstNonNull(grantor.getDisplayName(), mail);
      Map<String, Object> attrs =
          ImmutableMap.<String, Object>builder()
              .put(Provisioning.A_zimbraPrefIdentityId, grantor.getId())
              .put(Provisioning.A_zimbraPrefIdentityName, name)
              .put(Provisioning.A_zimbraPrefFromDisplay, name)
              .put(Provisioning.A_zimbraPrefFromAddress, mail)
              .put(Provisioning.A_objectClass, AttributeClass.OC_zimbraAclTarget)
              .build();
      result.add(new Identity(grantee, name, grantor.getId(), attrs, grantee.getProvisioning()));
    }
    return result.build();
  }

  /** Grant rights on a target entry. */
  public static List<ZimbraACE> grantRight(Provisioning prov, Entry target, Set<ZimbraACE> aces)
      throws ServiceException {
    for (ZimbraACE ace : aces) {
      ZimbraACE.validate(ace);
    }
    ZimbraACL acl = getACL(target, Boolean.TRUE);
    List<ZimbraACE> granted = null;

    if (acl == null) {
      acl = new ZimbraACL(aces);
      granted = acl.getAllACEs();
    } else {
      // Make a copy so we don't interfere with others that are using the acl.
      // This instance of acl will never be used in any AccessManager code path.
      // It only lives within this method for serialization.
      // serialize will erase the cached ZimbraACL object on the target object.
      // The new ACL will be loaded when it is needed.
      acl = acl.clone();
      granted = acl.grantAccess(aces);
    }

    serialize(prov, target, acl);

    PermissionCache.invalidateCache(target);

    return granted;
  }

  /**
   * Revoke(remove) rights from a target entry. If a right was not previously granted on the target,
   * NO error is thrown.
   *
   * @return a Set of grants that are actually revoked by this call
   */
  public static List<ZimbraACE> revokeRight(Provisioning prov, Entry target, Set<ZimbraACE> aces)
      throws ServiceException {
    ZimbraACL acl = getACL(target, Boolean.TRUE);
    if (acl == null) {
      return new ArrayList<ZimbraACE>(); // return empty list
    }
    // Make a copy so we don't interfere with others that are using the acl.
    // This instance of acl will never be used in any AccessManager code path.
    // It only lives within this method for serialization.
    // serialize will erase the cached ZimbraACL object on the target object.
    // The new ACL will be loaded when it is needed.
    acl = acl.clone();
    List<ZimbraACE> revoked = acl.revokeAccess(aces);
    serialize(prov, target, acl);

    PermissionCache.invalidateCache(target);

    return revoked;
  }

  /** Persists grants in LDAP */
  private static void serialize(Provisioning prov, Entry entry, ZimbraACL acl)
      throws ServiceException {
    // modifyAttrs will erase cached ACL and permission cache on the target
    prov.modifyAttrs(entry, Collections.singletonMap(Provisioning.A_zimbraACE, acl.serialize()));
  }

  /**
   * Get cached grants, if not in cache, load from LDAP.
   *
   * @param entry
   * @return
   * @throws ServiceException
   */
  static ZimbraACL getACL(Entry entry) throws ServiceException {
    return getACL(entry, Boolean.FALSE);
  }

  /**
   * Get cached grants, if not in cache, load from LDAP.
   *
   * @param entry the LDAP entry object for which we need ACLs
   * @param loadFromLdap when true we always load from LDAP, when false we try the cache first.
   * @return
   * @throws ServiceException
   */
  static ZimbraACL getACL(Entry entry, boolean loadFromLdap) throws ServiceException {
    ZimbraACL acl = null;
    if (!loadFromLdap) {
      acl = (ZimbraACL) entry.getCachedData(ACL_CACHE_KEY);
    }
    if (acl != null) {
      return acl;
    } else {
      acl = null;
      String[] aces = entry.getMultiAttr(Provisioning.A_zimbraACE);
      if (aces.length == 0) {
        return null;
      } else {
        acl = new ZimbraACL(aces, TargetType.getTargetType(entry), entry.getLabel());
        entry.setCachedData(ACL_CACHE_KEY, acl);
      }
    }
    return acl;
  }

  /**
   * Validates the grant expiry time against the maximum allowed expiry duration and returns the
   * effective expiry time for the grant.
   *
   * <p>If current expiry time is null it generates a new value. The new value will be 0 if
   * maxLifeTime is 0 (no lifetime)
   *
   * <p>If current expiry time is 0 and max lifetime more than 0 throws.
   *
   * <p>If current expiry time greater than zero, returns its value (still have time)
   *
   * @param grantExpiry Grant expiry XML attribute value
   * @param maxLifetime Maximum allowed grant expiry duration
   * @return Effective expiry time for the grant. Return value of 0 indicates that grant never
   *     expires.
   * @throws ServiceException If the grant expiry time is not valid according to the expiration
   *     policy.
   */
  public long validateGrantExpiry(String grantExpiry, long maxLifetime) throws ServiceException {
    long now = timeNowSupplier.get();
    long grantedTime = now + maxLifetime;
    if (Objects.isNull(grantExpiry)) {
      if (Objects.equals(0L, maxLifetime)) {
        return 0L;
      }
      return grantedTime;
    }
    final long grantExpiryLong = Long.parseLong(grantExpiry);
    if (grantExpiryLong > grantedTime) {
      throw ServiceException.PERM_DENIED("share expiration policy conflict");
    }
    if (Objects.equals(0L, grantExpiryLong) && maxLifetime > 0) {
      throw ServiceException.PERM_DENIED("share expiration policy conflict");
    }
    return grantExpiryLong;
  }

  public ACL parseACL(Element eAcl, MailItem.Type folderType, Account account)
      throws ServiceException {
    if (eAcl == null) {
      return null;
    }

    long internalGrantExpiry =
        validateGrantExpiry(
            eAcl.getAttribute(MailConstants.A_INTERNAL_GRANT_EXPIRY, null),
            AccountUtil.getMaxInternalShareLifetime(account, folderType));
    long guestGrantExpiry =
        validateGrantExpiry(
            eAcl.getAttribute(MailConstants.A_GUEST_GRANT_EXPIRY, null),
            AccountUtil.getMaxExternalShareLifetime(account, folderType));
    ACL acl = new ACL(internalGrantExpiry, guestGrantExpiry);

    for (Element grant : eAcl.listElements(MailConstants.E_GRANT)) {
      String zid = grant.getAttribute(MailConstants.A_ZIMBRA_ID);
      byte gtype = ACL.stringToType(grant.getAttribute(MailConstants.A_GRANT_TYPE));
      short rights = ACL.stringToRights(grant.getAttribute(MailConstants.A_RIGHTS));
      long expiry =
          gtype == ACL.GRANTEE_PUBLIC
              ? validateGrantExpiry(
                  grant.getAttribute(MailConstants.A_EXPIRY, null),
                  accountUtil.getMaxPublicShareLifetime(account, folderType))
              : grant.getAttributeLong(MailConstants.A_EXPIRY, 0);

      String secret = null;
      if (gtype == ACL.GRANTEE_KEY) {
        secret = grant.getAttribute(MailConstants.A_ACCESSKEY, null);
      } else if (gtype == ACL.GRANTEE_GUEST) {
        secret = grant.getAttribute(MailConstants.A_ARGS, null);
        // bug 30891 for 5.0.x
        if (secret == null) {
          secret = grant.getAttribute(MailConstants.A_PASSWORD, null);
        }
      }
      acl.grantAccess(zid, gtype, rights, secret, expiry);
    }
    return acl;
  }

  public ACL parseACL(
      String internalGrantExpiryString,
      String guestGrantExpiryString,
      List<GrantInput> grantInputList,
      MailItem.Type folderType,
      Account account)
      throws ServiceException {

    long internalGrantExpiry =
        validateGrantExpiry(
            internalGrantExpiryString,
            AccountUtil.getMaxInternalShareLifetime(account, folderType));
    long guestGrantExpiry =
        validateGrantExpiry(
            guestGrantExpiryString, AccountUtil.getMaxExternalShareLifetime(account, folderType));
    ACL acl = new ACL(internalGrantExpiry, guestGrantExpiry);

    for (GrantInput grant : grantInputList) {
      String zid = grant.getZid();
      byte granteeType = grant.getGranteeType();
      short rights = grant.getRights();
      long expiry =
          granteeType == ACL.GRANTEE_PUBLIC
              ? validateGrantExpiry(
                  String.valueOf(grant.getGrantExpiry()),
                  accountUtil.getMaxPublicShareLifetime(account, folderType))
              : grant.getGrantExpiry();

      String secret = null;
      if (granteeType == ACL.GRANTEE_KEY) {
        secret = grant.getAccessKey();
      } else if (granteeType == ACL.GRANTEE_GUEST) {
        secret = grant.getSecretArgs();
        // bug 30891 for 5.0.x
        if (secret == null) {
          secret = grant.getPassword();
        }
      }
      acl.grantAccess(zid, granteeType, rights, secret, expiry);
    }
    return acl;
  }
}
