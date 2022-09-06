// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.mail.type.Policy;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Delete a system retention policy.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_DELETE_SYSTEM_RETENTION_POLICY_REQUEST)
public class DeleteSystemRetentionPolicyRequest {

  /**
   * @zm-api-field-description COS
   */
  @XmlElement(name = AdminConstants.E_COS)
  private CosSelector cos;

  public void setCos(CosSelector cos) {
    this.cos = cos;
  }

  public CosSelector getCos() {
    return cos;
  }

  /**
   * @zm-api-field-description Details of policy
   */
  @XmlElement(
      name = AdminConstants.E_POLICY,
      namespace = MailConstants.NAMESPACE_STR,
      required = true)
  private Policy policy;

  public DeleteSystemRetentionPolicyRequest() {}

  public DeleteSystemRetentionPolicyRequest(Policy p) {
    policy = p;
  }

  public Policy getPolicy() {
    return policy;
  }
}
