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
public class ModifyVoiceMailPinSpec {

    /**
     * @zm-api-field-tag phone-name
     * @zm-api-field-description Phone name
     */
    @XmlAttribute(name=AccountConstants.A_NAME /* name */, required=false)
    private String name;

    /**
     * @zm-api-field-tag old-pin
     * @zm-api-field-description Old PIN
     */
    @XmlAttribute(name=VoiceConstants.A_VMAIL_OLD_PIN /* oldPin */, required=true)
    private String oldVoiceMailPin;

    /**
     * @zm-api-field-tag new-pin
     * @zm-api-field-description New PIN
     */
    @XmlAttribute(name=VoiceConstants.A_VMAIL_PIN /* pin */, required=true)
    private String newVoiceMailPin;

    public ModifyVoiceMailPinSpec() {
    }

    public void setName(String name) { this.name = name; }
    public void setOldVoiceMailPin(String oldVoiceMailPin) { this.oldVoiceMailPin = oldVoiceMailPin; }
    public void setNewVoiceMailPin(String newVoiceMailPin) { this.newVoiceMailPin = newVoiceMailPin; }
    public String getName() { return name; }
    public String getOldVoiceMailPin() { return oldVoiceMailPin; }
    public String getNewVoiceMailPin() { return newVoiceMailPin; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("oldVoiceMailPin", oldVoiceMailPin)
            .add("newVoiceMailPin", newVoiceMailPin);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
