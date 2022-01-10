// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.IOException;

public class StartOutOfBoundsException extends IOException {
    private static final long serialVersionUID = -625838451361202182L;

    public StartOutOfBoundsException() {
        super();
    }

    public StartOutOfBoundsException(String message) {
        super(message);
    }
}
