/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.SendNotificationRequest;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.MailNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/accounts/mail/notifications")
public class MailNotificationResource {

  private final MailNotificationService mailNotificationService;

  public MailNotificationResource(MailNotificationService mailNotificationService) {
    this.mailNotificationService = mailNotificationService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Send notification email to multiple accounts")
  @ApiResponse(responseCode = "200", description = "Request processed; per-recipient outcomes are server-side only",
      content = @Content(schema = @Schema(implementation = SendNotificationResponse.class)))
  @ApiResponse(responseCode = "400", description = "Missing or empty subject or body",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "500", description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public Response sendNotifications(SendNotificationRequest request) {
    return mailNotificationService.send(request)
        .map(accepted -> Response.ok(new SendNotificationResponse(accepted)).build())
        .recover(e -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build())
        .get();
  }

  public record SendNotificationResponse(long accepted) {
  }
}
