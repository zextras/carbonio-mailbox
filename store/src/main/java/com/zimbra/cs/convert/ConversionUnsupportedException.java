// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.convert;

import javax.servlet.ServletException;

public class ConversionUnsupportedException extends ServletException {

    private static final long serialVersionUID = -7047830018532941692L;

    public ConversionUnsupportedException(String msg) {
        super(msg);
    }
}
