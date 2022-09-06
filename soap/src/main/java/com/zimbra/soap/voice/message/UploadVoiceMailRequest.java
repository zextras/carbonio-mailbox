// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.voice.type.StorePrincipalSpec;
import com.zimbra.soap.voice.type.VoiceMsgUploadSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Retrieve the voice mail body from the gateway and upload(save) it as
 *     an attachment on the server.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = VoiceConstants.E_UPLOAD_VOICE_MAIL_REQUEST)
public class UploadVoiceMailRequest {

  /**
   * @zm-api-field-description Store Principal specification
   */
  @XmlElement(name = VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required = false)
  private StorePrincipalSpec storePrincipal;

  /**
   * @zm-api-field-description Specification of voice message to upload
   */
  @XmlElement(name = VoiceConstants.E_VOICEMSG /* vm */, required = false)
  private VoiceMsgUploadSpec voiceMsg;

  public UploadVoiceMailRequest() {}

  public void setStorePrincipal(StorePrincipalSpec storePrincipal) {
    this.storePrincipal = storePrincipal;
  }

  public void setVoiceMsg(VoiceMsgUploadSpec voiceMsg) {
    this.voiceMsg = voiceMsg;
  }

  public StorePrincipalSpec getStorePrincipal() {
    return storePrincipal;
  }

  public VoiceMsgUploadSpec getVoiceMsg() {
    return voiceMsg;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("storePrincipal", storePrincipal).add("voiceMsg", voiceMsg);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
