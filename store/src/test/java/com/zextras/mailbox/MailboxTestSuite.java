/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.CreateAccount;
import com.zextras.mailbox.util.CreateAccount.Factory;
import com.zextras.mailbox.util.MailboxSetupHelper;
import com.zextras.mailbox.util.MailboxTestData;
import com.zextras.mailbox.util.MailboxTestExtension;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class MailboxTestSuite {

	protected static final String DEFAULT_DOMAIN_NAME = "test.com";
	protected static final String DEFAULT_DOMAIN_ID = "f4806430-b434-4e93-9357-a02d9dd796b8";
	protected static final String SERVER_NAME = "localhost";
	private static final MailboxTestData mailboxTestData = new MailboxTestData(SERVER_NAME,
			DEFAULT_DOMAIN_NAME,
			DEFAULT_DOMAIN_ID);

	@RegisterExtension
	private static final MailboxTestExtension mailboxTestExtension = new MailboxTestExtension(mailboxTestData,
			MailboxSetupHelper.create());

	protected static CreateAccount createAccount() {
		return new Factory(Provisioning.getInstance(),
				MailboxTestSuite.DEFAULT_DOMAIN_NAME).get();
	}

	protected static Server createProxyServer() throws ServiceException {
		final String serverName = UUID.randomUUID() + ".com";
		return Provisioning.getInstance().createServer(
				serverName, new HashMap<>() {
					{
						put(ZAttrProvisioning.A_cn, serverName);
						put(ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_PROXY);
					}
				});
	}
	protected static AccountAction.Factory getAccountActionFactory() throws ServiceException {
		return AccountAction.Factory.getDefault();
	}

	protected void clearData() throws Exception {
		mailboxTestExtension.clearData();
	}
	protected void initData() throws Exception {
		mailboxTestExtension.initData();
	}
}
