// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.EffectiveRightsTargetSelector;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.admin.type.RightModifierInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Grant a right on a target to an individual or group grantee.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GRANT_RIGHT_REQUEST)
public class GrantRightRequest {

  /**
   * @zm-api-field-description Target selector
   */
  @XmlElement(name = AdminConstants.E_TARGET, required = true)
  private final EffectiveRightsTargetSelector target;

  /**
   * @zm-api-field-description Grantee selector
   */
  @XmlElement(name = AdminConstants.E_GRANTEE, required = true)
  private final GranteeSelector grantee;

  /**
   * @zm-api-field-description Right
   */
  @XmlElement(name = AdminConstants.E_RIGHT, required = true)
  private final RightModifierInfo right;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GrantRightRequest() {
    this((EffectiveRightsTargetSelector) null, (GranteeSelector) null, (RightModifierInfo) null);
  }

  public GrantRightRequest(
      EffectiveRightsTargetSelector target, GranteeSelector grantee, RightModifierInfo right) {
    this.target = target;
    this.grantee = grantee;
    this.right = right;
  }

  public EffectiveRightsTargetSelector getTarget() {
    return target;
  }

  public GranteeSelector getGrantee() {
    return grantee;
  }

  public RightModifierInfo getRight() {
    return right;
  }
}
