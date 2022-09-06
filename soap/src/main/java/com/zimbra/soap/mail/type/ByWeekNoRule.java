// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ByWeekNoRuleInterface;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_BY_WEEK_NO_RULE, description = "By-week-no rule")
public class ByWeekNoRule implements ByWeekNoRuleInterface {

  /**
   * @zm-api-field-tag byweekno-wklist
   * @zm-api-field-description BYWEEKNO Week list. Format : <b>[[+]|-]num[,...]</b> where num is
   *     between 1 and 53 <br>
   *     e.g. <b>&lt;byweekno wklist="1,+2,-1"/></b> means first week, 2nd week, and last week of
   *     the year.
   */
  @XmlAttribute(name = MailConstants.A_CAL_RULE_BYWEEKNO_WKLIST /* wklist */, required = true)
  @GraphQLNonNull
  @GraphQLQuery(
      name = GqlConstants.LIST,
      description = "BYWEEKNO Week list.  Format : [[+]|-]num[,...] where num is between 1 and 53")
  private final String list;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ByWeekNoRule() {
    this((String) null);
  }

  public ByWeekNoRule(@GraphQLNonNull @GraphQLInputField String list) {
    this.list = list;
  }

  @Override
  public ByWeekNoRuleInterface create(String list) {
    return new ByWeekNoRule(list);
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
