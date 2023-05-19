// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.type.NamedElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get a configuration for SMIME public key lookup via external LDAP on a domain or
 * globalconfig
 * <br />
 * Notes: if <b>&lt;domain></b> is present, get the config on the domain, otherwise get the config on globalconfig.
 * @zm-api-command-network-edition
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_SMIME_CONFIG_REQUEST)
public class GetSMIMEConfigRequest {

    /**
     * @zm-api-field-description Config
     */
    @XmlElement(name=AdminConstants.E_CONFIG /* config */, required=false)
    private final NamedElement config;

    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_DOMAIN /* domain */, required=false)
    private final DomainSelector domain;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetSMIMEConfigRequest() {
        this(null, null);
    }

    public GetSMIMEConfigRequest(NamedElement config, DomainSelector domain) {
        this.config = config;
        this.domain = domain;
    }

    public NamedElement getConfig() { return config; }
    public DomainSelector getDomain() { return domain; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("config", config)
            .add("domain", domain);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
