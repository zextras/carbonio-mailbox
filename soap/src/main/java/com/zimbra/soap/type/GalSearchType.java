// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;



@XmlEnum
public enum GalSearchType {
    // case must match protocol
    all,
    account,
    resource,
    group;

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
