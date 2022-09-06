// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class AutoProvDirectoryEntry extends AdminKeyValuePairs {

  /**
   * @zm-api-field-tag dn
   * @zm-api-field-description DN
   */
  @XmlAttribute(name = AdminConstants.A_DN /* dn */, required = true)
  private String dn;

  /**
   * @zm-api-field-description Keys
   */
  @XmlElement(name = AdminConstants.E_KEY /* key */, required = false)
  private List<String> keys = Lists.newArrayList();

  public AutoProvDirectoryEntry() {}

  public void setDn(String dn) {
    this.dn = dn;
  }

  public void setKeys(Iterable<String> keys) {
    this.keys.clear();
    if (keys != null) {
      Iterables.addAll(this.keys, keys);
    }
  }

  public void addKey(String key) {
    this.keys.add(key);
  }

  public String getDn() {
    return dn;
  }

  public List<String> getKeys() {
    return Collections.unmodifiableList(keys);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("dn", dn).add("keys", keys);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
