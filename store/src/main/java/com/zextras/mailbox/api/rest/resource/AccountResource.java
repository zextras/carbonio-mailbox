/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Map;
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
	@Operation(summary = "Get Account Info", description = "Returns account info by ID")
	@ApiResponse(responseCode = "200", description = "Account Info",
			content = @Content(schema = @Schema(implementation = AccountInfoResponse.class)))
	@ApiResponse(responseCode = "404", description = "Account not found",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@Path("/{id}")
	public Response getAccountInfo(
			@Parameter(description = "The account ID") @PathParam("id") String id) {
		return accountService.getAccount(id)
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
		return accountService.getAccountAndAuthToken(token)
				.map(tuple -> Response.ok(AccountInfoResponse.from(tuple._1(), tuple._2())).build())
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
		return (hasIds
				? accountService.getAccounts(batchRequest.ids())
				: accountService.getAccountsByEmails(batchRequest.emails()))
				.map(accounts -> Response.ok(accounts.stream().map(AccountInfoResponse::from).collect(Collectors.toList())).build())
				.recover(e -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build())
				.get();
	}

	public record BatchRequest(List<String> ids, List<String> emails) {}

	public record CarbonioFeature(String name, Object value) {
		public static CarbonioFeature map(Entry<String, Object> entry) {
			return new CarbonioFeature(entry.getKey(), entry.getValue());
		}
	}

	public record AccountInfoResponse(String id, String name, String displayName, String cosId,
																		String domainId, String domain, AccountStatus status, boolean isGlobalAdmin,
																		boolean isExternal, String locale, Map<String, Boolean> features,
																		Map<String, String> capabilities, Long sessionLifetimeMs) {

		public static AccountInfoResponse from(Account account) {
			try {
				var features = account.getAttrs().entrySet().stream()
						.filter(entry -> entry.getKey().startsWith("carbonioFeature"))
						.collect(Collectors.toMap(Entry::getKey, entry -> Boolean.parseBoolean(entry.getValue().toString())));
				var capabilities = account.getAttrs().entrySet().stream()
						.filter(entry -> {
							String key = entry.getKey();
							return key.startsWith("carbonioWsc") || key.startsWith("carbonioFiles")
									|| key.startsWith("carbonioTasks") || key.startsWith("carbonioDocs")
									|| key.startsWith("carbonioPreview");
						})
						.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
				boolean isExternal;
				try {
					isExternal = account.isAccountExternal();
				} catch (ServiceException e) {
					// If we cannot determine whether the account is external, default to true.
					// An internal account failing this check implies a non-standard transport
					// configuration, which is more consistent with an external account.
					isExternal = true;
				}
				return new AccountInfoResponse(account.getId(), account.getName(), account.getDisplayName(),
						account.getCOSId(), account.getDomainId(), account.getDomainName(),
						account.getAccountStatus(), account.isIsAdminAccount(), isExternal,
						account.getLocaleAsString(), features, capabilities, null);
			} catch (ServiceException e) {
				throw new RuntimeException(e);
			}
		}

		public static AccountInfoResponse from(Account account, AuthToken authToken) {
			try {
				var features = account.getAttrs().entrySet().stream()
						.filter(entry -> entry.getKey().startsWith("carbonioFeature"))
						.collect(Collectors.toMap(Entry::getKey, entry -> Boolean.parseBoolean(entry.getValue().toString())));
				var capabilities = account.getAttrs().entrySet().stream()
						.filter(entry -> {
							String key = entry.getKey();
							return key.startsWith("carbonioWsc") || key.startsWith("carbonioFiles")
									|| key.startsWith("carbonioTasks") || key.startsWith("carbonioDocs")
									|| key.startsWith("carbonioPreview");
						})
						.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
				boolean isExternal;
				try {
					isExternal = account.isAccountExternal();
				} catch (ServiceException e) {
					isExternal = true;
				}
				long sessionLifetimeMs = authToken.getExpires() - System.currentTimeMillis();
				return new AccountInfoResponse(account.getId(), account.getName(), account.getDisplayName(),
						account.getCOSId(), account.getDomainId(), account.getDomainName(),
						account.getAccountStatus(), account.isIsAdminAccount(), isExternal,
						account.getLocaleAsString(), features, capabilities, sessionLifetimeMs);
			} catch (ServiceException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
