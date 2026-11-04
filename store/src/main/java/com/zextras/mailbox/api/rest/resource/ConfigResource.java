/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.ConfigOperationRequest;
import com.zextras.mailbox.api.rest.resource.dto.ModifyConfigRequest;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.ConfigOperation;
import com.zextras.mailbox.api.rest.service.ConfigService;
import com.zextras.mailbox.api.rest.service.InvalidConfigModificationException;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vavr.control.Try;
import java.util.List;
import java.util.Set;
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
	@Operation(summary = "Modify Global Config", description = "Applies the operations in order, like an LDAP modify request, as a single LDAP modification. 'set' replaces all values of an attribute (one value for a single-valued attribute), 'unset' clears it, 'add' and 'remove' change values of multi-valued attributes only. A later operation on the same attribute builds on the earlier ones, so the last set wins. Attributes are validated by the LDAP schema. Adding a value already present or removing a missing one has no effect.")
	@ApiResponse(responseCode = "204", description = "Global config modified")
	@ApiResponse(responseCode = "400", description = "Invalid request",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response modifyConfig(ModifyConfigRequest request) {
		if (request == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Missing request body"))
					.build();
		}
		return Try.of(() -> toOperations(request))
				.flatMap(configService::modifyConfig)
				.map(ignored -> Response.noContent().build())
				.recover(ConfigResource::toErrorResponse)
				.get();
	}

	private static List<ConfigOperation> toOperations(ModifyConfigRequest request) {
		if (request.operations() == null) {
			return List.of();
		}
		return request.operations().stream().map(ConfigResource::toOperation).toList();
	}

	private static ConfigOperation toOperation(ConfigOperationRequest operation) {
		if (operation == null) {
			throw new InvalidConfigModificationException("Missing operation");
		}
		return operation.toOperation();
	}

	private static final Set<String> INVALID_REQUEST_CODES = Set.of(
			ServiceException.INVALID_REQUEST,
			AccountServiceException.INVALID_ATTR_NAME,
			AccountServiceException.INVALID_ATTR_VALUE);

	private static Response toErrorResponse(Throwable e) {
		return switch (e) {
			case InvalidConfigModificationException ignored -> badRequest(e);
			case ServiceException se when INVALID_REQUEST_CODES.contains(se.getCode()) -> badRequest(e);
			default -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
		};
	}

	private static Response badRequest(Throwable e) {
		return Response.status(Response.Status.BAD_REQUEST)
				.entity(new ErrorResponse(e.getMessage()))
				.build();
	}
}
