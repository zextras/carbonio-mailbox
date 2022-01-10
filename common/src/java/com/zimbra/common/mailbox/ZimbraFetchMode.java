// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.util.Arrays;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.zclient.ZClientException;

public enum ZimbraFetchMode {
        /* Everything. */
        NORMAL,
        /* Only IMAP data. */
        IMAP,
        /* Only the metadata modification sequence number. */
        MODSEQ,
        /* Only the ID of the item's parent (-1 if no parent). */
        PARENT,
        /* Only ID. */
        IDS;

    public static ZimbraFetchMode fromString(String s)
    throws ServiceException {
        try {
            return ZimbraFetchMode.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ZClientException.CLIENT_ERROR(String.format("unknown 'fetchMode':'%s' - valid values: ", s,
                   Arrays.asList(ZimbraFetchMode.values())), null);
        }
    }
}
