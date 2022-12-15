// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class ImapThrottledException extends ImapException {
    private static final long serialVersionUID = 2431054742961917965L;

    public ImapThrottledException(String message) {
        super(message);
    }
}
