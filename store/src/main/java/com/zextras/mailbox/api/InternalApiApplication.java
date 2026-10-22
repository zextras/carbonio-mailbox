/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import com.zextras.mailbox.api.rest.resource.AccountResource;
import com.zextras.mailbox.api.rest.resource.CosResource;
import com.zextras.mailbox.api.rest.resource.MailboxResource;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.api.rest.service.MailboxService;
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

	private final Set<Object> singletons;

	public InternalApiApplication() {
		final MailboxService mailboxService = InternalApiServices.mailboxService();
		final AccountService accountService = InternalApiServices.accountService(mailboxService);
		singletons = Set.of(
				new MailboxResource(mailboxService, accountService),
				new AccountResource(accountService),
				new CosResource(InternalApiServices.cosService()));
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
