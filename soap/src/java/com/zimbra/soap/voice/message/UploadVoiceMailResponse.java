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
import com.zimbra.soap.voice.type.VoiceMsgUploadInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_UPLOAD_VOICE_MAIL_RESPONSE)
public class UploadVoiceMailResponse {

    /**
     * @zm-api-field-description Upload information
     */
    @XmlElement(name=VoiceConstants.E_UPLOAD /* upload */, required=false)
    private VoiceMsgUploadInfo upload;

    public UploadVoiceMailResponse() {
    }

    public void setUpload(VoiceMsgUploadInfo upload) { this.upload = upload; }
    public VoiceMsgUploadInfo getUpload() { return upload; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("upload", upload);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
