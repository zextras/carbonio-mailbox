// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.util.ZimbraLog;

public class RequestStringFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                    ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            String qs = httpReq.getQueryString();
            if (qs != null && containsNull(qs)) {
                ZimbraLog.misc.warn("Rejecting request containing null character in query string");
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String uri = httpReq.getRequestURI();
            if (uri != null && containsNull(uri)) {
                ZimbraLog.misc.warn("Rejecting request containing null character in URI");
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static boolean containsNull(String s) {
        return s.indexOf('\0') >= 0 || s.indexOf("%00") >= 0;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}