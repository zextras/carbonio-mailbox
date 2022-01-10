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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_AUTH_CONFIG_RESPONSE)
@XmlType(propOrder = {"code", "message", "bindDn"})
public class CheckAuthConfigResponse {

    /**
     * @zm-api-field-description Code
     */
    @XmlElement(name=AdminConstants.E_CODE, required=true)
    private final String code;
    /**
     * @zm-api-field-description Message
     */
    @XmlElement(name=AdminConstants.E_MESSAGE, required=false)
    private final String message;
    /**
     * @zm-api-field-tag dn-computed-from-supplied-binddn-and-name
     * @zm-api-field-description DN computed from supplied bind DN and name
     */
    @XmlElement(name=AdminConstants.E_BINDDN, required=true)
    private final String bindDn;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CheckAuthConfigResponse() {
        this(null, null, null);
    }

    public CheckAuthConfigResponse(String code, String message, String bindDn) {
        this.code = code;
        this.message = message;
        this.bindDn = bindDn;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getBindDn() { return bindDn; }
}
