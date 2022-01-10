// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.type.Id;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Deletes the given data source.
 * <br />
 * Note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DELETE_DATA_SOURCE_REQUEST)
public class DeleteDataSourceRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag existing-account-id
     * @zm-api-field-description Id for an existing Account
     */
    @XmlAttribute(name=AdminConstants.E_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-tag data-source-id
     * @zm-api-field-description Data source ID
     */
    @XmlElement(name=AccountConstants.E_DATA_SOURCE, required=true)
    private final Id dataSource;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeleteDataSourceRequest() {
        this((String) null, (Id) null);
    }

    public DeleteDataSourceRequest(String id, Id dataSource) {
        this.id = id;
        this.dataSource = dataSource;
    }

    public String getId() { return id; }
    public Id getDataSource() { return dataSource; }
}
