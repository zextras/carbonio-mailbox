// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ModifyZimletPrefsSpec {

  /**
   * @zm-api-field-description Zimlet name
   */
  @XmlAttribute(name = AccountConstants.A_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-description Zimlet presence setting <br>
   *     Valid values : "enabled" | "disabled"
   */
  @XmlAttribute(name = AccountConstants.A_ZIMLET_PRESENCE /* presence */, required = true)
  private String presence;

  private ModifyZimletPrefsSpec() {}

  private ModifyZimletPrefsSpec(String name, String presence) {
    setName(name);
    setPresence(presence);
  }

  public static ModifyZimletPrefsSpec createForNameAndPresence(String name, String presence) {
    return new ModifyZimletPrefsSpec(name, presence);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPresence(String presence) {
    this.presence = presence;
  }

  public String getName() {
    return name;
  }

  public String getPresence() {
    return presence;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("name", name).add("presence", presence);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
