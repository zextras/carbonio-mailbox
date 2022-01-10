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

import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.voice.type.VoiceMsgActionInfo;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_VOICE_MSG_ACTION_RESPONSE)
public class VoiceMsgActionResponse {

    /**
     * @zm-api-field-description Information on action performed
     */
    @XmlElement(name=MailConstants.E_ACTION /* action */, required=true)
    @ZimbraUniqueElement
    private VoiceMsgActionInfo action;

    public VoiceMsgActionResponse() {
    }

    public void setAction(VoiceMsgActionInfo action) { this.action = action; }
    public VoiceMsgActionInfo getAction() { return action; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("action", action);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
