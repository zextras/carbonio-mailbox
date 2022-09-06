// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.voice.type.PhoneVoiceFeaturesSpec;
import com.zimbra.soap.voice.type.StorePrincipalSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Call features of a phone <br>
 *     Only features requested in <b>&lt;{call-feature}/></b> are returned in the response. <br>
 *     At least one feature has to be specified. This is because the velodrome gateway returns only
 *     partial data if features are not specifically requested. Therefore for now we do not support
 *     the "want all" (i.e. no <b>&lt;{call-feature}></b>) request.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = VoiceConstants.E_GET_VOICE_FEATURES_REQUEST)
public class GetVoiceFeaturesRequest {

  /**
   * @zm-api-field-description Store Principal specification
   */
  @XmlElement(name = VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required = false)
  private StorePrincipalSpec storePrincipal;

  /**
   * @zm-api-field-description Phone
   */
  @XmlElement(name = VoiceConstants.E_PHONE /* phone */, required = false)
  private PhoneVoiceFeaturesSpec phone;

  public GetVoiceFeaturesRequest() {}

  public void setStorePrincipal(StorePrincipalSpec storePrincipal) {
    this.storePrincipal = storePrincipal;
  }

  public void setPhone(PhoneVoiceFeaturesSpec phone) {
    this.phone = phone;
  }

  public StorePrincipalSpec getStorePrincipal() {
    return storePrincipal;
  }

  public PhoneVoiceFeaturesSpec getPhone() {
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
