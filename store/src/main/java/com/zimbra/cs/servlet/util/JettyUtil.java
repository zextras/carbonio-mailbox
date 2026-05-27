// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.util;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.ee8.nested.HttpChannel;
import org.eclipse.jetty.io.EndPoint;

import com.zimbra.common.util.ZimbraLog;

public class JettyUtil {

    public static void setIdleTimeout(long timeout, HttpServletRequest request) {
        if (request == null) {
            ZimbraLog.misc.warn("cannot set timeout for null request", new Exception());
            return;
        }
        HttpChannel httpChannel = (HttpChannel) request.getAttribute(HttpChannel.class.getName());
        if (httpChannel == null) {
            ZimbraLog.misc.warn("got null HttpChannel from request attribute", new Exception());
            return;
        }
        EndPoint ep = httpChannel.getCoreRequest().getConnectionMetaData().getConnection().getEndPoint();
        if (ep != null) {
            ep.setIdleTimeout(timeout);
        } else {
            ZimbraLog.misc.warn("null endpoint setting Jetty timeout?", new Exception());
        }
    }
}
