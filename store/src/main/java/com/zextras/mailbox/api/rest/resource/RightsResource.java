/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource;

import com.zextras.mailbox.api.rest.resource.dto.RightCheckRequest;
import com.zextras.mailbox.api.rest.resource.dto.RightCheckRequest.Subject;
import com.zextras.mailbox.api.rest.resource.dto.RightCheckRequest.Target;
import com.zextras.mailbox.api.rest.resource.dto.RightCheckResponse;
import com.zextras.mailbox.api.rest.response.ErrorResponse;
import com.zextras.mailbox.api.rest.service.RightsService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vavr.control.Try;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Dependent
@Path("/rights")
public class RightsResource {

	@Inject
	private RightsService rightsService;

	@POST
	@Path("/check")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Check Right", description = "Tells whether the subject holds a right (user or admin, as defined in the rights catalogue) on the target. "
			+ "The subject is identified by exactly one of 'token' or 'accountId'; the target type is a right target type "
			+ "(e.g. account, domain, cos). A missing target or subject is reported as not allowed.")
	@ApiResponse(responseCode = "200", description = "Authorization decision",
			content = @Content(schema = @Schema(implementation = RightCheckResponse.class)))
	@ApiResponse(responseCode = "400", description = "Invalid request, unknown right or target type, or right not applicable to the target type",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "401", description = "Invalid or expired token",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public Response check(RightCheckRequest request) {
		final String invalidReason = invalidReason(request);
		if (invalidReason != null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new ErrorResponse(invalidReason))
					.build();
		}
		return decide(request)
				.map(allowed -> Response.ok(new RightCheckResponse(allowed)).build())
				.recover(RightsResource::toErrorResponse)
				.get();
	}

	private Try<Boolean> decide(RightCheckRequest request) {
		final Subject subject = request.subject();
		final Target target = request.target();
		return isPresent(subject.token())
				? rightsService.tokenHasRight(subject.token(), request.right(), target.type(), target.id())
				: rightsService.accountHasRight(subject.accountId(), request.right(), target.type(), target.id());
	}

	private static String invalidReason(RightCheckRequest request) {
		if (request == null || request.subject() == null || request.target() == null
				|| !isPresent(request.right()) || !isPresent(request.target().type())) {
			return "Missing subject, right or target type";
		}
		final Subject subject = request.subject();
		if (isPresent(subject.token()) == isPresent(subject.accountId())) {
			return "Provide exactly one of 'token' or 'accountId' as subject";
		}
		return null;
	}

	private static boolean isPresent(String value) {
		return value != null && !value.isEmpty();
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
			case ServiceException se when se.getCode().equals(ServiceException.INVALID_REQUEST) ->
					Response.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorResponse(e.getMessage()))
							.build();
			default -> Response.serverError().entity(new ErrorResponse(e.getMessage())).build();
		};
	}
}
