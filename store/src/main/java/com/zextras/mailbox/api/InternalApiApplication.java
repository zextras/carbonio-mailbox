/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import com.zextras.mailbox.api.rest.resource.AccountResource;
import com.zextras.mailbox.api.rest.resource.MailboxResource;
import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.api.rest.service.MailboxService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import javax.ws.rs.core.Application;
import java.util.Set;

@OpenAPIDefinition(
		info = @Info(
				title = "Carbonio Mailbox Internal API",
				version = "1.0",
				description = "Service-to-service REST API for carbonio-mailbox"
		)
)
public class InternalApiApplication extends Application {

	@Override
	public Set<Object> getSingletons() {
		final AccountService accountService = new AccountService(Provisioning::getInstance);
		final MailboxService mailboxService = new MailboxService(
				() -> {
					try {
						return MailboxManager.getInstance();
					} catch (ServiceException e) {
						throw new RuntimeException(e);
					}
				},
				() -> {
					try {
						return SoapProvisioning.getAdminInstance();
					} catch (ServiceException e) {
						throw new RuntimeException(e);
					}
				},
				Provisioning::getInstance, accountService);
		return Set.of(new MailboxResource(mailboxService), new AccountResource(accountService));
	}
}
