// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient;

import java.io.IOException;

/**
 * Indicates that a mail client error has occurred.
 */
@SuppressWarnings("serial")
public class MailException extends IOException {
    /**
     * Creates a new <tt>MailException</tt> with a <tt>null</tt> detail
     * message.
     */
    public MailException() {}

    /**
     * Creates a new <tt>MailException</tt> with the specified detail message.
     *
     * @param msg the detail message, or <tt>null</tt> if none
     */
    public MailException(String msg) {
        super(msg);
    }

    /**
     * Creates a new <tt>MailException</tt> with the specified detail message
     * and cause.
     * 
     * @param msg the detail message, or <tt>null</tt> if none
     * @param cause the cause, or <tt>null</tt> if unknown
     */
    public MailException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }
}
