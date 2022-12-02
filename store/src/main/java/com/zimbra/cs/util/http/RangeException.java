// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.http;

public class RangeException extends Exception {
    private static final long serialVersionUID = 4081054215965536622L;
    private int errorCode;

    public RangeException(int errorCode) {
        super();
        this.errorCode = errorCode;
    }
}
