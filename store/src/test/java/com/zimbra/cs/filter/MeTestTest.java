// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.jsieve.MeTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MeTest}.
 *
 * @author ysasaki
 */
public final class MeTestTest extends MailboxTestSuite {

	private static Account account;
	private static String mail;

	@BeforeAll
	public static void init() throws Exception {
		account = createAccount().create();
		mail = account.getName();
	}


	@Test
	void meInTo() throws Exception {

		RuleManager.clearCachedRules(account);

		account.setMailSieveScript("if me :in \"To\" { tag \"Priority\"; }");
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(getTo(), false), 0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Message msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
	}

	private static byte [] getTo() {
		return ("To: " + mail).getBytes();
	}

	@Test
	void meInToMultiRecipient() throws Exception {

		RuleManager.clearCachedRules(account);

		account.setMailSieveScript("if me :in \"To\" { tag \"Priority\"; }");
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(("To: foo@zimbra.com, " + mail + ", bar@zimbra.com").getBytes(), false),
				0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Message msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
	}

	@Test
	void quotedMultiRecipientTo() throws Exception {

		RuleManager.clearCachedRules(account);

		account.setMailSieveScript("if me :in \"To\" { tag \"Priority\"; }");
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(
						("To: \"bar, foo\" <foo@zimbra.com>, \"user, test\" <" + mail
								+ ">, \"aaa bbb\" <aaabbb@test.com>").getBytes(),
						false), 0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Message msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
	}


	@Test
	void meInToOrCc() throws Exception {

		RuleManager.clearCachedRules(account);

		account.setMailSieveScript("if me :in \"To,Cc\" { tag \"Priority\"; }");
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(getTo(), false), 0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Message msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));

		ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(("Cc: " + mail).getBytes(), false), 0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
	}

	@Test
	void meInToOrCcMultiRecipient() throws Exception {

		RuleManager.clearCachedRules(account);

		account.setMailSieveScript("if me :in \"To,Cc\" { tag \"Priority\"; }");
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(("To: foo@zimbra.com, " + mail).getBytes(), false), 0,
				account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Message msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));

		ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(("Cc: foo@zimbra.com, " + mail).getBytes(), false), 0,
				account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
	}

	@Test
	void quotedMultiRecipientToOrCc() throws Exception {

		RuleManager.clearCachedRules(account);

		account.setMailSieveScript("if me :in \"To,Cc\" { tag \"Priority\"; }");
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

		List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(
						("To: \"bar, foo\" <foo@zimbra.com>, \"test user\" <" + mail + ">").getBytes(), false),
				0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		Message msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));

		ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(
						("Cc: \"foo bar\" <foo@zimbra.com>, \"user, test\" <" + mail + ">").getBytes(), false),
				0, account.getName(),
				new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		assertEquals(1, ids.size());
		msg = mbox.getMessageById(null, ids.get(0).getId());
		assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
	}

}
