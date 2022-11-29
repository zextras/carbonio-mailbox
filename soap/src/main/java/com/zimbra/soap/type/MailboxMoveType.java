// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.service.ServiceException;

@XmlEnum
public enum MailboxMoveType {
    // case must match protocol
    out, in;

    static public MailboxMoveType fromString(String str)
    throws ServiceException {
        try {
            return MailboxMoveType.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("Invalid MailboxMoveType: " + str +
                    ", valid values: " + Arrays.asList(MailboxMoveType.values()), null);
        }
    }

    public static MailboxMoveType lookup(String val) {
        if (val != null) {
            try {
                return valueOf(val);
            } catch (IllegalArgumentException e) {}
        }
        return null;
    }
};
