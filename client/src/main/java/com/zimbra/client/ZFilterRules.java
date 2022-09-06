// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;
import com.zimbra.soap.mail.type.FilterRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;

public final class ZFilterRules implements ToZJSONObject {

  private final List<ZFilterRule> rules;

  public ZFilterRules(List<ZFilterRule> rules) {
    this.rules = rules;
  }

  public ZFilterRules(ZFilterRules rules) {
    this.rules = new ArrayList<ZFilterRule>(rules.getRules());
  }

  public ZFilterRules(Collection<FilterRule> list) throws ServiceException {
    this.rules = Lists.newArrayListWithCapacity(list.size());
    for (FilterRule rule : list) {
      this.rules.add(new ZFilterRule(rule));
    }
  }

  public List<FilterRule> toJAXB() {
    List<FilterRule> list = Lists.newArrayListWithCapacity(rules.size());
    for (ZFilterRule rule : rules) {
      list.add(rule.toJAXB());
    }
    return list;
  }

  public List<ZFilterRule> getRules() {
    return rules;
  }

  @Override
  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject jo = new ZJSONObject();
    jo.put("rules", rules);
    return jo;
  }

  @Override
  public String toString() {
    return String.format("[ZFilterRules]");
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }
}
