/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.CosCountResponse;
import com.zextras.mailbox.api.rest.resource.dto.CosInfoResponse;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.AccountFilter;
import com.zextras.mailbox.api.rest.service.CosService;
import com.zimbra.common.service.ServiceException;
import io.vavr.control.Try;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
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

	@GET
	@Path("/count")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Count accounts by COS", description = "Counts the accounts in each of the given COSes, or in every COS when no cosId is given. An account inheriting the domain or default COS is attributed to that COS. Only the accounts a licence counts are ever considered: calendar resources, system resources and external virtual accounts are always left out. Accounts can be excluded further by property; a property given more than once excludes any of its values, and a property left out excludes nothing.")
	@ApiResponse(responseCode = "200", description = "Accounts per COS",
			content = @Content(schema = @Schema(implementation = CosCountResponse.class)))
	@ApiResponse(responseCode = "400", description = "Too many cosId query parameters, or an unknown excludeAccountStatus",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response countCos(
			@Parameter(description = "The COS ids to count, repeated; omit to count every COS") @QueryParam("cosId") List<String> cosIds,
			@Parameter(description = "The account statuses to leave out, repeated; omit to count every status. Matches the status an admin sees, so the domain status overrides the account's own.") @QueryParam("excludeAccountStatus") List<String> excludedAccountStatuses) {
		if (cosIds != null && cosIds.size() > 100) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Too many entries: max 100 allowed"))
					.build();
		}
		return Try.of(() -> AccountFilter.excluding(excludedAccountStatuses))
				.flatMap(accountFilter -> cosService.countCos(cosIds, accountFilter))
				.map(counts -> Response.ok(new CosCountResponse(
						counts.values().stream().mapToLong(Long::longValue).sum(), counts)).build())
				.recover(CosResource::toErrorResponse)
				.get();
	}

	private static Response toErrorResponse(Throwable e) {
		if (e instanceof ServiceException se) {
			if (se.getCode().equals(ServiceException.NOT_FOUND)) {
				return Response.status(Response.Status.NOT_FOUND)
						.entity(new ErrorResponse(e.getMessage()))
						.build();
			}
			if (se.getCode().equals(ServiceException.INVALID_REQUEST)) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorResponse(e.getMessage()))
						.build();
			}
		}
		return Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
	}
}
