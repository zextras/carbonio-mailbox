// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.VolumeInfo;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify volume
 */
@XmlRootElement(name=AdminConstants.E_MODIFY_VOLUME_REQUEST)
public class ModifyVolumeRequest {

    /**
     * @zm-api-field-tag volume-id
     * @zm-api-field-description Volume ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private short id;

    /**
     * @zm-api-field-description Volume information
     */
    @XmlElement(name=AdminConstants.E_VOLUME, required=true)
    private VolumeInfo volume;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private ModifyVolumeRequest() {
        this((short)-1, null);
    }

    public ModifyVolumeRequest(short id, VolumeInfo volume) {
        this.id = id;
        this.volume = volume;
    }

    public short getId() { return id; }
    public VolumeInfo getVolume() { return volume; }
}
