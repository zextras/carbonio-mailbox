// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.health.HealthService;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;

@Singleton
public class HealthServlet extends HttpServlet {

  private final HealthService healthService;

  @Inject
  public HealthServlet(HealthService healthService) {
    this.healthService = healthService;
  }

  @Override
  protected void doGet(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    final String path = httpServletRequest.getPathInfo();
    switch (path) {
      case "/ready":
        httpServletResponse.setStatus(healthService.isReady() ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        break;
      case "/live":
        httpServletResponse.setStatus(healthService.isLive() ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        break;
    }
  }

}
