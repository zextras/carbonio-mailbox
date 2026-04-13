/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.AccountInfoResponse;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
	@Operation(summary = "Get Account by Email", description = "Returns account info by email address")
	@ApiResponse(responseCode = "200", description = "Account Info",
			content = @Content(schema = @Schema(implementation = AccountInfoResponse.class)))
	@ApiResponse(responseCode = "400", description = "Missing email query parameter",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Account not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response getAccountByEmail(
			@Parameter(description = "The account email address") @QueryParam("email") String email) {
		if (email == null || email.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Missing required query parameter: email"))
					.build();
		}
		return accountService.getAccountByEmail(email)
				.mapTry(account -> Response.ok(AccountInfoResponse.from(account)).build())
				.recover(AccountResource::toErrorResponse)
				.get();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Account Info", description = "Returns account info by ID")
	@ApiResponse(responseCode = "200", description = "Account Info",
			content = @Content(schema = @Schema(implementation = AccountInfoResponse.class)))
	@ApiResponse(responseCode = "404", description = "Account not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@Path("/{id}/info")
	public Response getAccountInfo(
			@Parameter(description = "The account ID") @PathParam("id") String id) {
		return accountService.getAccount(id)
				.mapTry(account -> Response.ok(AccountInfoResponse.from(account)).build())
				.recover(AccountResource::toErrorResponse)
				.get();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Authenticated Account Info", description = "Returns account info for the authenticated user")
	@ApiResponse(responseCode = "200", description = "Account Info")
	@ApiResponse(responseCode = "401", description = "Invalid or missing auth token")
	@ApiResponse(responseCode = "404", description = "Account not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@Path("/myself")
	public Response getMyAccountInfo(
			@CookieParam("ZM_AUTH_TOKEN") String authTokenCookie,
			@CookieParam("ZM_ADMIN_AUTH_TOKEN") String adminAuthTokenCookie) {
		final String token = adminAuthTokenCookie != null ? adminAuthTokenCookie : authTokenCookie;
		if (token == null || token.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorResponse("Missing auth token"))
					.build();
		}
		return accountService.getAuthToken(token)
				.mapTry(authToken -> Response.ok(AccountInfoResponse.from(authToken)).build())
				.recover(AccountResource::toErrorResponse)
				.get();
	}

	@POST
	@Path("/batch")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Batch Get Accounts", description = "Returns account info for a list of IDs or emails. Exactly one of 'ids' or 'emails' must be provided. Unknown entries are silently skipped.")
	@ApiResponse(responseCode = "200", description = "List of Account Info",
			content = @Content(schema = @Schema(implementation = AccountInfoResponse.class)))
	@ApiResponse(responseCode = "400", description = "Invalid request: exactly one of 'ids' or 'emails' required, max 100 entries",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response batchGetAccounts(BatchRequest batchRequest) {
		if (batchRequest == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Missing request body"))
					.build();
		}
		boolean hasIds = batchRequest.ids() != null && !batchRequest.ids().isEmpty();
		boolean hasEmails = batchRequest.emails() != null && !batchRequest.emails().isEmpty();
		if (hasIds == hasEmails) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Provide exactly one of 'ids' or 'emails'"))
					.build();
		}
		List<String> inputList = hasIds ? batchRequest.ids() : batchRequest.emails();
		if (inputList.size() > 100) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse("Too many entries: max 100 allowed"))
					.build();
		}
		final Try<List<Account>> accountsList = hasIds
				? accountService.getAccounts(batchRequest.ids())
				: accountService.getAccountsByEmails(batchRequest.emails());
		return accountsList
				.mapTry(AccountResource::batchResponse)
				.recover(AccountResource::toErrorResponse)
				.get();
	}

	private static Response batchResponse(List<Account> accounts) throws ServiceException {
		final List<AccountInfoResponse> response = new ArrayList<>();
		for (Account account : accounts) {
			response.add(AccountInfoResponse.from(account));
		}
		return Response.ok(response).build();
	}

	@GET
	@Path("/{id}/shared-accounts")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Shared Accounts", description = "Returns accounts that share folders with the given account (via mountpoints)")
	@ApiResponse(responseCode = "200", description = "List of shared accounts",
			content = @Content(schema = @Schema(implementation = SharedAccountResponse.class)))
	@ApiResponse(responseCode = "404", description = "Account not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response getSharedAccounts(
			@Parameter(description = "The account ID") @PathParam("id") String id) {
		return accountService.getSharedAccounts(id)
				.map(accounts -> Response.ok(
						accounts.stream().map(SharedAccountResponse::from).collect(Collectors.toList())).build())
				.recover(AccountResource::toErrorResponse)
				.get();
	}

	private static Response toErrorResponse(Throwable e) {
		return switch (e) {
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
		};
	}

	public record SharedAccountResponse(String id, String email, String domain, String cosId) {
		public static SharedAccountResponse from(Account account) {
			return new SharedAccountResponse(account.getId(), account.getName(),
					account.getDomainName(), account.getCOSId());
		}
	}

	public record BatchRequest(List<String> ids, List<String> emails) {}

}
