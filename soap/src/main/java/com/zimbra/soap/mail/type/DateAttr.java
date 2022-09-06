// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.DateAttrInterface;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_DATE_ATTR, description = "Date attr")
public class DateAttr implements DateAttrInterface {

  /**
   * @zm-api-field-tag YYYYMMDDThhmmssZ
   * @zm-api-field-description Date in format : <b>YYYYMMDDThhmmssZ</b>
   */
  @XmlAttribute(name = MailConstants.A_DATE, required = true)
  @GraphQLNonNull
  @GraphQLQuery(name = GqlConstants.DATE, description = "Date in format: YYYYMMDDThhmmssZ")
  private final String date;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DateAttr() {
    this((String) null);
  }

  public DateAttr(@GraphQLNonNull @GraphQLInputField(name = GqlConstants.DATE) String date) {
    this.date = date;
  }

  @Override
  public DateAttrInterface create(String date) {
    return new DateAttr(date);
  }

  @Override
  public String getDate() {
    return date;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("date", date).toString();
  }
}
