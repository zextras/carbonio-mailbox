// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.IntervalRuleInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class IntervalRule implements IntervalRuleInterface {

  /**
   * @zm-api-field-tag rule-interval
   * @zm-api-field-description Rule interval count - a positive integer
   */
  @XmlAttribute(name = MailConstants.A_CAL_RULE_INTERVAL_IVAL /* ival */, required = true)
  private final int ival;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IntervalRule() {
    this(-1);
  }

  public IntervalRule(int ival) {
    this.ival = ival;
  }

  public static IntervalRule create(int ival) {
    return new IntervalRule(ival);
  }

  @Override
  public int getIval() {
    return ival;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("ival", ival).toString();
  }
}
