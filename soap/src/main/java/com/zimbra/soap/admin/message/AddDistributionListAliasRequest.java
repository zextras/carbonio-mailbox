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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Add an alias for a distribution list
 * <br />
 * Access: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_ADD_DISTRIBUTION_LIST_ALIAS_REQUEST)
public class AddDistributionListAliasRequest {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.E_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag alias
     * @zm-api-field-description Alias
     */
    @XmlAttribute(name=AdminConstants.E_ALIAS /* alias */, required=true)
    private final String alias;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AddDistributionListAliasRequest() {
        this((String)null, (String)null);
    }

    public AddDistributionListAliasRequest(String id, String alias) {
        this.id = id;
        this.alias = alias;
    }

    public String getId() { return id; }
    public String getAlias() { return alias; }
}
