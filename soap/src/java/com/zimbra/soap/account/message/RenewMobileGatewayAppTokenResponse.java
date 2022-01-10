// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.AuthToken;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AccountConstants.E_RENEW_MOBILE_GATEWAY_APP_TOKEN_RESPONSE)
public class RenewMobileGatewayAppTokenResponse {

    /**
     * @zm-api-field-tag auth-token
     * @zm-api-field-description app account auth token
     */
    @ZimbraUniqueElement
    @XmlElement(name = AccountConstants.E_AUTH_TOKEN, required = true)
    private AuthToken authToken;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RenewMobileGatewayAppTokenResponse() {
        this(null);
    }

    public RenewMobileGatewayAppTokenResponse(AuthToken authToken) {
        this.authToken = authToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper.
                add("authToken", authToken);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
