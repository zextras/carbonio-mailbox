// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.service.ServiceException;
import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

/** JAXB analog to {@com.zimbra.cs.account.accesscontrol.TargetType} */
@XmlEnum
@GraphQLType(name = GqlConstants.ENUM_TARGET_TYPE, description = "")
public enum TargetType {
  // case must match protocol
  @GraphQLEnumValue
  account,
  @GraphQLEnumValue
  calresource,
  @GraphQLEnumValue
  cos,
  @GraphQLEnumValue
  dl,
  @GraphQLEnumValue
  group,
  @GraphQLEnumValue
  domain,
  @GraphQLEnumValue
  server,
  @GraphQLEnumValue
  ucservice,
  @GraphQLEnumValue
  xmppcomponent,
  @GraphQLEnumValue
  zimlet,
  @GraphQLEnumValue
  config,
  @GraphQLEnumValue
  global;

  public static TargetType fromString(String s) throws ServiceException {
    try {
      return TargetType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "unknown 'TargetType' key: "
              + s
              + ", valid values: "
              + Arrays.asList(TargetType.values()),
          null);
    }
  }
}
