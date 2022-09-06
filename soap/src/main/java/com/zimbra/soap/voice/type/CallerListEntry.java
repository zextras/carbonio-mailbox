// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.type.TrueOrFalse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class CallerListEntry {

  /**
   * @zm-api-field-tag phone-number
   * @zm-api-field-description Caller number from which the call should be forwarded to the
   *     {forward-to} number
   */
  @XmlAttribute(name = VoiceConstants.A_PHONE_NUMBER /* pn */, required = true)
  private String phoneNumber;

  /**
   * @zm-api-field-tag phone-active
   * @zm-api-field-description Flag whether <b>{phone-number}</b> is active in the list - "true" or
   *     "false"
   */
  @XmlAttribute(name = VoiceConstants.A_ACTIVE /* a */, required = true)
  private TrueOrFalse active;

  public CallerListEntry() {}

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setActive(TrueOrFalse active) {
    this.active = active;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public TrueOrFalse getActive() {
    return active;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("phoneNumber", phoneNumber).add("active", active);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
