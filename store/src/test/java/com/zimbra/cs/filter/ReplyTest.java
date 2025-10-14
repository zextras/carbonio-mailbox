// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.DirectoryEntryVisitor;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.DirectInsertionMailboxManager;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author zimbra
 */

@Disabled
public class ReplyTest extends MailboxTestSuite {

	private Account getAccount1() throws Exception {
		return createAccount()
				.withAttribute(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE")
				.withAttribute(Provisioning.A_zimbraSieveRequireControlEnabled, "TRUE").create();
	}

	private Account getAccount2() throws Exception {
		return createAccount()
				.withAttribute(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE").create();
	}

	@Test
	void testReply() throws Exception {
		Account acct1 = getAccount1();
		Account acct2 = getAccount2();

		String sampleMsg = getSampleMsg(acct2, acct1);

		Mailbox mbox1 = getMailboxManager().getMailboxByAccount(acct1);
		Mailbox mbox2 = getMailboxManager().getMailboxByAccount(acct2);

		RuleManager.clearCachedRules(acct1);
		String filterScript = "if anyof (true) { reply \"Hello World\"" + "    stop;" + "}";
		acct1.setMailSieveScript(filterScript);
		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox1),
				mbox1, new ParsedMessage(sampleMsg.getBytes(), false), 0, acct1.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
				.getIds(MailItem.Type.MESSAGE).get(0);
		Message notifyMsg = mbox2.getMessageById(null, item);
		assertEquals("Hello World", notifyMsg.getFragment());
	}

	private static String getSampleMsg(Account account2, Account acct1) {
		return "from: " + account2.getName() + "\n" + "Return-Path: " + account2.getName() + "\n"
				+ "Subject: Hello\n" + "to: " + acct1.getName() + "\n";
	}

	@Test
	void testReplyMimeVariables() throws Exception {
		Account acct1 = getAccount1();
		Account acct2 = getAccount2();

		String sampleMsg = getSampleMsg(acct2, acct1);

		Mailbox mbox1 = getMailboxManager().getMailboxByAccount(acct1);
		Mailbox mbox2 = getMailboxManager().getMailboxByAccount(acct2);

		RuleManager.clearCachedRules(acct1);
		String filterScript = "require \"variables\";\n"
				+ "set \"var\" \"World\";\n"
				+ "if anyof (true) { reply \"${Subject} ${var}\"" + "    stop;" + "}";
		acct1.setMailSieveScript(filterScript);
		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox1),
				mbox1, new ParsedMessage(sampleMsg.getBytes(), false), 0, acct1.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
				.getIds(MailItem.Type.MESSAGE).get(0);
		Message notifyMsg = mbox2.getMessageById(null, item);
		assertEquals("Hello World", notifyMsg.getFragment());
	}

	private static @NotNull DirectInsertionMailboxManager getMailboxManager()
			throws ServiceException {
		return new DirectInsertionMailboxManager();
	}
}
