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
import com.zimbra.soap.type.AttributeSelectorImpl;
import com.zimbra.soap.admin.type.CosSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Class Of Service (COS)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_COS_REQUEST)
public class GetCosRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-description COS
     */
    @XmlElement(name=AdminConstants.E_COS)
    private CosSelector cos;

    public GetCosRequest() {
    }

    public GetCosRequest(CosSelector cos) {
        this.cos = cos;
    }

    public void setCos(CosSelector cos) {
        this.cos = cos;
    }

    public CosSelector getCos() { return cos; }
}
