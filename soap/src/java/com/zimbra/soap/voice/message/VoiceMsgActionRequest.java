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
import com.zimbra.soap.voice.type.StorePrincipalSpec;
import com.zimbra.soap.voice.type.VoiceMsgActionSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Perform an action on a voice message
 * <ul>
 * <li> Modify state of voice messages </li>
 * <li> soft delete/undelete voice messages </li>
 * <li> empty (hard-delete) voice message trash folders </li>
 * </ul>

 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_VOICE_MSG_ACTION_REQUEST)
public class VoiceMsgActionRequest {

    /**
     * @zm-api-field-description Store principal specification
     */
    @XmlElement(name=VoiceConstants.E_STOREPRINCIPAL /* storeprincipal */, required=false)
    private StorePrincipalSpec storePrincipal;

    /**
     * @zm-api-field-description Action specification
     */
    @XmlElement(name=MailConstants.E_ACTION /* action */, required=true)
    private VoiceMsgActionSpec action;

    public VoiceMsgActionRequest() {
    }

    public void setStorePrincipal(StorePrincipalSpec storePrincipal) { this.storePrincipal = storePrincipal; }
    public void setAction(VoiceMsgActionSpec action) { this.action = action; }
    public StorePrincipalSpec getStorePrincipal() { return storePrincipal; }
    public VoiceMsgActionSpec getAction() { return action; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("storePrincipal", storePrincipal)
            .add("action", action);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
