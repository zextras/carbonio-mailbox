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
import com.zimbra.soap.voice.type.PhoneSpec;
import com.zimbra.soap.voice.type.StorePrincipalSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get voice mail preferences.
 * <br />
 * If no <b>&lt;pref></b> elements are provided, all known prefs for the requested phone are returned in the response.
 * If <b>&lt;pref></b> elements are provided, only those prefs are returned in the response.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_GET_VOICE_MAIL_PREFS_REQUEST)
public class GetVoiceMailPrefsRequest {

    /**
     * @zm-api-field-description Store principal specification
     */
    @XmlElement(name=VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required=false)
    private StorePrincipalSpec storePrincipal;

    /**
     * @zm-api-field-description Phone specification
     */
    @XmlElement(name=VoiceConstants.E_PHONE /* phone */, required=false)
    private PhoneSpec phone;

    public GetVoiceMailPrefsRequest() {
    }

    public void setStorePrincipal(StorePrincipalSpec storePrincipal) { this.storePrincipal = storePrincipal; }
    public void setPhone(PhoneSpec phone) { this.phone = phone; }
    public StorePrincipalSpec getStorePrincipal() { return storePrincipal; }
    public PhoneSpec getPhone() { return phone; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("storePrincipal", storePrincipal)
            .add("phone", phone);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
