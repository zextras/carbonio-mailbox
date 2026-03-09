/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
	@ApiResponse(responseCode = "200", description = "Account Info")
	@ApiResponse(responseCode = "404", description = "Account not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@Path("/{accountId}/info")
	public Response getAccountInfo(@Parameter(description = "The account ID") @PathParam("accountId") String accountId) {
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

	public record AccountInfoResponse(String id, String name, String cosId, String domainId, boolean isGlobalAdmin) {
		public static AccountInfoResponse from(Account account) {
			return new AccountInfoResponse(account.getId(), account.getName(), account.getCOSId(), account.getDomainId(), account.isIsAdminAccount());
		}
	}
}
