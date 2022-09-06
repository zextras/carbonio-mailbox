// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_OPERATION_VALUE, description = "Operation value")
public class OpValue {

  /**
   * @zm-api-field-description Operation to apply to an address <br>
   *     <ul>
   *       <li><b>+</b> : add, ignored if the value already exists
   *       <li><b>-</b> : remove, ignored if the value does not exist
   *     </ul>
   *     if not present, replace the entire list with provided values.
   */
  @XmlAttribute(name = AccountConstants.A_OP, required = false)
  @GraphQLInputField(
      name = GqlConstants.OPERATION,
      description =
          "Operation to apply to an address.\n* "
              + "\\+ : add, ignored if the value already exists\n* "
              + "\\- : remove, ignored if the value does not exist\n* "
              + "If not present, replace entire list with provided values.")
  private final String op;

  /**
   * @zm-api-field-description Email address
   */
  @XmlValue
  @GraphQLInputField(name = GqlConstants.VALUE, description = "Email or domain address")
  private final String value;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private OpValue() {
    this((String) null, (String) null);
  }

  public OpValue(
      @GraphQLInputField(name = GqlConstants.OPERATION) String op,
      @GraphQLInputField(name = GqlConstants.VALUE) String value) {
    this.op = op;
    this.value = value;
  }

  public String getOp() {
    return op;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("op", op).add("value", value).toString();
  }
}
