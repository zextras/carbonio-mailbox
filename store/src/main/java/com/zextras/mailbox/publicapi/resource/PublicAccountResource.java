/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi.resource;

import com.zextras.mailbox.api.rest.resource.dto.AccountInfoResponse;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.publicapi.service.PublicAccountService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Dependent
@Path("/accounts")
public class PublicAccountResource {

	@Inject
	private PublicAccountService publicAccountService;

	@GET
	@Path("/{id}/info")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Account Info", description = "Returns account info by ID on behalf of the token owner, "
			+ "who must be the account itself or hold getAccountInfo on it")
	@ApiResponse(responseCode = "200", description = "Account Info",
			content = @Content(schema = @Schema(implementation = AccountInfoResponse.class)))
	@ApiResponse(responseCode = "401", description = "Missing, invalid or expired auth token",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Token owner cannot read the account",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Account not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response getAccountInfo(
			@Parameter(description = "The account ID") @PathParam("id") String id,
			@CookieParam("ZM_AUTH_TOKEN") String authTokenCookie,
			@CookieParam("ZM_ADMIN_AUTH_TOKEN") String adminAuthTokenCookie) {
		final String token = adminAuthTokenCookie != null ? adminAuthTokenCookie : authTokenCookie;
		if (token == null || token.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorResponse("Missing auth token"))
					.build();
		}
		return publicAccountService.getAccount(token, id)
				.mapTry(account -> Response.ok(AccountInfoResponse.from(account)).build())
				.recover(PublicAccountResource::toErrorResponse)
				.get();
	}

	private static Response toErrorResponse(Throwable e) {
		return switch (e) {
			case AuthTokenException ignored -> error(Response.Status.UNAUTHORIZED, "Invalid auth token");
			case ServiceException se when se.getCode().equals(ServiceException.AUTH_EXPIRED) ->
					error(Response.Status.UNAUTHORIZED, e.getMessage());
			case ServiceException se when se.getCode().equals(ServiceException.PERM_DENIED) ->
					error(Response.Status.FORBIDDEN, e.getMessage());
			case ServiceException se when se.getCode().equals(ServiceException.NOT_FOUND) ->
					error(Response.Status.NOT_FOUND, e.getMessage());
			default -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
		};
	}

	private static Response error(Response.Status status, String message) {
		return Response.status(status).entity(new ErrorResponse(message)).build();
	}
}
