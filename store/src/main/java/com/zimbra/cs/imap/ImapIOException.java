// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class ImapIOException extends ImapException {

    private static final long serialVersionUID = 5910832733809945145L;

    public ImapIOException() {
        super();
    }

    public ImapIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImapIOException(String message) {
        super(message);
    }
}
