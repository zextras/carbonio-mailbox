// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import static com.zimbra.common.mime.MimeConstants.CT_APPLICATION_JSON;

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
  private final ObjectMapper objectMapper;

  @Inject
  public HealthServlet(HealthUseCase healthUseCase) {
    this.healthUseCase = healthUseCase;
    objectMapper = new ObjectMapper();
  }

  @Override
  protected void doGet(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    String requestedPath = httpServletRequest.getPathInfo();
    if (Objects.isNull(requestedPath)) {
      requestedPath = "/";
    }

    switch (requestedPath) {
      case "/":
        handleRoot(httpServletResponse);
        break;
      case "/ready":
        handleReady(httpServletResponse);
        break;
      case "/live":
        handleLive(httpServletResponse);
        break;
      default:
        handleDefault(httpServletResponse);
        break;
    }
  }

  private void handleRoot(HttpServletResponse httpServletResponse) {
    final var ready = healthUseCase.isReady();
    final var status = ready ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR;
    httpServletResponse.setStatus(status);

    final HealthResponse healthResponse =
        HealthResponseBuilder.newInstance()
            .withReadiness(ready)
            .withDependencies(healthUseCase.dependenciesHealthSummary())
            .build();

    try {
      httpServletResponse.setContentType(CT_APPLICATION_JSON);
      httpServletResponse.getWriter().write(objectMapper.writeValueAsString(healthResponse));
    } catch (IOException e) {
      LOG.warn(e.getMessage(), e);
    }
  }

  private void handleLive(HttpServletResponse httpServletResponse) {
    final var status =
        healthUseCase.isLive() ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR;
    httpServletResponse.setStatus(status);
  }

  private void handleReady(HttpServletResponse httpServletResponse) {
    final var status =
        healthUseCase.isReady() ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR;
    httpServletResponse.setStatus(status);
  }

  private static void handleDefault(HttpServletResponse httpServletResponse) {
    httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
  }
}
