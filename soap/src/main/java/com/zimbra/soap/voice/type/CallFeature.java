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
public class CallFeature {

    /**
     * @zm-api-field-tag call-feature
     * @zm-api-field-description Call feature name
     */
    @XmlAttribute(name=VoiceConstants.A_NAME /* name */, required=true)
    private String featureName;

    public CallFeature() {
    }

    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public String getFeatureName() { return featureName; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("featureName", featureName);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
