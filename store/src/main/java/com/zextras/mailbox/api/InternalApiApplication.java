/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import com.zextras.mailbox.api.rest.PingResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.util.Set;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
		info = @Info(
				title = "Carbonio Mailbox Internal API",
				version = "1.0",
				description = "Service-to-service REST API for carbonio-mailbox"
		)
)
public class InternalApiApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		return Set.of(PingResource.class, OpenApiResource.class);
	}
}
