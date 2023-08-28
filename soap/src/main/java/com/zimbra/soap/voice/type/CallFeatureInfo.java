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
import com.zimbra.soap.type.TrueOrFalse;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class CallFeatureInfo {

    /**
     * @zm-api-field-tag subscribed-true|false
     * @zm-api-field-description Flag whether subscribed or not - "true" or "false"
     */
    @XmlAttribute(name=VoiceConstants.A_SUBSCRIBED /* s */, required=true)
    private TrueOrFalse subscribed;

    /**
     * @zm-api-field-tag active-true|false
     * @zm-api-field-description Flag whether active or not - "true" or "false"
     */
    @XmlAttribute(name=VoiceConstants.A_ACTIVE /* a */, required=true)
    private TrueOrFalse active;

    public CallFeatureInfo() {
    }

    public void setSubscribed(TrueOrFalse subscribed) { this.subscribed = subscribed; }
    public void setActive(TrueOrFalse active) { this.active = active; }
    public TrueOrFalse getSubscribed() { return subscribed; }
    public TrueOrFalse getActive() { return active; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("subscribed", subscribed)
            .add("active", active);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
