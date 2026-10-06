/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz;

import com.zextras.mailbox.authz.resource.RightsResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.util.Set;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
		info = @Info(
				title = "Carbonio Authz API",
				version = "1.0",
				description = "Service-to-service authorization API backed by the mailbox rights catalogue"
		)
)
public class AuthzApiApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		return Set.of(RightsResource.class);
	}
}
