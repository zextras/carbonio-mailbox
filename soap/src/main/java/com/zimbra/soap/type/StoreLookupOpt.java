// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;



@XmlEnum
public enum StoreLookupOpt {
    // case must match protocol
    ANY,
    ALL;

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