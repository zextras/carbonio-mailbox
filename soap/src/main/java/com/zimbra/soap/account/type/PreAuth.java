// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/*
<preauth timestamp="{timestamp}" expires="{expires}">{computed-preauth-value}</preauth>
 */
@GraphQLType(name = "PreAuth", description = "PreAuth token")
public class PreAuth {

  /**
   * @zm-api-field-description Time stamp
   */
  @XmlAttribute(required = true)
  @GraphQLNonNull
  @GraphQLQuery(name = "timestamp", description = "Time stamp")
  private long timestamp;
  /**
   * @zm-api-field-tag expires
   * @zm-api-field-description expiration time of the authtoken, in milliseconds. set to 0 to use
   *     the default expiration time for the account. Can be used to sync the auth token expiration
   *     time with the external system's notion of expiration (like a Kerberos TGT lifetime, for
   *     example).
   */
  @XmlAttribute
  @GraphQLQuery(
      name = "expires",
      description =
          "Expiration time of the auth token, in milliseconds. Set to 0 to use the default for the"
              + " account.")
  private Long expires;
  /**
   * @zm-api-field-tag computed-preauth-value
   * @zm-api-field-description Computed preauth value
   */
  @XmlValue
  @GraphQLQuery(name = "value", description = "Computed preauth value")
  private String value;

  @GraphQLNonNull
  @GraphQLQuery(name = "timestamp", description = "Time stamp")
  public long getTimestamp() {
    return timestamp;
  }

  @GraphQLInputField(name = "timestamp", description = "Time stamp")
  public PreAuth setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  @GraphQLQuery(
      name = "expires",
      description =
          "Expiration time of the auth token, in milliseconds. Set to 0 to use the default for the"
              + " account.")
  public Long getExpires() {
    return expires;
  }

  @GraphQLInputField(
      name = "expires",
      description =
          "Expiration time of the auth token, in milliseconds. Set to 0 to use the default for the"
              + " account.")
  public PreAuth setExpires(Long timestamp) {
    this.expires = timestamp;
    return this;
  }
  /**
   * Gets the expiration for this PreAuth
   *
   * @deprecated Use the `getExpires` method
   */
  @Deprecated
  @GraphQLIgnore
  public Long getExpiresTimestamp() {
    return getExpires();
  }
  /**
   * Sets the expiration for this PreAuth
   *
   * @deprecated Use the `setExpires` method
   */
  @Deprecated
  @GraphQLIgnore
  public PreAuth setExpiresTimestamp(Long timestamp) {
    return setExpires(timestamp);
  }

  @GraphQLQuery(name = "value", description = "Computed preauth value")
  public String getValue() {
    return value;
  }

  @GraphQLInputField(name = "value", description = "Computed preauth value")
  public PreAuth setValue(String value) {
    this.value = value;
    return this;
  }
}
