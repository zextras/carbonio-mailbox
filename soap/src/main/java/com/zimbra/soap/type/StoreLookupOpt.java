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
public enum StoreLookupOpt {
    // case must match protocol
    @GraphQLEnumValue(description = "While iterating through stores, stop if any certs are found in a store and"
            + " just return those certs - remaining stores will not be attempted.") ANY, 
    @GraphQLEnumValue(description = "Always iterate through all specified stores") ALL;

    public static StoreLookupOpt fromString(String s)
    throws ServiceException {
        try {
            return StoreLookupOpt.valueOf(s);
        } catch (IllegalArgumentException e) {
           throw ServiceException.INVALID_REQUEST(
                   "unknown 'StoreLookupOpt' key: " + s + ", valid values: " +
                   Arrays.asList(StoreLookupOpt.values()), null);
        }
    }
}