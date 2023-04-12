// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.service.ServiceException;



public enum MemberOfSelector {
    all,
    directOnly,
    none;

    public static MemberOfSelector fromString(String s) throws ServiceException {
        try {
            return MemberOfSelector.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("unknown NeedMemberOf: "+s, e);
        }
    }
}
