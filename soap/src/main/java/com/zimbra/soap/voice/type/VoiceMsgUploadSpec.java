// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.VoiceConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMsgUploadSpec {

    /**
     * @zm-api-field-tag voicemail-id
     * @zm-api-field-description Message id of the voice mail.  It can only be a voice mail in the INBOX, not the
     * trash folder.
     */
    @XmlAttribute(name=AccountConstants.A_ID /* id */, required=true)
    private String voiceMailId;

    /**
     * @zm-api-field-tag phone-number
     * @zm-api-field-description Phone number of the voice mail
     */
    @XmlAttribute(name=VoiceConstants.A_PHONE /* phone */, required=true)
    private String phoneNumber;

    public VoiceMsgUploadSpec() {
    }

    public void setVoiceMailId(String voiceMailId) { this.voiceMailId = voiceMailId; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getVoiceMailId() { return voiceMailId; }
    public String getPhoneNumber() { return phoneNumber; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("voiceMailId", voiceMailId)
            .add("phoneNumber", phoneNumber);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
