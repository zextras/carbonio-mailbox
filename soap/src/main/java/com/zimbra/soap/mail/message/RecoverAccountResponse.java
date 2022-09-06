// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Recover account response
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_RECOVER_ACCOUNT_RESPONSE)
public final class RecoverAccountResponse {
  /**
   * @zm-api-field-description recoveryAccount
   */
  @XmlAttribute(name = MailConstants.A_RECOVERY_ACCOUNT /* recoveryAccount */, required = false)
  private String recoveryAccount;

  /** - * @zm-api-field-description attempts remaining before feature suspension */
  @XmlAttribute(
      name = MailConstants.A_RECOVERY_ATTEMPTS_LEFT /* RecoveryAttemptsLeft */,
      required = false)
  private Integer recoveryAttemptsLeft;

  public RecoverAccountResponse() {}

  public RecoverAccountResponse(String recoveryAccount) {
    this.recoveryAccount = recoveryAccount;
  }

  /**
   * @return the recovery recoveryAccount
   */
  public String getRecoveryAccount() {
    return recoveryAccount;
  }

  /**
   * @param recoveryAccount the recovery account
   */
  public void setRecoveryAccount(String recoveryAccount) {
    this.recoveryAccount = recoveryAccount;
  }

  public Integer getRecoveryAttemptsLeft() {
    return recoveryAttemptsLeft;
  }

  public void setRecoveryAttemptsLeft(Integer recoveryAttemptsLeft) {
    this.recoveryAttemptsLeft = recoveryAttemptsLeft;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("recoveryAccount", recoveryAccount)
        .add("recoveryAttemptsRemain", recoveryAttemptsLeft);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
