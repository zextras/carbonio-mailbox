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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Create a volume
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_VOLUME_REQUEST)
public class CreateVolumeRequest {

    /**
     * @zm-api-field-description Volume information
     */
    @XmlElement(name=AdminConstants.E_VOLUME, required=true)
    private VolumeInfo volume;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private CreateVolumeRequest() {
        this((VolumeInfo)null);
    }

    public CreateVolumeRequest(VolumeInfo volume) {
        this.volume = volume;
    }

    public VolumeInfo getVolume() { return volume; }
}
