// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_REVOKE_OAUTH_CONSUMER_REQUEST)
public class RevokeOAuthConsumerRequest {

    @XmlAttribute(name=AccountConstants.A_ACCESS_TOKEN, required=true)
    private String accessToken;

    public RevokeOAuthConsumerRequest() {}

    public RevokeOAuthConsumerRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}
