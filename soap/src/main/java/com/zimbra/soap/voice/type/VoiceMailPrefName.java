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
public class VoiceMailPrefName {

    /**
     * @zm-api-field-tag preference
     * @zm-api-field-description Preference
     */
    @XmlAttribute(name=VoiceConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private VoiceMailPrefName() {
        this((String) null);
    }

    public VoiceMailPrefName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
