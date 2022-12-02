// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;

public class IndexStoreException extends IOException {
    private static final long serialVersionUID = 5616624118610385214L;

    public IndexStoreException(String message) {
        super(message);
    }

    public IndexStoreException(String message, Exception exception) {
        super(message, exception);
    }
}
