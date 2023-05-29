// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;

@XmlEnum
public enum AutoProvPrincipalBy {
    // case must match protocol
    dn,    // DN in external LDAP source
    name;  // name to be applied to the auto provision search or bind DN template configured on the domain

    public static AutoProvPrincipalBy fromString(String str)
    throws ServiceException {
        try {
            return AutoProvPrincipalBy.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("Invalid AutoProvPrincipalBy: " + str +
                    ", valid values: " + Arrays.asList(AutoProvPrincipalBy.values()), null);
        }
    }
};
