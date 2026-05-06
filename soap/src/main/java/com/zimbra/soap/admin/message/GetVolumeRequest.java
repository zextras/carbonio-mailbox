// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Volume
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_VOLUME_REQUEST)
public final class GetVolumeRequest {

    /**
     * @zm-api-field-tag volume-id
     * @zm-api-field-description ID of volume
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final short id;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private GetVolumeRequest() {
        this((short) -1);
    }

    public GetVolumeRequest(short id) {
        this.id = id;
    }

    public short getId() {
        return id;
    }
}
