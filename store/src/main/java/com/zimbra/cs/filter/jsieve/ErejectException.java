// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.exception.SieveException;

/**
 * Class <code>ErejectException</code> indicates that message delivery should be
 * terminated due to a Ereject Command.
 */

public class ErejectException extends SieveException {
    /**
     * Constructor for ErejectException
     */
    public ErejectException() {
        super();
    }

    /**
     * Constructor for ErejectException
     * @param message
     * @param cause
     */
    public ErejectException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for ErejectException
     * @param message
     */
    public ErejectException(String message) {
        super(message);
    }

    /**
     * Constructor for ErejectException
     * @param cause
     */
    public ErejectException(Throwable cause) {
        super(cause);
    }
}
