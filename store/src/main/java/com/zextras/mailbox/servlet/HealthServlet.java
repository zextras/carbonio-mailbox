// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.mailbox.health.HealthUseCase;
import com.zextras.mailbox.servlet.HealthResponse.Builder;
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
        httpServletResponse.setStatus(isReady ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        final ObjectMapper objectMapper = new ObjectMapper();
        final Builder builder = new Builder().withReadiness(isReady);
        healthUseCase.getDependencies().forEach(
            builder::withDependency
        );
        final String jsonResponse = objectMapper.writeValueAsString(builder.build());
        httpServletResponse.getWriter().write(jsonResponse);
        break;
      case "/ready":
        httpServletResponse.setStatus(isReady ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        break;
      case "/live":
        httpServletResponse.setStatus(healthUseCase.isLive() ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        break;
    }
  }

}
