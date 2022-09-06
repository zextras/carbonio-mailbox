// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.TargetType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class EffectiveRightsTargetInfo extends EffectiveRightsInfo {

  /**
   * @zm-api-field-tag target-type
   * @zm-api-field-description Target type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE /* type */, required = true)
  private final TargetType type;

  /**
   * @zm-api-field-tag target-id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag target-name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = AdminConstants.A_NAME /* name */, required = true)
  private final String name;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private EffectiveRightsTargetInfo() {
    this(
        (TargetType) null,
        (String) null,
        (String) null,
        (Iterable<RightWithName>) null,
        (EffectiveAttrsInfo) null,
        (EffectiveAttrsInfo) null);
  }

  public EffectiveRightsTargetInfo(
      TargetType type,
      String id,
      String name,
      EffectiveAttrsInfo setAttrs,
      EffectiveAttrsInfo getAttrs) {
    this(type, id, name, (Iterable<RightWithName>) null, setAttrs, getAttrs);
  }

  public EffectiveRightsTargetInfo(
      TargetType type,
      String id,
      String name,
      Iterable<RightWithName> rights,
      EffectiveAttrsInfo setAttrs,
      EffectiveAttrsInfo getAttrs) {
    super(rights, setAttrs, getAttrs);
    this.type = type;
    this.id = id;
    this.name = name;
  }

  public TargetType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
