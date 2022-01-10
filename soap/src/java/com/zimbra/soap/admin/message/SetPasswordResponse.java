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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_SET_PASSWORD_RESPONSE)
public class SetPasswordResponse {

    /**
     * @zm-api-field-tag message
     * @zm-api-field-description If the password had violated any policy, it is returned in this> element, and the
     * password is still set successfully.
     */
    @XmlElement(name=AdminConstants.E_MESSAGE, required=false)
    private final String message;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private SetPasswordResponse() {
        this(null);
    }

    public SetPasswordResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
}
