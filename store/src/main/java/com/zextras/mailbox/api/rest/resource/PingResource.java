/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/ping")
public class PingResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Ping", description = "Health-check endpoint for the internal API")
	@ApiResponse(responseCode = "200", description = "Service is alive")
	public String ping() {
		return "{\"status\":\"pong\"}";
	}
}
