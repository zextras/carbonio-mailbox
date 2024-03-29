// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Identity;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify an Identity
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_MODIFY_IDENTITY_REQUEST)
public class ModifyIdentityRequest {

    /**
     * @zm-api-field-description Specify identity to be modified
     * Must specify either "name" or "id" attribute
     */
    @XmlElement(name=AccountConstants.E_IDENTITY, required=true)
    private final Identity identity;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ModifyIdentityRequest() {
        this(null);
    }

    public ModifyIdentityRequest(Identity identity) {
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
