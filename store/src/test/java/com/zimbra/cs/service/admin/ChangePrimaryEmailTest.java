// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.Key;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.ChangePrimaryEmailRequest;
import com.zimbra.soap.type.AccountSelector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChangePrimaryEmailTest extends MailboxTestSuite {

	public String testName;
	private static Account oldAccount;
	private static Account admin;

	@BeforeAll
	public static void init() throws Exception {
		oldAccount = createAccount().create();
		admin = createAccount().withAttribute(Provisioning.A_zimbraIsAdminAccount, true).create();
	}

	@Test
	void testChangePrimaryEmail() throws Exception {
		admin.setIsAdminAccount(true);
		final String domainName = oldAccount.getDomainName();
		ChangePrimaryEmailRequest request = new ChangePrimaryEmailRequest(
				AccountSelector.fromName(oldAccount.getName()), "new@" + domainName);
		Element req = JaxbUtil.jaxbToElement(request);
		ChangePrimaryEmail handler = new ChangePrimaryEmail();
		handler.setResponseQName(AdminConstants.CHANGE_PRIMARY_EMAIL_REQUEST);
		handler.handle(req, ServiceTestUtil.getRequestContext(admin));
		//getting new account with old name as mock provisioning doesn't rename account
		Account newAcc = Provisioning.getInstance().get(Key.AccountBy.name, oldAccount.getName());
		String change = newAcc.getPrimaryEmailChangeHistory()[0];
		assertEquals(oldAccount.getName(), change.substring(0, change.indexOf("|")));
		assertEquals(oldAccount.getName(), newAcc.getAliases()[0]);
	}

}
