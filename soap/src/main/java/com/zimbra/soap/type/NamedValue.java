// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AdminConstants;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_NAMED_VALUE, description = "attribute names and values")
public class NamedValue {

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = true)
  @GraphQLNonNull
  @GraphQLQuery(name = GqlConstants.NAME, description = "name of the attribute")
  private final String name;

  /**
   * @zm-api-field-tag value
   * @zm-api-field-description Value
   */
  @XmlValue
  @GraphQLQuery(name = GqlConstants.VALUE, description = "value of the attribute")
  private final String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private NamedValue() {
    this((String) null, (String) null);
  }

  public NamedValue(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("name", name).add("value", value);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
