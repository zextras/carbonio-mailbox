// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.google.common.collect.Iterables;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.account.ZAttrProvisioning;
import java.util.Collection;
import java.util.Map;

public class ZFeatures {

  private Map<String, Collection<String>> mAttrs;

  public ZFeatures(Map<String, Collection<String>> attrs) {
    mAttrs = attrs;
  }

  /**
   * @param name name of attr to get
   * @return null if unset, or first value in list
   */
  private String get(String name) {
    Collection<String> value = mAttrs.get(name);
    if (value == null || value.isEmpty()) {
      return null;
    }
    return Iterables.get(value, 0);
  }

  public boolean getBool(String name) {
    return ProvisioningConstants.TRUE.equals(get(name));
  }

  public boolean getGalAutoComplete() {
    return getBool(ZAttrProvisioning.A_zimbraFeatureGalAutoCompleteEnabled);
  }
}
