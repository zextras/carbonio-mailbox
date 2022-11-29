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
 * @zm-api-command-description Rename Account
 * <br />
 * <b>Access</b>: domain admin sufficient
 * <br />
 * note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_RENAME_ACCOUNT_REQUEST)
@XmlType(propOrder = {})
public class RenameAccountRequest {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-tag new-account-name
     * @zm-api-field-description New account name
     */
    @XmlAttribute(name=AdminConstants.E_NEW_NAME, required=true)
    private final String newName;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RenameAccountRequest() {
        this(null, null);
    }

    public RenameAccountRequest(String id, String newName) {
        this.id = id;
        this.newName = newName;
    }

    public String getId() { return id; }
    public String getNewName() { return newName; }
}
