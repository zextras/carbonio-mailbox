// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/** Rejects (404, fail-closed) any request that did not arrive on {@code allowedPort}. */
public class PortRestrictionFilter implements Filter {

  private final int allowedPort;

  public PortRestrictionFilter(int allowedPort) {
    this.allowedPort = allowedPort;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request.getLocalPort() != allowedPort) {
      ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    chain.doFilter(request, response);
  }
}
