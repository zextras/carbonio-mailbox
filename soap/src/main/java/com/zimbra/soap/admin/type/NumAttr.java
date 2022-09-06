// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.NumAttrInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class NumAttr implements NumAttrInterface {

  /**
   * @zm-api-field-tag num
   * @zm-api-field-description Number
   */
  @XmlAttribute(name = MailConstants.A_CAL_RULE_COUNT_NUM /* num */, required = true)
  private final int num;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private NumAttr() {
    this(-1);
  }

  public NumAttr(int num) {
    this.num = num;
  }

  @Override
  public NumAttrInterface create(int num) {
    return new NumAttr(num);
  }

  @Override
  public int getNum() {
    return num;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("num", num).toString();
  }
}
