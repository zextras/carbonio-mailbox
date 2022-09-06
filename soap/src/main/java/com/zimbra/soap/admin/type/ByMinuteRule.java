// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByMinuteRuleInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ByMinuteRule implements ByMinuteRuleInterface {

  /**
   * @zm-api-field-tag minute-list
   * @zm-api-field-description Comma separated list of minutes where minute is a number between 0
   *     and 59
   */
  @XmlAttribute(name = MailConstants.A_CAL_RULE_BYMINUTE_MINLIST, required = true)
  private final String list;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ByMinuteRule() {
    this((String) null);
  }

  public ByMinuteRule(String list) {
    this.list = list;
  }

  @Override
  public ByMinuteRuleInterface create(String list) {
    return new ByMinuteRule(list);
  }

  @Override
  public String getList() {
    return list;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("list", list).toString();
  }
}
