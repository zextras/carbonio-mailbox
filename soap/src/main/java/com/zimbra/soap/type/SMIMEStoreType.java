// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;



@XmlEnum
public enum SMIMEStoreType {
    // case must match protocol
    CONTACT,
    GAL,
    LDAP;

    public static SMIMEStoreType fromString(String s)
    throws ServiceException {
        try {
            return SMIMEStoreType.valueOf(s);
        } catch (IllegalArgumentException e) {
           throw ServiceException.INVALID_REQUEST(
                   "unknown 'SMIMEStoreType' key: " + s + ", valid values: " +
                   Arrays.asList(SMIMEStoreType.values()), null);
        }
    }
}
