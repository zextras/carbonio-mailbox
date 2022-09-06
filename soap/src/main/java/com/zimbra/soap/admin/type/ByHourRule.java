// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByHourRuleInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ByHourRule implements ByHourRuleInterface {

  /**
   * @zm-api-field-tag hour-list
   * @zm-api-field-description Comma separated list of hours where hour is a number between 0 and 23
   */
  @XmlAttribute(name = MailConstants.A_CAL_RULE_BYHOUR_HRLIST /* hrlist */, required = true)
  private final String list;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ByHourRule() {
    this((String) null);
  }

  public ByHourRule(String list) {
    this.list = list;
  }

  @Override
  public ByHourRuleInterface create(String list) {
    return new ByHourRule(list);
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
