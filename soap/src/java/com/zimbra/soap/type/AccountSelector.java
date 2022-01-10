// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AdminConstants;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name="AccountSelector")
public class AccountSelector {

    /**
     * @zm-api-field-tag acct-selector-by
     * @zm-api-field-description Select the meaning of <b>{acct-selector-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY, required=true)
    @GraphQLNonNull
    @GraphQLQuery(name="accountBy", description="Select the meaning of {acct-selector-key}")
    private final AccountBy accountBy;

    /**
     * @zm-api-field-tag acct-selector-key
     * @zm-api-field-description The key used to identify the account. Meaning determined by <b>{acct-selector-by}</b>
     */
    @XmlValue
    @GraphQLQuery(name="key", description="The key used to identify the account. Meaning determined by {acct-selector-by}")
    private final String key;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AccountSelector() {
        this.accountBy = null;
        this.key = null;
    }

    public AccountSelector(
        @GraphQLInputField @GraphQLNonNull AccountBy by,
        @GraphQLInputField String key) {
        this.accountBy = by;
        this.key = key;
    }

    @GraphQLQuery(name="key", description="The key used to identify the account. Meaning determined by {acct-selector-by}")
    public String getKey() { return key; }

    @GraphQLNonNull
    @GraphQLQuery(name="accountBy", description="Select the meaning of {acct-selector-key}")
    public AccountBy getBy() { return accountBy; }

    public static AccountSelector fromId(String id) {
        return new AccountSelector(AccountBy.id, id);
    }

    public static AccountSelector fromName(String name) {
        return new AccountSelector(AccountBy.name, name);
    }
}
