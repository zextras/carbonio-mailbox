// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.util;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.HttpConnection;

import com.zimbra.common.util.ZimbraLog;

public class JettyUtil {

    public static void setIdleTimeout(long timeout, HttpServletRequest request) {
        if (request != null) {
            Object attr = request.getAttribute("org.eclipse.jetty.server.HttpConnection");
            if (attr instanceof HttpConnection) {
                @SuppressWarnings("resource")
                HttpConnection conn = (HttpConnection) attr;
                EndPoint ep = conn.getEndPoint();
                if (ep != null) {
                    ep.setIdleTimeout(timeout);
                } else {
                    ZimbraLog.misc.warn("null endpoint setting Jetty timeout?", new Exception());
                }
            } else {
                //this won't work for SPDY connections, so we'll have to consider this further once we enable it.
                ZimbraLog.misc.warn("got [%s] not instanceof org.eclipse.jetty.server.HttpConnection", attr, new Exception());
            }
        } else {
            ZimbraLog.misc.warn("cannot set timeout for null request", new Exception());
        }
    }
}