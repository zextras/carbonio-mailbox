// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns all data sources defined for the given mailbox.  For each data source, every
 * attribute value is returned except password.
 * <br />
 * Note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_DATA_SOURCES_REQUEST)
public class GetDataSourcesRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag existing-account-id
     * @zm-api-field-description Account ID for an existing account
     */
    @XmlAttribute(name=AdminConstants.E_ID, required=true)
    private final String id;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetDataSourcesRequest() {
        this((String) null);
    }

    public GetDataSourcesRequest(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}
