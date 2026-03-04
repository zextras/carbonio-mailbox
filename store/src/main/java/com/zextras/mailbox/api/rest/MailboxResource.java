/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/accounts/mail")
public class MailboxResource {

	private final MailboxService mailboxService;

	public MailboxResource(MailboxService mailboxService) {
		this.mailboxService = mailboxService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get Mailbox usage", description = "Returns mail usage for the account mailbox")
	@ApiResponse(responseCode = "200", description = "Mailbox usage in bytes")
	@ApiResponse(responseCode = "404", description = "Account not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@Path("/usage/{accountId}")
	public Response getMailUsage(@Parameter(description = "The account ID") @PathParam("accountId") String accountId) {
		return mailboxService.getMailUsage(accountId)
				.map(used -> Response.ok(new MailUsageResponse(used)).build())
				.recover(e -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build())
				.get();
	}

	public record MailUsageResponse(long used) {}

	public record ErrorResponse(String error) {}
}
