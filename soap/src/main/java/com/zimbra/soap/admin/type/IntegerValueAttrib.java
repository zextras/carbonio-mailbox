// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class IntegerValueAttrib {

  /**
   * @zm-api-field-tag value
   * @zm-api-field-description Value
   */
  @XmlAttribute(name = AdminConstants.A_VALUE, required = false)
  private final Integer value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IntegerValueAttrib() {
    this((Integer) null);
  }

  public IntegerValueAttrib(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }
}
