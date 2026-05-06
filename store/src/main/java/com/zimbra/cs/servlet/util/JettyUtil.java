// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.util;

import jakarta.servlet.http.HttpServletRequest;

import com.zimbra.common.util.ZimbraLog;

public class JettyUtil {

    /**
     * Attempts to set the idle timeout on the underlying Jetty connection.
     * Note: This is a best-effort operation. In Jetty 12, direct access to
     * the internal HttpConnection from servlet context is no longer supported.
     * Long-running responses should configure connector-level idle timeouts instead.
     */
    public static void setIdleTimeout(long timeout, HttpServletRequest request) {
        ZimbraLog.misc.debug("JettyUtil.setIdleTimeout(%d) called; direct connection timeout adjustment not supported in Jetty 12", timeout);
    }
}
