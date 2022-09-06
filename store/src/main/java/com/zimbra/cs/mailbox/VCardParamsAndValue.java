// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;

public final class VCardParamsAndValue {
  private final Set<String> params = Sets.newHashSet();
  // Technically, a property can have multiple values but we treat it as one value
  private final String value;

  public VCardParamsAndValue(String value) {
    this.value = value;
  }

  public VCardParamsAndValue(String value, Set<String> params) {
    this.value = value;
    if (params != null) {
      this.params.addAll(params);
    }
  }

  public String getValue() {
    return value;
  }

  public Set<String> getParams() {
    return params;
  }

  public static String getFirstValue(String key, ListMultimap<String, VCardParamsAndValue> mmap) {
    List<VCardParamsAndValue> vals = mmap.get(key);
    if (vals == null || vals.isEmpty()) {
      return null;
    }
    return vals.get(0).getValue();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("params", params).add("value", value).toString();
  }
}
