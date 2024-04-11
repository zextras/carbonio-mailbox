// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.account;

import com.zimbra.common.service.ServiceException;

/** Keys used to look up Provisioning objects. */
public class Key {

  public enum AccountBy {

    // case must match protocol
    adminName,
    appAdminName,
    id,
    foreignPrincipal,
    name,
    krb5Principal;

    public static AccountBy fromString(String s) throws ServiceException {
      try {
        return AccountBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  // data sources
  public enum DataSourceBy {
    id,
    name;

    public static DataSourceBy fromString(String s) throws ServiceException {
      try {
        return DataSourceBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  // identities
  public enum IdentityBy {
    id,
    name;

    public static IdentityBy fromString(String s) throws ServiceException {
      try {
        return IdentityBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum DomainBy {

    // case must match protocol
    id,
    name,
    virtualHostname,
    krb5Realm,
    foreignName;

    public static DomainBy fromString(String s) throws ServiceException {
      try {
        return DomainBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum ServerBy {

    // case must match protocol
    id,
    name,
    serviceHostname;

    public static ServerBy fromString(String s) throws ServiceException {
      try {
        return ServerBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum ZimletBy {

    // case must match protocol
    id,
    name;

    public static ZimletBy fromString(String s) throws ServiceException {
      try {
        return ZimletBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  // signatures
  public enum SignatureBy {
    id,
    name;

    public static SignatureBy fromString(String s) throws ServiceException {
      try {
        return SignatureBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum CacheEntryBy {

    // case must match protocol
    id,
    name;

    public static CacheEntryBy fromString(String s) throws ServiceException {
      try {
        return CacheEntryBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum CosBy {

    // case must match protocol
    id,
    name;

    public static CosBy fromString(String s) throws ServiceException {
      try {
        return CosBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum CalendarResourceBy {

    // case must match protocol
    id,
    foreignPrincipal,
    name;

    public static CalendarResourceBy fromString(String s) throws ServiceException {
      try {
        return CalendarResourceBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum XMPPComponentBy {

    // case must match protocol
    id,
    name,
    serviceHostname;

    public static XMPPComponentBy fromString(String s) throws ServiceException {
      try {
        return XMPPComponentBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum DistributionListBy {

    // case must match protocol
    id,
    name;

    public static DistributionListBy fromString(String s) throws ServiceException {
      try {
        return DistributionListBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  public enum ShareLocatorBy {
    id;

    public static ShareLocatorBy fromString(String s) throws ServiceException {
      try {
        return ShareLocatorBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }
}
