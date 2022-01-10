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
@GraphQLType(name=GqlConstants.ENUM_LICENSE_STATUS, description="License status")
public enum LicenseStatus {
    // case must match protocol
    @GraphQLEnumValue NOT_INSTALLED,
    @GraphQLEnumValue NOT_ACTIVATED,
    @GraphQLEnumValue IN_FUTURE,
    @GraphQLEnumValue EXPIRED,
    @GraphQLEnumValue INVALID,
    @GraphQLEnumValue LICENSE_GRACE_PERIOD,
    @GraphQLEnumValue ACTIVATION_GRACE_PERIOD,
    @GraphQLEnumValue OK;

    public static LicenseStatus fromString(String s) throws ServiceException {
        try {
            return LicenseStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("Invalid license status: " + s +
                    ", valid values: " +
                    Arrays.asList(LicenseStatus.values()), null);
        }
    }
}
