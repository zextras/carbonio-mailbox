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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/** Explicitly sets ETag header in response; bypassing Jetty ETag generation */
public class ETagHeaderFilter implements Filter {

    public static String ZIMBRA_ETAG_HEADER = "X-Zimbra-ETag";

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ETagResponseWrapper wrapper = new ETagResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, wrapper);
    }

    private static class ETagResponseWrapper extends HttpServletResponseWrapper {

        public ETagResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void addHeader(String name, String value) {
            if (ZIMBRA_ETAG_HEADER.equalsIgnoreCase(name)) {
                super.addHeader("ETag", value);
            }
            super.addHeader(name, value);
        }

        @Override
        public void setHeader(String name, String value) {
            if (ZIMBRA_ETAG_HEADER.equalsIgnoreCase(name)) {
                super.setHeader("ETag", value);
            }
            super.setHeader(name, value);
        }
    }
}
