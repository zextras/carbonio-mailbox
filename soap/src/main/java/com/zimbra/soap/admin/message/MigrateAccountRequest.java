// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.IdAndAction;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Migrate an account
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MIGRATE_ACCOUNT_REQUEST)
public class MigrateAccountRequest {

    /**
     * @zm-api-field-description Specification for the migration
     */
    @XmlElement(name=AdminConstants.E_MIGRATE, required=true)
    private final IdAndAction migrate;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MigrateAccountRequest() {
        this(null);
    }

    public MigrateAccountRequest(IdAndAction migrate) {
        this.migrate = migrate;
    }

    public IdAndAction getMigrate() { return migrate; }
}
