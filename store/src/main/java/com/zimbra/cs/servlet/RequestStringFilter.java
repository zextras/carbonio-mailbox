// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import com.zimbra.common.util.ZimbraLog;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestStringFilter implements Filter {

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpReq = (HttpServletRequest) request;
      if (httpReq.getQueryString() != null && httpReq.getQueryString().matches(".*(%00|\\x00).*")) {
        ZimbraLog.misc.warn("Rejecting request containing null character in query string");
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      if (httpReq.getRequestURI() != null && httpReq.getRequestURI().matches(".*(%00|\\x00).*")) {
        ZimbraLog.misc.warn("Rejecting request containing null character in URI");
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}
}
