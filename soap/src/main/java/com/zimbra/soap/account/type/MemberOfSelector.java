// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.service.ServiceException;
import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(
    name = GqlConstants.CLASS_MEMBER_OF_SELECTOR,
    description = "Criteria to decide when \"isMember\" is set for group")
public enum MemberOfSelector {
  @GraphQLEnumValue(
      description =
          "the isMember flag returned is set if the user is a direct or indirect member of the"
              + " group, otherwise it is unset")
  all,
  @GraphQLEnumValue(
      description =
          "the isMember flag returned is set if the user is a direct member of the group, otherwise"
              + " it is unset")
  directOnly,
  @GraphQLEnumValue(description = "the isMember flag is not returned")
  none;

  public static MemberOfSelector fromString(String s) throws ServiceException {
    try {
      return MemberOfSelector.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST("unknown NeedMemberOf: " + s, e);
    }
  }
}
