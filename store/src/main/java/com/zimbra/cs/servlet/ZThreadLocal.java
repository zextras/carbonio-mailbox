// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import com.zimbra.soap.RequestContext;


/**
 * @author zimbra
 *
 */
public class ZThreadLocal {

    public static final ThreadLocal<RequestContext> zThreadLocal  = new ThreadLocal<>();

    public static void setContext(RequestContext context) {
        zThreadLocal.set(context);
    }

    public static void unset() {
        zThreadLocal.remove();
    }

    public static RequestContext getRequestContext() {
        return zThreadLocal.get();
    }
}
