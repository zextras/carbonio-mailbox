// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.ModifyZimletPrefsSpec;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify Zimlet Preferences
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_MODIFY_ZIMLET_PREFS_REQUEST)
public class ModifyZimletPrefsRequest {

  /**
   * @zm-api-field-description Zimlet Preference Specifications
   */
  @XmlElement(name = AccountConstants.E_ZIMLET /* zimlet */, required = false)
  private List<ModifyZimletPrefsSpec> zimlets = Lists.newArrayList();

  public ModifyZimletPrefsRequest() {}

  public void setZimlets(Iterable<ModifyZimletPrefsSpec> zimlets) {
    this.zimlets.clear();
    if (zimlets != null) {
      Iterables.addAll(this.zimlets, zimlets);
    }
  }

  public void addZimlet(ModifyZimletPrefsSpec zimlet) {
    this.zimlets.add(zimlet);
  }

  public List<ModifyZimletPrefsSpec> getZimlets() {
    return zimlets;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("zimlets", zimlets);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
