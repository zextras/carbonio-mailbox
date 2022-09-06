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
public class ComboRightInfo {

  /**
   * @zm-api-field-tag right-name
   * @zm-api-field-description Right name
   */
  @XmlAttribute(name = AdminConstants.A_N /* n */, required = true)
  private final String name;

  /**
   * @zm-api-field-tag type
   * @zm-api-field-description Type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE /* type */, required = true)
  private final RightInfo.RightType type;

  /**
   * @zm-api-field-tag target-type
   * @zm-api-field-description Target type
   */
  @XmlAttribute(name = AdminConstants.A_TARGET_TYPE /* targetType */, required = false)
  private final String targetType;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ComboRightInfo() {
    this((String) null, (RightInfo.RightType) null, (String) null);
  }

  public ComboRightInfo(String name, RightInfo.RightType type, String targetType) {
    this.name = name;
    this.type = type;
    this.targetType = targetType;
  }

  public String getName() {
    return name;
  }

  public RightInfo.RightType getType() {
    return type;
  }

  public String getTargetType() {
    return targetType;
  }
}
