// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Delete a volume
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DELETE_VOLUME_REQUEST)
public final class DeleteVolumeRequest {

    /**
     * @zm-api-field-tag volume-id
     * @zm-api-field-description Volume ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final short id;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private DeleteVolumeRequest() {
         this((short) -1);
    }

    public DeleteVolumeRequest(short value) {
        id = value;
    }

    public short getId() {
        return id;
    }

}
