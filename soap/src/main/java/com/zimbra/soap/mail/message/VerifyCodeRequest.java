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
 * @zm-api-command-description Validate the verification code sent to a device. After successful
 *     validation the server sets the device email address as the value of
 *     zimbraCalendarReminderDeviceEmail account attribute.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_VERIFY_CODE_REQUEST)
public class VerifyCodeRequest {

  /**
   * @zm-api-field-tag device-email-address
   * @zm-api-field-description Device email address
   */
  @XmlAttribute(name = MailConstants.A_ADDRESS /* a */, required = false)
  private String address;

  /**
   * @zm-api-field-tag verification-code
   * @zm-api-field-description Verification code
   */
  @XmlAttribute(name = MailConstants.A_VERIFICATION_CODE /* code */, required = false)
  private String verificationCode;

  public VerifyCodeRequest() {
    this(null, null);
  }

  public VerifyCodeRequest(String address, String verificationCode) {
    setAddress(address);
    setVerificationCode(verificationCode);
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setVerificationCode(String verificationCode) {
    this.verificationCode = verificationCode;
  }

  public String getAddress() {
    return address;
  }

  public String getVerificationCode() {
    return verificationCode;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("address", address).add("verificationCode", verificationCode);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
