// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.service.ServiceException;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlEnum
@GraphQLType(name=GqlConstants.CLASS_SEARCH_TYPE, description="Search type")
public enum GalSearchType {
    // case must match protocol
    @GraphQLEnumValue(description="for combination of all types") all,
    @GraphQLEnumValue(description="for regular user accounts, aliases and distribution lists") account,
    @GraphQLEnumValue(description="for calendar resources") resource,
    @GraphQLEnumValue(description="for groups") group;

    public static GalSearchType fromString(String s) throws ServiceException {
        try {
            return GalSearchType.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("Invalid search type: " + s +
                    ", valid values: " +
                    Arrays.asList(GalSearchType.values()), null);
        }
    }
}
