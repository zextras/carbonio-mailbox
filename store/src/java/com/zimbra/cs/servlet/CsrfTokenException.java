// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;



/**
 * @author zimbra
 *
 */
public class CsrfTokenException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -3966730476787360251L;

    /**
     *
     */
    public CsrfTokenException() {
        super();
    }

    /**
     * @param message
     */
    public CsrfTokenException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public CsrfTokenException(Throwable cause) {
        super(cause);
    }

    /**
     * @param string
     * @param e
     */
    public CsrfTokenException(String message,Throwable cause) {
       super(message, cause);
    }

}
