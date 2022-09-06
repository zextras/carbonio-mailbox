// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByYearDayRuleInterface;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_BY_YEAR_DAY_RULE, description = "By-year-day rule")
public class ByYearDayRule implements ByYearDayRuleInterface {

  /**
   * @zm-api-field-tag byyearday-yrdaylist
   * @zm-api-field-description BYYEARDAY yearday list. Format : <b>[[+]|-]num[,...]"</b> where num
   *     is between 1 and 366 <br>
   *     e.g. <b>&lt;byyearday yrdaylist="1,+2,-1"/></b> means January 1st, January 2nd, and
   *     December 31st.
   */
  @XmlAttribute(
      name = MailConstants.A_CAL_RULE_BYYEARDAY_YRDAYLIST /* yrdaylist */,
      required = true)
  @GraphQLNonNull
  @GraphQLQuery(
      name = GqlConstants.LIST,
      description =
          "BYYEARDAY yearday list. Format : \"[[+]|-]num[,...]\" where num is between 1 and 366")
  private final String list;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ByYearDayRule() {
    this((String) null);
  }

  public ByYearDayRule(@GraphQLNonNull @GraphQLInputField String list) {
    this.list = list;
  }

  @Override
  public ByYearDayRuleInterface create(String list) {
    return new ByYearDayRule(list);
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
