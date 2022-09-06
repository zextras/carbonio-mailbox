// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.TargetBy;
import com.zimbra.soap.type.TargetType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class EffectiveRightsTargetSelector {

  /**
   * @zm-api-field-tag target-type
   * @zm-api-field-description Target type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE, required = true)
  private final TargetType type;

  /**
   * @zm-api-field-tag target-selector-by
   * @zm-api-field-description Select the meaning of <b>{target-selector-key}</b>
   */
  @XmlAttribute(name = AdminConstants.A_BY, required = false)
  private final TargetBy by;

  /**
   * @zm-api-field-tag target-selector-key
   * @zm-api-field-description The key used to identify the target. Meaning determined by
   *     <b>{target-selector-by}</b>
   */
  @XmlValue private final String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private EffectiveRightsTargetSelector() {
    this((TargetType) null, (TargetBy) null, (String) null);
  }

  public EffectiveRightsTargetSelector(TargetType type, TargetBy by, String value) {
    this.type = type;
    this.by = by;
    this.value = value;
  }

  public TargetType getType() {
    return type;
  }

  public TargetBy getBy() {
    return by;
  }

  public String getValue() {
    return value;
  }
}
