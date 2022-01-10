// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Copy Class of service (COS)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_COPY_COS_REQUEST)
@XmlType(propOrder = {})
public class CopyCosRequest {

    /**
     * @zm-api-field-tag dest-cos-name
     * @zm-api-field-description Destination name for COS
     */
    @XmlElement(name=AdminConstants.E_NAME)
    private String newName;

    /**
     * @zm-api-field-description Source COS
     */
    @XmlElement(name=AdminConstants.E_COS)
    private CosSelector cos;

    public CopyCosRequest() {
    }

    public CopyCosRequest(CosSelector cos, String newName) {
        this.newName = newName;
        this.cos = cos;
    }

    public void setNewName(String name) {
        this.newName = name;
    }

    public void setCos(CosSelector cos) {
        this.cos = cos;
    }

    public String getNewName() { return newName; }
    public CosSelector getCos() { return cos; }
}
