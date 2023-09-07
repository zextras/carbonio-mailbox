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

public class GranteeProvider {

  public NamedEntry lookupGranteeByEmailAddress(String name) throws ServiceException {
    if (name.indexOf('<') > 0) {
      InternetAddress addr = new InternetAddress(name);
      name = addr.getAddress();
    }
    Provisioning prov = Provisioning.getInstance();
    NamedEntry nentry = prov.get(AccountBy.name, name);
    if (nentry == null) {
      nentry = prov.getGroup(Key.DistributionListBy.name, name);
    }
    return nentry;
  }

  public NamedEntry lookupGranteeByName(String name, byte type, OperationContext operationContext)
      throws ServiceException {
    if (type == ACL.GRANTEE_AUTHUSER
        || type == ACL.GRANTEE_PUBLIC
        || type == ACL.GRANTEE_GUEST
        || type == ACL.GRANTEE_KEY) return null;

    Provisioning prov = Provisioning.getInstance();
    // for addresses, default to the authenticated user's domain
    if ((type == ACL.GRANTEE_USER || type == ACL.GRANTEE_GROUP) && name.indexOf('@') == -1) {
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
      switch (type) {
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
    switch (type) {
      case ACL.GRANTEE_COS:
        throw AccountServiceException.NO_SUCH_COS(name);
      case ACL.GRANTEE_DOMAIN:
        throw AccountServiceException.NO_SUCH_DOMAIN(name);
      case ACL.GRANTEE_USER:
        throw AccountServiceException.NO_SUCH_ACCOUNT(name);
      case ACL.GRANTEE_GROUP:
        throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(name);
      default:
        throw ServiceException.FAILURE("LDAP entry not found for " + name + " : " + type, null);
    }
  }

  public NamedEntry lookupGranteeByZimbraId(String zid, byte type) {
    Provisioning prov = Provisioning.getInstance();
    try {
      switch (type) {
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
