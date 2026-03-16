/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/accounts")
public class AccountResource {

	private final AccountService accountService;

	public AccountResource(AccountService accountService) {
		this.accountService = accountService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Account Info", description = "Returns account info")
	@ApiResponse(responseCode = "200", description = "Account Info",
			content = @Content(schema = @Schema(implementation = AccountInfoResponse.class)))
	@ApiResponse(responseCode = "404", description = "Account not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@Path("/{accountId}/info")
	public Response getAccountInfo(
			@Parameter(description = "The account ID") @PathParam("accountId") String accountId) {
		return accountService.getAccount(accountId)
				.map(account -> Response.ok(AccountInfoResponse.from(account)).build())
				.recover(e -> switch (e) {
					case ServiceException se when se.getCode().equals(ServiceException.NOT_FOUND) ->
							Response.status(Response.Status.NOT_FOUND)
									.entity(new ErrorResponse(e.getMessage()))
									.build();
					default -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
				})
				.get();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Authenticated Account Info", description = "Returns account info for the authenticated user")
	@ApiResponse(responseCode = "200", description = "Account Info")
	@ApiResponse(responseCode = "401", description = "Invalid or missing auth token")
	@ApiResponse(responseCode = "404", description = "Account not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@Path("/myself/info")
	public Response getMyAccountInfo(
			@CookieParam("ZM_AUTH_TOKEN") String authTokenCookie,
			@CookieParam("ZM_ADMIN_AUTH_TOKEN") String adminAuthTokenCookie) {
		final String token = adminAuthTokenCookie != null ? adminAuthTokenCookie : authTokenCookie;
		if (token == null || token.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorResponse("Missing auth token"))
					.build();
		}
		return accountService.getAccountByAuthToken(token)
				.map(account -> Response.ok(AccountInfoResponse.from(account)).build())
				.recover(e -> switch (e) {
					case AuthTokenException ignored -> Response.status(Response.Status.UNAUTHORIZED)
							.entity(new ErrorResponse("Invalid auth token"))
							.build();
					case ServiceException se when se.getCode().equals(ServiceException.AUTH_EXPIRED) ->
							Response.status(Response.Status.UNAUTHORIZED)
									.entity(new ErrorResponse(e.getMessage()))
									.build();
					case ServiceException se when se.getCode().equals(ServiceException.NOT_FOUND) ->
							Response.status(Response.Status.NOT_FOUND)
									.entity(new ErrorResponse(e.getMessage()))
									.build();
					default -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
				})
				.get();
	}

	public record AccountInfoResponse(String id, String name, String displayName, String cosId,
																		String domainId, AccountStatus status, boolean isGlobalAdmin,
																		boolean isExternal, String locale) {

		public static AccountInfoResponse from(Account account) {
			try {
				return new AccountInfoResponse(account.getId(), account.getName(), account.getDisplayName(),
						account.getCOSId(), account.getDomainId(), account.getAccountStatus(),
						account.isIsAdminAccount(), account.isAccountExternal(),
						account.getLocaleAsString()
				);
			} catch (ServiceException e) {
				// TODO: isAccountExternal throws exception, how to handle?
				throw new RuntimeException(e);
			}
		}
	}
}
