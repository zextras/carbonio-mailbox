// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.LDAPUtilsConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Rename LDAP Entry
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = LDAPUtilsConstants.E_RENAME_LDAP_ENTRIY_REQUEST)
public class RenameLDAPEntryRequest {

  /**
   * @zm-api-field-tag current-dn
   * @zm-api-field-description A valid LDAP DN String (RFC 2253) that identifies the LDAP object
   */
  @XmlAttribute(name = LDAPUtilsConstants.E_DN /* dn */, required = true)
  private final String dn;

  /**
   * @zm-api-field-tag new-dn
   * @zm-api-field-description New DN - a valid LDAP DN String (RFC 2253) that describes the new DN
   *     to be given to the LDAP object
   */
  @XmlAttribute(name = LDAPUtilsConstants.E_NEW_DN /* new_dn */, required = true)
  private final String newDn;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RenameLDAPEntryRequest() {
    this((String) null, (String) null);
  }

  public RenameLDAPEntryRequest(String dn, String newDn) {
    this.dn = dn;
    this.newDn = newDn;
  }

  public String getDn() {
    return dn;
  }

  public String getNewDn() {
    return newDn;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("dn", dn).add("newDn", newDn);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
