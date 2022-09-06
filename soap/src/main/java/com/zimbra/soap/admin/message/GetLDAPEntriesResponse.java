// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.LDAPUtilsConstants;
import com.zimbra.soap.admin.type.LDAPEntryInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = LDAPUtilsConstants.E_GET_LDAP_ENTRIES_RESPONSE)
@XmlType(propOrder = {})
public class GetLDAPEntriesResponse {

  /**
   * @zm-api-field-description LDAP entries
   */
  @XmlElement(name = LDAPUtilsConstants.E_LDAPEntry /* LDAPEntry */, required = false)
  private List<LDAPEntryInfo> LDAPentries = Lists.newArrayList();

  public GetLDAPEntriesResponse() {}

  public void setLDAPentries(Iterable<LDAPEntryInfo> LDAPentries) {
    this.LDAPentries.clear();
    if (LDAPentries != null) {
      Iterables.addAll(this.LDAPentries, LDAPentries);
    }
  }

  public void addLDAPentry(LDAPEntryInfo LDAPentry) {
    this.LDAPentries.add(LDAPentry);
  }

  public List<LDAPEntryInfo> getLDAPentries() {
    return Collections.unmodifiableList(LDAPentries);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("LDAPentries", LDAPentries);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
