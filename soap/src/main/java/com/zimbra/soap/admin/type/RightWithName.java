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
public class RightWithName {

  /**
   * @zm-api-field-tag right-name
   * @zm-api-field-description Right name
   */
  @XmlAttribute(name = AdminConstants.A_N /* n */, required = false)
  private final String name;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RightWithName() {
    this(null);
  }

  public RightWithName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
