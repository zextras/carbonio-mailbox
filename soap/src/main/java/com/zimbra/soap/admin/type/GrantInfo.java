// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class GrantInfo {

  /**
   * @zm-api-field-description Information on target
   */
  @XmlElement(name = AdminConstants.E_TARGET /* target */, required = true)
  private final TypeIdName target;

  /**
   * @zm-api-field-description Information on grantee
   */
  @XmlElement(name = AdminConstants.E_GRANTEE /* grantee */, required = true)
  private final GranteeInfo grantee;

  /**
   * @zm-api-field-description Information on right
   */
  @XmlElement(name = AdminConstants.E_RIGHT /* right */, required = true)
  private final RightModifierInfo right;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GrantInfo() {
    this((TypeIdName) null, (GranteeInfo) null, (RightModifierInfo) null);
  }

  public GrantInfo(TypeIdName target, GranteeInfo grantee, RightModifierInfo right) {
    this.target = target;
    this.grantee = grantee;
    this.right = right;
  }

  public TypeIdName getTarget() {
    return target;
  }

  public GranteeInfo getGrantee() {
    return grantee;
  }

  public RightModifierInfo getRight() {
    return right;
  }
}
