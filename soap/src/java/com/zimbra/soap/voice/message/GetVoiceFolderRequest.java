// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

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
 * @zm-api-command-description Get Voice Folders
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_GET_VOICE_FOLDER_REQUEST)
public class GetVoiceFolderRequest {

    /**
     * @zm-api-field-description Store principal specification
     */
    @XmlElement(name=VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required=false)
    private StorePrincipalSpec storePrincipal;

    /**
     * @zm-api-field-description Phone specification
     */
    @XmlElement(name=VoiceConstants.E_PHONE /* phone */, required=false)
    private List<PhoneSpec> phones = Lists.newArrayList();

    public GetVoiceFolderRequest() {
    }

    public void setStorePrincipal(StorePrincipalSpec storePrincipal) { this.storePrincipal = storePrincipal; }
    public void setPhones(Iterable <PhoneSpec> phones) {
        this.phones.clear();
        if (phones != null) {
            Iterables.addAll(this.phones, phones);
        }
    }

    public void addPhone(PhoneSpec phone) {
        this.phones.add(phone);
    }

    public StorePrincipalSpec getStorePrincipal() { return storePrincipal; }
    public List<PhoneSpec> getPhones() {
        return phones;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("storePrincipal", storePrincipal)
            .add("phones", phones);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
