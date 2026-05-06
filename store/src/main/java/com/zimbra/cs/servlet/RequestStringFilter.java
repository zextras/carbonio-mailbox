// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
            if (httpReq.getQueryString() != null && httpReq.getQueryString().matches(".*(%00|\\x00).*")) {
                ZimbraLog.misc.warn("Rejecting request containing null character in query string");
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (httpReq.getRequestURI() != null && httpReq.getRequestURI().matches(".*(%00|\\x00).*")) {
                ZimbraLog.misc.warn("Rejecting request containing null character in URI");
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}