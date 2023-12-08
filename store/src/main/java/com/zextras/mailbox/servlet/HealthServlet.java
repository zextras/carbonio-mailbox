// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.mailbox.health.HealthUseCase;
import com.zextras.mailbox.servlet.HealthResponse.HealthResponseBuilder;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import java.io.IOException;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;

@Singleton
public class HealthServlet extends HttpServlet {

  private static final Log LOG = LogFactory.getLog(HealthServlet.class);
  private final HealthUseCase healthUseCase;

  @Inject
  public HealthServlet(HealthUseCase healthUseCase) {
    this.healthUseCase = healthUseCase;
  }

  @Override
  protected void doGet(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    String requestedPath = httpServletRequest.getPathInfo();
    if (Objects.isNull(requestedPath)) {
      requestedPath = "/";
    }
    final boolean isReady = healthUseCase.isReady();
    switch (requestedPath) {
      case "/":
        try {
          httpServletResponse.setStatus(
              isReady ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);

          final HealthResponse healthResponse = HealthResponseBuilder.newInstance()
              .withReadiness(isReady)
              .withDependencies(healthUseCase.getDependencies())
              .build();

          httpServletResponse.getWriter()
              .write(new ObjectMapper().writeValueAsString(healthResponse));
        } catch (IOException e) {
          LOG.warn(e.getMessage(), e);
        }
        break;
      case "/ready":
        httpServletResponse.setStatus(
            isReady ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        break;
      case "/live":
        httpServletResponse.setStatus(
            healthUseCase.isLive() ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
        break;
    }
  }

}
