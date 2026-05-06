// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.Attr;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Config request
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_CONFIG_REQUEST)
@XmlType(propOrder = {})
public class GetConfigRequest {

    /**
     * @zm-api-field-description Attribute
     */
    @XmlElement(name=AdminConstants.E_A, required=false)
    private Attr attr;

    public GetConfigRequest() {
    }

    public void setAttr(Attr attr) { this.attr = attr; }

    public Attr getAttr() { return attr; }
}
