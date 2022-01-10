// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get all domains
 */
@XmlRootElement(name=AdminConstants.E_GET_ALL_DOMAINS_REQUEST)
public class GetAllDomainsRequest {

    /**
     * @zm-api-field-tag apply-config
     * @zm-api-field-description Apply config flag
     */
    @XmlAttribute(name=AdminConstants.A_APPLY_CONFIG, required=false)
    private final ZmBoolean applyConfig;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetAllDomainsRequest() {
        this(null);
    }

    public GetAllDomainsRequest(Boolean applyConfig) {
        this.applyConfig = ZmBoolean.fromBool(applyConfig);
    }

    public boolean isApplyConfig() { return ZmBoolean.toBool(applyConfig); }
}
