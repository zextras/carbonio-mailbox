// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import com.zimbra.soap.mail.type.FilterRule;
import com.zimbra.soap.type.AccountSelector;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_FILTER_RULES_RESPONSE)
public class GetFilterRulesResponse {
    /**
     * @zm-api-field-tag type
     * @zm-api-field-description Type can be either before or after
     */
    @XmlAttribute(name=AdminConstants.A_TYPE /* type */, required=true)
    protected String type;
    /**
     * @zm-api-field-description Account
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT /* account */, required=false)
    protected AccountSelector account;
    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_DOMAIN /* domain */, required=false)
    protected DomainSelector domain;
    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_COS /* cos */, required=false)
    protected CosSelector cos;
    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_SERVER /* server */, required=false)
    protected ServerSelector server;

    /**
     * @zm-api-field-description Filter rules
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_FILTER_RULES /* filterRules */, required=true)
    @XmlElement(name=AdminConstants.E_FILTER_RULE /* filterRule */, required=false)
    protected final List<FilterRule> rules = Lists.newArrayList();

    public GetFilterRulesResponse() {
    }

    public GetFilterRulesResponse(String type) {
        this.type = type;
        this.account = null;
        this.domain = null;
        this.cos = null;
        this.server = null;
    }

    public GetFilterRulesResponse(String type, AccountSelector accountSelector) {
        this.type = type;
        this.account = accountSelector;
        this.domain = null;
        this.cos = null;
        this.server = null;
    }

    public GetFilterRulesResponse(String type, DomainSelector domainSelector) {
        this.type = type;
        this.account = null;
        this.domain = domainSelector;
        this.cos = null;
        this.server = null;
    }

    public GetFilterRulesResponse(String type, CosSelector cosSelector) {
        this.type = type;
        this.account = null;
        this.domain = null;
        this.cos = cosSelector;
        this.server = null;
    }

    public GetFilterRulesResponse(String type, ServerSelector serverSelector) {
        this.type = type;
        this.account = null;
        this.domain = null;
        this.cos = null;
        this.server = serverSelector;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFilterRules(Collection<FilterRule> list) {
        rules.clear();
        if (list != null) {
            rules.addAll(list);
        }
    }

    public void addFilterRule(FilterRule rule) {
        rules.add(rule);
    }

    public void addFilterRules(Collection<FilterRule> list) {
        rules.addAll(list);
    }

    public List<FilterRule> getFilterRules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("rules", rules).toString();
    }
}
