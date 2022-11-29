// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import javax.servlet.http.HttpServletResponse;

public class UserServletException extends Exception {
    private int mCode;

    public UserServletException(int code, String message) {
        super(message);
        mCode = code;
    }
    
    public UserServletException(int code, String message, Throwable cause) {
        super(message, cause);
        mCode = code;
    }

    public int getHttpStatusCode() {
       return mCode;
    }

    public static UserServletException notImplemented(String message) {
        return new UserServletException(HttpServletResponse.SC_NOT_IMPLEMENTED, message);
    }

    public static UserServletException badRequest(String message) {
        return new UserServletException(HttpServletResponse.SC_BAD_REQUEST, message);
    }
    
}
