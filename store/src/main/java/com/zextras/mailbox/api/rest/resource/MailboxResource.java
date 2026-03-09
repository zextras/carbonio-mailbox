/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.api.rest.service.MailboxService;
import com.zimbra.common.service.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vavr.control.Try;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/accounts/mail")
public class MailboxResource {

  private final MailboxService mailboxService;
  private final AccountService accountService;

  public MailboxResource(MailboxService mailboxService, AccountService accountService) {
    this.mailboxService = mailboxService;
    this.accountService = accountService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get Mailbox usage", description = "Returns mail usage for the account mailbox")
  @ApiResponse(responseCode = "200", description = "Mailbox usage in bytes")
  @ApiResponse(responseCode = "404", description = "Account not found")
  @ApiResponse(responseCode = "500", description = "Internal server error")
  @Path("/usage/{accountId}")
  public Response getMailUsage(@Parameter(description = "The account ID") @PathParam("accountId") String accountId) {
    return accountService.getAccount(accountId)
            .mapTry(mailboxService::getMailboxUsage)
            .map(used -> Response.ok(new MailUsageResponse(used)).build())
            .recover(e -> switch (e) {
              case ServiceException se when se.getCode().equals(ServiceException.NOT_FOUND) ->
                      Response.status(Response.Status.NOT_FOUND)
                              .entity(new ErrorResponse(e.getMessage()))
                              .build();
              default -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
            })
            .get();
  }

  public record MailUsageResponse(long used) {
  }

}
