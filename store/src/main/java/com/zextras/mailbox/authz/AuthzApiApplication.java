/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz;

import com.zextras.mailbox.api.InternalApiInitializationException;
import com.zextras.mailbox.api.InternalApiServices;
import com.zextras.mailbox.authz.resource.RightsResource;
import com.zextras.mailbox.authz.service.RightsService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
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

	private final Set<Object> singletons;

	public AuthzApiApplication() {
		final RightsService rightsService = new RightsService(
				Provisioning::getInstance,
				AccessManager::getInstance,
				() -> {
					try {
						return RightManager.getInstance();
					} catch (ServiceException e) {
						throw new InternalApiInitializationException(e);
					}
				},
				InternalApiServices.accountService(InternalApiServices.mailboxService()));
		singletons = Set.of(new RightsResource(rightsService));
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
