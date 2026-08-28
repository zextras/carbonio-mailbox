/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.CosInfoResponse;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.CosService;
import com.zimbra.common.service.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Dependent
@Path("/cos")
public class CosResource {

	@Inject
	private CosService cosService;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get COS", description = "Returns a class of service by ID. A 404 means no COS carries that ID, which callers can read as non-existence.")
	@ApiResponse(responseCode = "200", description = "COS info",
			content = @Content(schema = @Schema(implementation = CosInfoResponse.class)))
	@ApiResponse(responseCode = "404", description = "COS not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response getCos(
			@Parameter(description = "The COS ID") @PathParam("id") String id) {
		return cosService.getCos(id)
				.map(cos -> Response.ok(CosInfoResponse.from(cos)).build())
				.recover(CosResource::toErrorResponse)
				.get();
	}

	private static Response toErrorResponse(Throwable e) {
		if (e instanceof ServiceException se && se.getCode().equals(ServiceException.NOT_FOUND)) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity(new ErrorResponse(e.getMessage()))
					.build();
		}
		return Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
	}
}
