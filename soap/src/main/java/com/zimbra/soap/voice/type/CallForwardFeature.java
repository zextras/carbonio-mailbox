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
public class CallForwardFeature extends CallFeatureInfo {

    /**
     * @zm-api-field-tag forward-to
     * @zm-api-field-description Telephone number to forward calls to
     */
    @XmlAttribute(name=VoiceConstants.A_FORWARD_TO /* ft */, required=false)
    private String forwardTo;

    public CallForwardFeature() {
    }

    public void setForwardTo(String forwardTo) { this.forwardTo = forwardTo; }
    public String getForwardTo() { return forwardTo; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("forwardTo", forwardTo);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
