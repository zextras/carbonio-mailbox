// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;




@XmlEnum
public enum AccountBy {
    // case must match protocol
    adminName,
    appAdminName,
    id,
    foreignPrincipal,
    name,
    krb5Principal;

    public static AccountBy fromString(String s)
    throws ServiceException {
        try {
            return AccountBy.valueOf(s);
        } catch (final IllegalArgumentException e) {
           throw ServiceException.INVALID_REQUEST("unknown 'By' key: " + s + ", valid values: " +
                   Arrays.asList(AccountBy.values()), null);
        }
    }

    public com.zimbra.common.account.Key.AccountBy toKeyAccountBy()
    throws ServiceException {
        return com.zimbra.common.account.Key.AccountBy.fromString(this.name());
    }
}
