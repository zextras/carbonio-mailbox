// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.health.HealthUseCase;
import com.zextras.mailbox.servlet.HealthResponse.HealthResponseBuilder;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** JAX-RS health endpoint; CDI-managed with {@link HealthUseCase} injected by Weld. */
@Dependent
@Path("/")
public class HealthResource {

  @Inject
  private HealthUseCase healthUseCase;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response health() {
    final boolean ready = healthUseCase.isReady();
    final HealthResponse body =
        HealthResponseBuilder.newInstance()
            .withReadiness(ready)
            .withDependencies(healthUseCase.dependenciesHealthSummary())
            .build();
    return Response.status(ready ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
        .entity(body)
        .build();
  }

  @GET
  @Path("/ready")
  public Response ready() {
    return status(healthUseCase.isReady());
  }

  @GET
  @Path("/live")
  public Response live() {
    return status(healthUseCase.isLive());
  }

  private static Response status(boolean ok) {
    return Response.status(ok ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR).build();
  }
}
