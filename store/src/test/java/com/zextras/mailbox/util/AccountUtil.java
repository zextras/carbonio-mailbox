/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.UUID;

public class AccountUtil {

	// Use it with parsimony. Introduced to migrate old legacy tests easily
	public static Account createAccount() throws ServiceException {
		return Provisioning.getInstance()
				.createAccount(UUID.randomUUID() + "@" + UUID.randomUUID() + ".com", "secret",
						new HashMap<String, Object>());
	}
}
