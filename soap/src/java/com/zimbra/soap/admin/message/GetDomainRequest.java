// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AttributeSelectorImpl;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get information about a domain
 */
@XmlRootElement(name=AdminConstants.E_GET_DOMAIN_REQUEST)
public class GetDomainRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-tag apply-config
     * @zm-api-field-description If <b>{apply-config}</b> is <b>1 (true)</b>, then certain unset attrs on a domain
     * will get their values from the global config.
     * <br />
     * if <b>{apply-config}</b> is <b>0 (false)</b>, then only attributes directly set on the domain will be returned
     */
    @XmlAttribute(name=AdminConstants.A_APPLY_CONFIG, required=false)
    private ZmBoolean applyConfig = ZmBoolean.ONE /* true */;

    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_DOMAIN)
    private DomainSelector domain;

    public GetDomainRequest() {
        this(null, null);
    }

    public GetDomainRequest(DomainSelector domain, Boolean applyConfig) {
        setDomain(domain);
        setApplyConfig(applyConfig);
    }

    void setDomain(DomainSelector domain) {
        this.domain = domain;
    }

    public void setApplyConfig(Boolean applyConfig) {
        this.applyConfig = ZmBoolean.fromBool(applyConfig);
    }

    public DomainSelector getDomain() { return domain; }
    public Boolean isApplyConfig() { return ZmBoolean.toBool(applyConfig); }
}
