// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;
import javax.xml.bind.annotation.XmlEnum;

// TODO: Use this in ZimbraServer code instead of Provisioning.CacheEntryType
@XmlEnum
public enum CacheEntryType {
  // non ldap entries
  acl,
  locale,
  license,

  // ldap entries
  all, // all ldap entries
  account,
  config,
  globalgrant,
  cos,
  domain,
  galgroup,
  group,
  mime,
  server;

  private static final Joiner PIPE_JOINER = Joiner.on("|");

  public static CacheEntryType fromString(String s) throws ServiceException {
    try {
      return CacheEntryType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST("unknown cache type: " + s, e);
    }
  }

  public static String names() {
    return PIPE_JOINER.join(CacheEntryType.values());
  }
}
