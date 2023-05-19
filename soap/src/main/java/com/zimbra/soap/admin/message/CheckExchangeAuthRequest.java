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
import com.zimbra.soap.admin.type.ExchangeAuthSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check Exchange Authorisation
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_EXCHANGE_AUTH_REQUEST)
public class CheckExchangeAuthRequest {

    /**
     * @zm-api-field-description Exchange Auth details
     */
    @XmlElement(name=AdminConstants.E_AUTH, required=true)
    private final ExchangeAuthSpec auth;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CheckExchangeAuthRequest() {
        this(null);
    }

    public CheckExchangeAuthRequest(ExchangeAuthSpec auth) {
        this.auth = auth;
    }

    public ExchangeAuthSpec getAuth() { return auth; }
}
