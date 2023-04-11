// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;

import io.leangen.graphql.annotations.GraphQLEnumValue;

@XmlEnum
public enum SourceLookupOpt {
    // case must match protocol
    @GraphQLEnumValue(description = "While iterating through multiple sources configured for a store, stop if any "
            + "certificates are found in one source - remaining configured sources will not be attempted.") ANY,
    @GraphQLEnumValue(description = "Always iterate through all configured sources") ALL;

    public static SourceLookupOpt fromString(String s)
    throws ServiceException {
        try {
            return SourceLookupOpt.valueOf(s);
        } catch (IllegalArgumentException e) {
           throw ServiceException.INVALID_REQUEST(
                   "unknown 'SourceLookupOpt' key: " + s + ", valid values: " +
                   Arrays.asList(SourceLookupOpt.values()), null);
        }
    }
}
