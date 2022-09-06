// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class CheckedRight {

  // Note: SOAP handler gets AdminConstants.A_DENY attribute but it is ignored

  /**
   * @zm-api-field-tag right name
   * @zm-api-field-description Name of right
   */
  @XmlValue private String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckedRight() {}

  public CheckedRight(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
