// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;


/*
<preauth timestamp="{timestamp}" expires="{expires}">{computed-preauth-value}</preauth>
 */
public class PreAuth {

    /**
     * @zm-api-field-description Time stamp
     */
    @XmlAttribute(required=true)
    private long timestamp;
    /**
     * @zm-api-field-tag expires
     * @zm-api-field-description expiration time of the authtoken, in milliseconds. set to 0 to use the default
     * expiration time for the account. Can be used to sync the auth token expiration time with the external system's
     * notion of expiration (like a Kerberos TGT lifetime, for example).
     */
    @XmlAttribute
    private Long expires;
    /**
     * @zm-api-field-tag computed-preauth-value
     * @zm-api-field-description Computed preauth value
     */
    @XmlValue
    private String value;

    public long getTimestamp() { return timestamp; }
    public PreAuth setTimestamp(long timestamp) { this.timestamp = timestamp; return this; }

    public Long getExpires() { return expires; }
    public PreAuth setExpires(Long timestamp) { this.expires = timestamp; return this; }
    /**
     * Gets the expiration for this PreAuth
     * @deprecated Use the `getExpires` method
     */
    @Deprecated
    public Long getExpiresTimestamp() { return getExpires(); }
    /**
     * Sets the expiration for this PreAuth
     * @deprecated Use the `setExpires` method
     */
    @Deprecated
    public PreAuth setExpiresTimestamp(Long timestamp) { return setExpires(timestamp); }

    public String getValue() { return value; }
    public PreAuth setValue(String value) { this.value = value; return this; }
}
