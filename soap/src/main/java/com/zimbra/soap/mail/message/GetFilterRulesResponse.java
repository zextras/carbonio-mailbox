// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import com.zimbra.soap.mail.type.FilterRule;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_FILTER_RULES_RESPONSE)
public final class GetFilterRulesResponse {

  /**
   * @zm-api-field-description Filter rules
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = MailConstants.E_FILTER_RULES /* filterRules */, required = true)
  @XmlElement(name = MailConstants.E_FILTER_RULE /* filterRule */, required = false)
  private final List<FilterRule> rules = Lists.newArrayList();

  public GetFilterRulesResponse() {}

  public void setFilterRules(Collection<FilterRule> list) {
    rules.clear();
    if (list != null) {
      rules.addAll(list);
    }
  }

  public void addFilterRule(FilterRule rule) {
    rules.add(rule);
  }

  public void addFilterRule(Collection<FilterRule> list) {
    rules.addAll(list);
  }

  public List<FilterRule> getFilterRules() {
    return Collections.unmodifiableList(rules);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("rules", rules).toString();
  }
}
