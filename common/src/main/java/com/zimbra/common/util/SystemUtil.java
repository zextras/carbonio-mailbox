// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.ObjectUtils;

public class SystemUtil {

    public static String getStackTrace() {
        return getStackTrace(new Throwable());
    }
    
    public static String getStackTrace(Throwable t) {
        return Throwables.getStackTraceAsString(t);
    }
    
    /**
     * Returns the innermost exception wrapped by
     * <tt>t</tt>.  The innermost exception is found by iterating
     * the exceptions returned by {@link Throwable#getCause()}.
     * 
     * @return the innermost exception, or <tt>null</tt> if <tt>t</tt>
     * is <tt>null</tt>.
     */
    public static Throwable getInnermostException(Throwable t) {
        return t == null ? null : Throwables.getRootCause(t);
    }
    
    /**
     * Returns the first non-null value in the given list.
     */
    public static <E> E coalesce(E ... values) {
        return ObjectUtils.firstNonNull(values);
    }
    
    public static String getProductVersion() {
        return SystemUtil.class.getPackage().getImplementationVersion();
    }
}
