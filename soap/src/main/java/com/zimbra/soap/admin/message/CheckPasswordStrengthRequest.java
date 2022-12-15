// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check password strength
 * <br />
 * <b>Access</b>: domain admin sufficient
 * <br />
 * note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_PASSWORD_STRENGTH_REQUEST)
@XmlType(propOrder = {})
public class CheckPasswordStrengthRequest {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.E_ID, required=true)
    private String id;
    /**
     * @zm-api-field-tag password
     * @zm-api-field-description Passowrd to check
     */
    @XmlAttribute(name=AdminConstants.E_PASSWORD, required=true)
    private String password;

    /**
     * no-argument constructor wanted by JAXB
     */
    public CheckPasswordStrengthRequest() {
    }

    public CheckPasswordStrengthRequest(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
}
