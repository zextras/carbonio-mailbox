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
public class ValueAttrib {

  /**
   * @zm-api-field-tag value
   * @zm-api-field-description Value
   */
  @XmlAttribute(name = AdminConstants.A_VALUE /* value */, required = true)
  private final String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ValueAttrib() {
    this((String) null);
  }

  public ValueAttrib(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
