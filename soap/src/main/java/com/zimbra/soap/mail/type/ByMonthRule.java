// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByMonthRuleInterface;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_BY_MONTH_RULE, description = "By-month rule")
public class ByMonthRule implements ByMonthRuleInterface {

  /**
   * @zm-api-field-tag month-list
   * @zm-api-field-description Comma separated list of months where month is a number between 1 and
   *     12
   */
  @XmlAttribute(name = MailConstants.A_CAL_RULE_BYMONTH_MOLIST /* molist */, required = true)
  @GraphQLNonNull
  @GraphQLQuery(
      name = GqlConstants.LIST,
      description = "Comma separated list of months where month is a number between 1 and 12")
  private final String list;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ByMonthRule() {
    this((String) null);
  }

  public ByMonthRule(@GraphQLNonNull @GraphQLInputField String list) {
    this.list = list;
  }

  @Override
  public ByMonthRuleInterface create(String list) {
    return new ByMonthRule(list);
  }

  @Override
  public String getList() {
    return list;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("list", list).toString();
  }
}
