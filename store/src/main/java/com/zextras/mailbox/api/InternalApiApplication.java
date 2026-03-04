/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.glassfish.jersey.server.ResourceConfig;

@OpenAPIDefinition(
		info = @Info(
				title = "Carbonio Mailbox Internal API",
				version = "1.0",
				description = "Service-to-service REST API for carbonio-mailbox"
		)
)
public class InternalApiApplication extends ResourceConfig {

	public InternalApiApplication() {
		packages("com.zextras.mailbox.api.rest");
		register(OpenApiResource.class);
	}
}
