// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;

@XmlEnum
public enum GetSessionsSortBy {
    // case must match protocol
    nameAsc, nameDesc, createdAsc, createdDesc, accessedAsc, accessedDesc;

    public static GetSessionsSortBy fromString(String s)
    throws ServiceException {
        try {
            return GetSessionsSortBy.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("invalid SortBy " + s + ", valid values: " +
                    Arrays.asList(GetSessionsSortBy.values()), e);
        }
    }
}
