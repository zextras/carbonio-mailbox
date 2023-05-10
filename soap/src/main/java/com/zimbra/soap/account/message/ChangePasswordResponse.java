// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
<ChangePasswordResponse>
   <authToken>...</authToken>
   <lifetime>...</lifetime>
<ChangePasswordResponse/>
 * @zm-api-response-description Note: Returns new authToken, as old authToken will be invalidated on password change.
 */
@XmlRootElement(name=AccountConstants.E_CHANGE_PASSWORD_RESPONSE)
@XmlType(propOrder = {})
public class ChangePasswordResponse {

    /**
     * @zm-api-field-tag new-auth-token
     * @zm-api-field-description New authToken, as old authToken is invalidated on password change.
     */
    @XmlElement(name=AccountConstants.E_AUTH_TOKEN /* authToken */, required=true)
    private String authToken;
    /**
     * @zm-api-field-description Life time associated with <b>{new-auth-token}</b>
     */
    @ZimbraJsonAttribute
    @XmlElement(name=AccountConstants.E_LIFETIME /* lifetime */, required=true)
    private long lifetime;

    public ChangePasswordResponse() {
    }

    public String getAuthToken() { return authToken; }
    public long getLifetime() { return lifetime; }

    public ChangePasswordResponse setAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public ChangePasswordResponse setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }
}
