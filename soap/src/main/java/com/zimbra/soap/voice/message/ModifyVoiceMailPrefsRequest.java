// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.voice.type.PhoneInfo;
import com.zimbra.soap.voice.type.StorePrincipalSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify voice mail preferences.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = VoiceConstants.E_MODIFY_VOICE_MAIL_PREFS_REQUEST)
public class ModifyVoiceMailPrefsRequest {

  /**
   * @zm-api-field-description Store principal specification
   */
  @XmlElement(name = VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required = false)
  private StorePrincipalSpec storePrincipal;

  /**
   * @zm-api-field-description New Preferences information
   */
  @XmlElement(name = VoiceConstants.E_PHONE /* phone */, required = false)
  private PhoneInfo phone;

  public ModifyVoiceMailPrefsRequest() {}

  public void setStorePrincipal(StorePrincipalSpec storePrincipal) {
    this.storePrincipal = storePrincipal;
  }

  public void setPhone(PhoneInfo phone) {
    this.phone = phone;
  }

  public StorePrincipalSpec getStorePrincipal() {
    return storePrincipal;
  }

  public PhoneInfo getPhone() {
    return phone;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("storePrincipal", storePrincipal).add("phone", phone);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
