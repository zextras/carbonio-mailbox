// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

public class LdapConstants {

  public static final String LDAP_TRUE = "TRUE";
  public static final String LDAP_FALSE = "FALSE";
  public static final String EARLIEST_SYNC_TOKEN = "19700101000000Z";

  public static final String DN_ROOT_DSE = "";
  public static final String ATTR_dn = "dn";
  public static final String ATTR_dc = "dc";
  public static final String ATTR_uid = "uid";
  public static final String ATTR_ou = "ou";
  public static final String ATTR_cn = "cn";
  public static final String ATTR_objectClass = "objectClass";
  public static final String ATTR_createTimestamp = "createTimestamp";
  public static final String ATTR_hasSubordinates = "hasSubordinates";

  // AD attrs
  public static final String ATTR_memberOf = "memberOf";

  // milli seconds to wait for checking LDAP server health
  public static final int CHECK_LDAP_SLEEP_MILLIS = 5000;

  public static final String DN_SUBTREE_MATCH_ATTR = "entryDN";
  public static final String DN_SUBTREE_MATCH_MATCHING_RULE = "dnSubtreeMatch";
  public static final String DN_SUBTREE_MATCH_FILTER_TEMPLATE =
      "(" + DN_SUBTREE_MATCH_ATTR + ":" + DN_SUBTREE_MATCH_MATCHING_RULE + ":=%s)";

  public static final String FILTER_TYPE_EQUAL = "=";
  public static final String FILTER_TYPE_GREATER_OR_EQUAL = ">=";
  public static final String FILTER_TYPE_LESS_OR_EQUAL = "<=";
  public static final String FILTER_VALUE_ANY = "*";

  public static final String OC_dcObject = "dcObject";
  public static final String PEOPLE = "people";
  public static final String TOP = "top";
  public static final String PERSON = "person";
  public static final String ORGANIZATIONAL_PERSON = "organizationalPerson";
}
