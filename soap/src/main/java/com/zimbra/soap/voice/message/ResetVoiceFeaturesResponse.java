// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.voice.type.PhoneName;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = VoiceConstants.E_RESET_VOICE_FEATURES_RESPONSE)
public class ResetVoiceFeaturesResponse {

  /**
   * @zm-api-field-description Phone
   */
  @XmlElement(name = VoiceConstants.E_PHONE /* phone */, required = false)
  private PhoneName phone;

  public ResetVoiceFeaturesResponse() {}

  public void setPhone(PhoneName phone) {
    this.phone = phone;
  }

  public PhoneName getPhone() {
    return phone;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("phone", phone);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
