// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

public enum LdapConnType {
  PLAIN,
  LDAPS,
  STARTTLS,
  LDAPI;

  private static final String LDAPI_SCHEME = "ldapi";
  private static final String LDAPS_SCHEME = "ldaps";
  private static final String LDAPI_URL = LDAPI_SCHEME + "://";
  private static final String LDAPS_URL = LDAPS_SCHEME + "://";

  public static LdapConnType getConnType(String urls, boolean wantStartTLS) {
    if (urls.toLowerCase().contains(LDAPI_URL)) {
      return LDAPI;
    } else if (urls.toLowerCase().contains(LDAPS_URL)) {
      return LDAPS;
    } else if (wantStartTLS) {
      return STARTTLS;
    } else {
      return PLAIN;
    }
  }

  /*
   * for external LDAP, only called from legacy external GAL code
   */
  public static boolean requireStartTLS(String[] urls, boolean wantStartTLS) {
    if (wantStartTLS) {
      for (String url : urls) {
        if (url.toLowerCase().contains(LDAPS_URL)) return false;
      }
      return true;
    }
    return false;
  }

  public static boolean isLDAPI(String scheme) {
    return LDAPI_SCHEME.equalsIgnoreCase(scheme);
  }
}
