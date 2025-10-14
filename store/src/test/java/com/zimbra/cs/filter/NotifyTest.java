// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author zimbra
 */


public class NotifyTest extends MailboxTestSuite {

	@BeforeAll
	public static void init() throws Exception {
		// this MailboxManager does everything except actually send mail
		MailboxManager.setInstance(new DirectInsertionMailboxManager());
	}

	@Test
	void filterValidToField() {
		try {

			Account acct1 = createAccount().withAttribute(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE").create();;
			Account acct2 = createAccount().withAttribute(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE").create();;

			Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(
					acct1);
			Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(
					acct2);
			RuleManager.clearCachedRules(acct1);
			String filterScript =
					"require [\"enotify\"];if anyof (true) { notify \"" + acct2.getName() + "\" \"\" \"Hello World\""
							+ "[\"*\"];" + "    keep;" + "}";
			acct1.setMailSieveScript(filterScript);
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
					new OperationContext(mbox1), mbox1, new ParsedMessage(
							("To: " + acct1.getName()).getBytes(), false), 0, acct1
							.getName(), new DeliveryContext(),
					Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());
			Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
					.getIds(MailItem.Type.MESSAGE).get(0);
			Message notifyMsg = mbox2.getMessageById(null, item);
			assertEquals("Hello World", notifyMsg.getFragment());
			assertEquals("text/plain; charset=us-ascii", notifyMsg
					.getMimeMessage().getContentType());
		} catch (Exception e) {
			fail("No exception should be thrown");
		}

	}

	@Test
	void testNotifyMailtoWithMimeVariable() {

		try {
			Account acct1 = createAccount().withAttribute(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE").create();;
			Account acct2 = createAccount().withAttribute(Provisioning.A_zimbraSieveNotifyActionRFCCompliant, "FALSE").create();;

			String sampleMsg = "from: abc@zimbra.com\n"
					+ "Subject: Hello\n"
					+ "to: " + acct1.getName() + "\n";
			String filterScript = "require [\"enotify\", \"variables\"];\n"
					+ "set \"to\" \"nick\";\n"
					+ "if anyof (header :contains [\"Subject\"] \"Hello\") {\n"
					+ "notify \"" + acct2.getName() + "\" \"\" \"${SUBJECT} ${to}\"\n"
					+ "[\"*\"];"
					+ "keep;"
					+ "stop; }";
			Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
			Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);
			RuleManager.clearCachedRules(acct1);
			acct1.setMailSieveScript(filterScript);
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox1),
					mbox1, new ParsedMessage(sampleMsg.getBytes(), false), 0, acct1.getName(),
					new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());
			Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
					.getIds(MailItem.Type.MESSAGE).get(0);
			Message notifyMsg = mbox2.getMessageById(null, item);
			assertEquals("Hello nick", notifyMsg.getFragment());
		} catch (Exception e) {
			fail("No exception should be thrown");
		}
	}
}
