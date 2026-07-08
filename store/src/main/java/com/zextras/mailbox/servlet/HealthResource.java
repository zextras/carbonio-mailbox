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

/**
 * JAX-RS health endpoint managed by CDI. Routing is by annotation (no {@code switch}); the
 * {@link HealthUseCase} dependency is injected by CDI/Weld ({@code @Inject}), and RESTEasy obtains
 * the resource instance from the CDI {@code BeanManager} via {@code resteasy-cdi}. {@code @Dependent}
 * makes it a CDI bean (discovered because {@code beans.xml} uses {@code bean-discovery-mode=annotated})
 * without requiring a no-arg constructor for proxying.
 */
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
