// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class VolumeIdAndProgress {

    /**
     * @zm-api-field-tag volumeId
     * @zm-api-field-description volumeId
     */
    @XmlAttribute(name=AdminConstants.A_VOLUME_ID, required=true)
    private final String volumeId;

    /**
     * @zm-api-field-tag progress
     * @zm-api-field-description progress
     */
    @XmlAttribute(name=AdminConstants.A_PROGRESS, required=true)
    private final String progress;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private VolumeIdAndProgress() {
        this((String) null, (String) null);
    }

    public VolumeIdAndProgress(String volumeId, String progress) {
        this.volumeId = volumeId;
        this.progress = progress;
    }

    public String getVolumeId() { return volumeId; }
    public String getProgress() { return progress; }
}
