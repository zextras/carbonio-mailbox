// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.GalSearchType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Perform an autocomplete for a name against the Global Address List
 * <p>
 * Notes: admin verison of mail equiv. Used for testing via zmprov.
 * </p>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_AUTO_COMPLETE_GAL_REQUEST)
public class AutoCompleteGalRequest {

    /**
     * @zm-api-field-description domain
     */
    @XmlAttribute(name=AdminConstants.A_DOMAIN /* domain */, required=true)
    private String domain;

    /**
     * @zm-api-field-description The name to test for autocompletion
     */
    @XmlAttribute(name=AccountConstants.E_NAME /* name */, required=true)
    private String name;

    /**
     * @zm-api-field-description Type of addresses to auto-complete on
     * <ul>
     * <li>     "account" for regular user accounts, aliases and distribution lists
     * <li>     "resource" for calendar resources
     * <li>     "group" for groups
     * <li>     "all" for combination of types
     * </ul>
     * if omitted, defaults to "accounts"
     */
    @XmlAttribute(name=AccountConstants.A_TYPE /* type */, required=false)
    private GalSearchType type;

    // TODO: is this appropriate for AutoCompleteGal?
    /**
     * @zm-api-field-tag gal-account-id
     * @zm-api-field-description GAL Account ID
     */
    @XmlAttribute(name=AccountConstants.A_GAL_ACCOUNT_ID /* galAcctId */, required=false)
    private String galAccountId;

    /**
     * @zm-api-field-tag limit
     * @zm-api-field-description An integer specifying the maximum number of results to return
     */
    @XmlAttribute(name=MailConstants.A_QUERY_LIMIT /* limit */, required=false)
    private Integer limit;

    /**
     * no-argument constructor wanted by JAXB
     */
    private AutoCompleteGalRequest() {
        this(null, null);
    }

    private AutoCompleteGalRequest(String domain, String name) {
        this.setDomain(domain);
        this.name = name;
    }

    public AutoCompleteGalRequest createForDomainAndName(String domain, String name) {
        return new AutoCompleteGalRequest(domain, name);
    }

    public void setDomain(String domain) { this.domain = domain; }
    public void setName(String name) {this.name = name; }
    public void setType(GalSearchType type) { this.type = type; }
    public void setGalAccountId(String galAccountId) { this.galAccountId = galAccountId; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public String getDomain() { return domain; }
    public String getName() { return name; }
    public GalSearchType getType() { return type; }
    public String getGalAccountId() { return galAccountId; }
    public Integer getLimit() { return limit; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("domain", domain)
            .add("name", name)
            .add("type", type)
            .add("galAccountId", galAccountId)
            .add("limit", limit);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
