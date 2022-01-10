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
import com.zimbra.soap.voice.type.ModifyVoiceFeaturesSpec;
import com.zimbra.soap.voice.type.StorePrincipalSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify call features of a phone.
 * <br />
 * Refer to <b>GetVoiceFeaturesResponse</b> for attributes and child elements of each call feature
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_MODIFY_VOICE_FEATURES_REQUEST)
public class ModifyVoiceFeaturesRequest {

    /**
     * @zm-api-field-description Store Principal specification
     */
    @XmlElement(name=VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required=false)
    private StorePrincipalSpec storePrincipal;

    /**
     * @zm-api-field-description Specification of voice features to be modified
     */
    @XmlElement(name=VoiceConstants.E_PHONE /* phone */, required=false)
    private ModifyVoiceFeaturesSpec phone;

    public ModifyVoiceFeaturesRequest() {
    }

    public void setStorePrincipal(StorePrincipalSpec storePrincipal) { this.storePrincipal = storePrincipal; }
    public void setPhone(ModifyVoiceFeaturesSpec phone) { this.phone = phone; }
    public StorePrincipalSpec getStorePrincipal() { return storePrincipal; }
    public ModifyVoiceFeaturesSpec getPhone() { return phone; }

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
