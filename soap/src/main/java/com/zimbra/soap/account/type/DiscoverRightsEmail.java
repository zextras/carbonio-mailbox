// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_DISCOVER_RIGHTS_EMAIL, description = "Discover rights email")
public class DiscoverRightsEmail {

  /**
   * @zm-api-field-tag email-address
   * @zm-api-field-description Email address
   */
  @XmlAttribute(name = AccountConstants.A_ADDR /* addr */, required = true)
  private String addr;

  public DiscoverRightsEmail() {
    this(null);
  }

  public DiscoverRightsEmail(String addr) {
    setAddr(addr);
  }

  public void setAddr(String addr) {
    this.addr = addr;
  }

  @GraphQLNonNull
  @GraphQLQuery(name = GqlConstants.ADDR, description = "Email address")
  public String getAddr() {
    return addr;
  }
}
