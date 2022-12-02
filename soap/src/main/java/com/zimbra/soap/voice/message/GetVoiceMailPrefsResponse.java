// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.voice.type.PhoneInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_GET_VOICE_MAIL_PREFS_RESPONSE)
public class GetVoiceMailPrefsResponse {

    /**
     * @zm-api-field-description Phone information
     */
    @XmlElement(name=VoiceConstants.E_PHONE /* phone */, required=false)
    private PhoneInfo phone;

    public GetVoiceMailPrefsResponse() {
    }

    public void setPhone(PhoneInfo phone) { this.phone = phone; }
    public PhoneInfo getPhone() { return phone; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("phone", phone);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
