package com.zextras.mailbox.usecase.ldap;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.mime.InternetAddress;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.OperationContext;

/**
 * Provider class to look up a grantee.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class GranteeProvider {

  /**
   * Looks up a grantee by email address.
   *
   * @param email email to find a grantee by
   * @return {@link NamedEntry}
   * @throws ServiceException if not able to find a grantee by email address
   */
  public NamedEntry lookupGranteeByEmailAddress(String email) throws ServiceException {
    if (email.indexOf('<') > 0) {
      InternetAddress addr = new InternetAddress(email);
      email = addr.getAddress();
    }
    Provisioning prov = Provisioning.getInstance();
    NamedEntry nentry = prov.get(AccountBy.name, email);
    if (nentry == null) {
      nentry = prov.getGroup(Key.DistributionListBy.name, email);
    }
    return nentry;
  }

  /**
   * Looks up a grantee by name.
   *
   * @param name grantee name
   * @param granteeType ACL grantee type
   * @param operationContext an {@link OperationContext}
   * @return {@link NamedEntry}
   * @throws ServiceException if not able to find a grantee by name
   */
  public NamedEntry lookupGranteeByName(
      String name, byte granteeType, OperationContext operationContext) throws ServiceException {
    if (granteeType == ACL.GRANTEE_AUTHUSER
        || granteeType == ACL.GRANTEE_PUBLIC
        || granteeType == ACL.GRANTEE_GUEST
        || granteeType == ACL.GRANTEE_KEY) return null;

    Provisioning prov = Provisioning.getInstance();
    // for addresses, default to the authenticated user's domain
    if ((granteeType == ACL.GRANTEE_USER || granteeType == ACL.GRANTEE_GROUP)
        && name.indexOf('@') == -1) {
      Account authacct =
          prov.get(
              AccountBy.id,
              operationContext.getmAuthTokenAccountId(),
              operationContext.getAuthToken());
      String authname = (authacct == null ? null : authacct.getName());
      if (authacct != null) name += authname.substring(authname.indexOf('@'));
    }

    NamedEntry nentry = null;
    if (name != null)
      switch (granteeType) {
        case ACL.GRANTEE_COS:
          nentry = prov.get(Key.CosBy.name, name);
          break;
        case ACL.GRANTEE_DOMAIN:
          nentry = prov.get(Key.DomainBy.name, name);
          break;
        case ACL.GRANTEE_USER:
          nentry = lookupGranteeByEmailAddress(name);
          break;
        case ACL.GRANTEE_GROUP:
          nentry = prov.getGroup(Key.DistributionListBy.name, name);
          break;
      }

    if (nentry != null) return nentry;
    switch (granteeType) {
      case ACL.GRANTEE_COS:
        throw AccountServiceException.NO_SUCH_COS(name);
      case ACL.GRANTEE_DOMAIN:
        throw AccountServiceException.NO_SUCH_DOMAIN(name);
      case ACL.GRANTEE_USER:
        throw AccountServiceException.NO_SUCH_ACCOUNT(name);
      case ACL.GRANTEE_GROUP:
        throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(name);
      default:
        throw ServiceException.FAILURE(
            "LDAP entry not found for " + name + " : " + granteeType, null);
    }
  }

  /**
   * Looks up a grantee by id.
   *
   * @param zimbraId account zimbraId attribute
   * @param granteeType ACL grantee type
   * @return {@link NamedEntry}
   */
  public NamedEntry lookupGranteeByZimbraId(String zid, byte granteeType) {
    Provisioning prov = Provisioning.getInstance();
    try {
      switch (granteeType) {
        case ACL.GRANTEE_COS:
          return prov.get(Key.CosBy.id, zid);
        case ACL.GRANTEE_DOMAIN:
          return prov.get(Key.DomainBy.id, zid);
        case ACL.GRANTEE_USER:
          return prov.get(AccountBy.id, zid);
        case ACL.GRANTEE_GROUP:
          return prov.getGroup(Key.DistributionListBy.id, zid);
        case ACL.GRANTEE_GUEST:
        case ACL.GRANTEE_KEY:
        case ACL.GRANTEE_AUTHUSER:
        case ACL.GRANTEE_PUBLIC:
        default:
          return null;
      }
    } catch (ServiceException e) {
      return null;
    }
  }
}
