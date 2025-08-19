/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateAccount {

	private final Provisioning provisioning;
	private String username = UUID.randomUUID().toString();
	private String password = "password";
	private String domain;
	private final Map<String, Object> attributes =
			new HashMap<>(Map.of(Provisioning.A_zimbraMailHost, "localhost"));

	private CreateAccount(Provisioning provisioning, String domain) {
		this.provisioning = provisioning;
		this.domain = domain;
	}

	public static class Factory {

		private final Provisioning provisioning;
		private final String domain;

		public Factory(Provisioning provisioning, String domain) {
			this.provisioning = provisioning;
			this.domain = domain;
		}

		public CreateAccount get() {
			return new CreateAccount(provisioning, domain);
		}
	}

	public CreateAccount withUsername(String username) {
		this.username = username;
		return this;
	}

	public CreateAccount withPassword(String password) {
		this.password = password;
		return this;
	}

	public CreateAccount withDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public CreateAccount withAttribute(String name, Object value) {
		this.attributes.put(name, value);
		return this;
	}

	public CreateAccount asGlobalAdmin() {
		this.attributes.put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
		return this;
	}

	public Account create() throws ServiceException {
		return provisioning.createAccount(this.username + "@" + this.domain, password, attributes);
	}
}
