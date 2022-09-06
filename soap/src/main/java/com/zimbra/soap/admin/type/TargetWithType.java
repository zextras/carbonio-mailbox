// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class TargetWithType {

  // TODO:Is there an enum for this?
  /**
   * @zm-api-field-tag target-type
   * @zm-api-field-description Target type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE, required = true)
  private final String type;

  /**
   * @zm-api-field-tag target-value
   * @zm-api-field-description Value matching <b>{target-type}</b> if this is part of a response
   *     (otherwise blank)
   */
  @XmlValue private final String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private TargetWithType() {
    this((String) null, (String) null);
  }

  public TargetWithType(String type, String value) {
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }
}
