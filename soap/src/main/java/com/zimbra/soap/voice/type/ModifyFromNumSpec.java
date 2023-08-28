// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.VoiceConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ModifyFromNumSpec {

    /**
     * @zm-api-field-tag old-phone-number
     * @zm-api-field-description Old phone number
     */
    @XmlAttribute(name=VoiceConstants.A_OLD_PHONE /* oldPhone */, required=true)
    private String oldPhoneNum;

    /**
     * @zm-api-field-tag new-phone-number
     * @zm-api-field-description New phone number
     */
    @XmlAttribute(name=VoiceConstants.A_PHONE /* phone */, required=true)
    private String newPhoneNum;

    /**
     * @zm-api-field-tag phone-id
     * @zm-api-field-description Phone ID
     */
    @XmlAttribute(name=VoiceConstants.A_ID /* id */, required=true)
    private String id;

    /**
     * @zm-api-field-tag phone-label
     * @zm-api-field-description Phone label/name
     */
    @XmlAttribute(name=VoiceConstants.A_LABEL /* label */, required=true)
    private String label;

    public ModifyFromNumSpec() {
    }

    public void setOldPhoneNum(String oldPhoneNum) { this.oldPhoneNum = oldPhoneNum; }
    public void setNewPhoneNum(String newPhoneNum) { this.newPhoneNum = newPhoneNum; }
    public void setId(String id) { this.id = id; }
    public void setLabel(String label) { this.label = label; }
    public String getOldPhoneNum() { return oldPhoneNum; }
    public String getNewPhoneNum() { return newPhoneNum; }
    public String getId() { return id; }
    public String getLabel() { return label; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("oldPhoneNum", oldPhoneNum)
            .add("newPhoneNum", newPhoneNum)
            .add("id", id)
            .add("label", label);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
