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
import com.zimbra.soap.voice.type.VoiceFolderInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_GET_VOICE_FOLDER_RESPONSE)
public class GetVoiceFolderResponse {

    /**
     * @zm-api-field-description Information for phones
     */
    @XmlElement(name=VoiceConstants.E_PHONE /* phone */, required=false)
    private List<VoiceFolderInfo> phones = Lists.newArrayList();

    public GetVoiceFolderResponse() {
    }

    public void setPhones(Iterable <VoiceFolderInfo> phones) {
        this.phones.clear();
        if (phones != null) {
            Iterables.addAll(this.phones, phones);
        }
    }

    public void addPhone(VoiceFolderInfo phone) {
        this.phones.add(phone);
    }

    public List<VoiceFolderInfo> getPhones() {
        return phones;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("phones", phones);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
