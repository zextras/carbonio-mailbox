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

@XmlEnum
@GraphQLType(name = GqlConstants.CLASS_SMIME_STORE_TYPE, description = "Smime certificate stores")
public enum SMIMEStoreType {
  // case must match protocol
  @GraphQLEnumValue(description = "contacts")
  CONTACT,
  @GraphQLEnumValue(description = "Global Address List (internal and external)")
  GAL,
  @GraphQLEnumValue(description = " external LDAP")
  LDAP;

  public static SMIMEStoreType fromString(String s) throws ServiceException {
    try {
      return SMIMEStoreType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "unknown 'SMIMEStoreType' key: "
              + s
              + ", valid values: "
              + Arrays.asList(SMIMEStoreType.values()),
          null);
    }
  }
}
