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

/**
 * Restricts a path to requests that arrived on a specific local port, mirroring the port check
 * {@code ZimbraServlet} does with its {@code allowed.ports} init-param, but <b>fail-closed</b>:
 * anything not on the allowed port gets a 404 (indistinguishable from "not mapped").
 *
 * <p>Used to keep the internal API reachable only via its dedicated (loopback) connector once both
 * APIs share a single CDI servlet context, replacing the previous per-context virtual-host binding.
 */
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
