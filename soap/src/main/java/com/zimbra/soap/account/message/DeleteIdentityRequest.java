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
import com.zimbra.soap.account.type.NameId;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Delete an Identity
 * <p>
 * must specify either <b>{name}</b> or <b>{id}</b> attribute to <b>&lt;identity></b>
 * </p>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_DELETE_IDENTITY_REQUEST)
public class DeleteIdentityRequest {

    /**
     * @zm-api-field-description Details of the identity to delete.
     */
    @ZimbraUniqueElement
    @XmlElement(name=AccountConstants.E_IDENTITY /* identity */, required=true)
    private final NameId identity;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeleteIdentityRequest() {
        this((NameId) null);
    }

    public DeleteIdentityRequest(NameId identity) {
        this.identity = identity;
    }

    public NameId getIdentity() { return identity; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("identity", identity)
            .toString();
    }
}
