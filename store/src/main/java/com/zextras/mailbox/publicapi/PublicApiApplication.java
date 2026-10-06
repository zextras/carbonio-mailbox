/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi;

import com.zextras.mailbox.publicapi.resource.PublicAccountResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.util.Set;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
		info = @Info(
				title = "Carbonio Mailbox Public API",
				version = "1.0",
				description = "Service-to-service API acting on behalf of the user owning the forwarded auth token"
		)
)
public class PublicApiApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		return Set.of(PublicAccountResource.class);
	}
}
