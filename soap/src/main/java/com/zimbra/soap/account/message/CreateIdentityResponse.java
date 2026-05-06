// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Identity;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_CREATE_IDENTITY_RESPONSE)
public class CreateIdentityResponse {

    /**
     * @zm-api-field-description Information about created identity
     */
    @XmlElement(name=AccountConstants.E_IDENTITY, required=true)
    private final Identity identity;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateIdentityResponse() {
        this(null);
    }

    public CreateIdentityResponse(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() { return identity; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("identity", identity)
            .toString();
    }
}
