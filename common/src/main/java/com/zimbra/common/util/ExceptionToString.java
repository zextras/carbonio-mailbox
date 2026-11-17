// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Sep 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.common.util;

import com.google.common.base.Throwables;


/**
 * @author tim
 *
 * Dead-Simple: does what it says - writes data about the exception to a string.
 */
public final class ExceptionToString {
    public static final String ToString(Throwable e) {
        return Throwables.getStackTraceAsString(e);
    }
    
}
