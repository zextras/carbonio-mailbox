// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;

@XmlEnum
public enum GalMode {
    // case must match protocol
    both, ldap, zimbra;

    public static GalMode fromString(String s) throws ServiceException {
        try {
            return GalMode.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST(
                    "invalid value: "+s+", valid values: "
                    + Arrays.asList(values()), null);
        }
    }
    public boolean isBoth() { return this == both;}
    public boolean isLdap() { return this == ldap;}
    public boolean isZimbra() { return this == zimbra;}
}
