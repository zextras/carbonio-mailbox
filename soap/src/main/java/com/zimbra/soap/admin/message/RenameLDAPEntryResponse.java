// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.LDAPUtilsConstants;
import com.zimbra.soap.admin.type.LDAPEntryInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = LDAPUtilsConstants.E_RENAME_LDAP_ENTRY_RESPONSE)
@XmlType(propOrder = {})
public class RenameLDAPEntryResponse {

  /**
   * @zm-api-field-description Information about updated LDAP entry
   */
  @XmlElement(name = LDAPUtilsConstants.E_LDAPEntry /* LDAPEntry */, required = true)
  private final LDAPEntryInfo LDAPentry;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RenameLDAPEntryResponse() {
    this((LDAPEntryInfo) null);
  }

  public RenameLDAPEntryResponse(LDAPEntryInfo LDAPentry) {
    this.LDAPentry = LDAPentry;
  }

  public LDAPEntryInfo getLDAPentry() {
    return LDAPentry;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("LDAPentry", LDAPentry);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
