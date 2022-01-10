// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.io.IOException;

/**
 * This exception is thrown by {@link ImapProxy} if a network error occurred with the remote IMAP server.
 *
 * @author ysasaki
 */
final class ImapProxyException extends IOException {

    ImapProxyException(String message) {
        super(message);
    }

    ImapProxyException(Throwable e) {
        super(e);
    }

}
