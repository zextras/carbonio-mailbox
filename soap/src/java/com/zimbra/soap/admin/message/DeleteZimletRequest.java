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
import com.zimbra.soap.type.NamedElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Delete a Zimlet
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DELETE_ZIMLET_REQUEST)
public class DeleteZimletRequest {

    /**
     * @zm-api-field-tag zimlet-name
     * @zm-api-field-description Zimlet name
     */
    @XmlElement(name=AdminConstants.E_ZIMLET, required=true)
    private final NamedElement zimlet;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeleteZimletRequest() {
        this((NamedElement) null);
    }

    public DeleteZimletRequest(NamedElement zimlet) {
        this.zimlet = zimlet;
    }

    public NamedElement getZimlet() { return zimlet; }
}
