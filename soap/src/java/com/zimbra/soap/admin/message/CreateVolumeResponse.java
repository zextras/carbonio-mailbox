// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.VolumeInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_VOLUME_RESPONSE)
public class CreateVolumeResponse {

    /**
     * @zm-api-field-description Information about the newly created volume
     */
    @XmlElement(name=AdminConstants.E_VOLUME, required=true)
    private VolumeInfo volume;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private CreateVolumeResponse() {
        this((VolumeInfo)null);
    }

    public CreateVolumeResponse(VolumeInfo volume) {
        this.volume = volume;
    }

    public VolumeInfo getVolume() { return volume; }
}
