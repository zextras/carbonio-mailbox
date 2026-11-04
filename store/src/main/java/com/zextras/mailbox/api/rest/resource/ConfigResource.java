/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.ModifyConfigRequest;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.ConfigService;
import com.zextras.mailbox.api.rest.service.InvalidConfigModificationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/config")
public class ConfigResource {

	private final ConfigService configService;

	public ConfigResource(ConfigService configService) {
		this.configService = configService;
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Modify Global Config", description = "Adds and removes values of multi-valued global config attributes in a single LDAP modification. Only allowed attributes can be modified. Adding a value already present or removing a missing one has no effect.")
	@ApiResponse(responseCode = "204", description = "Global config modified")
	@ApiResponse(responseCode = "400", description = "Invalid request or attribute not allowed",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response modifyConfig(ModifyConfigRequest request) {
		if (request == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Missing request body"))
					.build();
		}
		return configService.modifyConfig(request.add(), request.remove())
				.map(ignored -> Response.noContent().build())
				.recover(ConfigResource::toErrorResponse)
				.get();
	}

	private static Response toErrorResponse(Throwable e) {
		if (e instanceof InvalidConfigModificationException) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse(e.getMessage()))
					.build();
		}
		return Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
	}
}
