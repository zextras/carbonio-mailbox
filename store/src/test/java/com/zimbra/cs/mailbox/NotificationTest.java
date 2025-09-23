// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning.PrefExternalSendersType;
import com.zimbra.cs.account.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class NotificationTest extends MailboxTestSuite {

	private Account acct1;


	@BeforeEach
	public void init() throws Exception {
		acct1 = createAccount().create();
	}

	@Test
	void testOOOWhenSpecificDomainSenderNotSet() throws Exception {
		acct1.setPrefOutOfOfficeSuppressExternalReply(true);
		acct1.unsetInternalSendersDomain();
		acct1.unsetPrefExternalSendersType();
		Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
		boolean skipOOO = Notification.skipOutOfOfficeMsg("test3@external.com", acct1, mbox1);
		assertTrue(skipOOO);
	}

	@Test
	void testOOOWhenSpecificDomainSenderIsSet() throws Exception {
		acct1.setPrefOutOfOfficeSuppressExternalReply(true);
		acct1.setPrefExternalSendersType(PrefExternalSendersType.INSD);
		String[] domains = {"external.com"};
		acct1.setPrefOutOfOfficeSpecificDomains(domains);
		Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
		boolean skipOOO = Notification.skipOutOfOfficeMsg("test3@external.com", acct1, mbox1);
		assertFalse(skipOOO);
	}

	@Test
	void testOOOMsgWhenSpecificDomainSenderIsSetWithSpecificDomainSender() throws Exception {
		acct1.setPrefOutOfOfficeExternalReplyEnabled(true);
		acct1.setPrefExternalSendersType(PrefExternalSendersType.INSD);
		String[] domains = {"external.com"};
		acct1.setPrefOutOfOfficeSpecificDomains(domains);
		acct1.setInternalSendersDomain(domains);
		Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
		boolean customMsg = Notification.sendOutOfOfficeExternalReply("test3@external.com", acct1,
				mbox1);
		assertTrue(customMsg);
	}

	@Test
	void testOOOMsgWhenSpecificDomainSenderIsSetWithInternalSender() throws Exception {
		acct1.setPrefOutOfOfficeExternalReplyEnabled(true);
		acct1.setPrefExternalSendersType(PrefExternalSendersType.INSD);
		String[] domains = {"external.com"};
		acct1.setPrefOutOfOfficeSpecificDomains(domains);
		Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
		final Account internalAccount = createAccount().create();
		boolean customMsg = Notification.sendOutOfOfficeExternalReply(internalAccount.getName(), acct1, mbox1);
		assertFalse(customMsg);
	}
}
