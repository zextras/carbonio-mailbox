// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.DirectInsertionMailboxManager;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.file.VolumeBlob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import qa.unittest.TestUtil;

class FileIntoCopyTest extends MailboxTestSuite {

	@TempDir
	private Path tempDir;

	@BeforeEach
	public void setUp() throws Exception {
		MailboxManager.setInstance(new DirectInsertionMailboxManager());
	}


	@Test
	void testCopyFileInto() {
		String filterScript = "require [\"copy\", \"fileinto\"];\n"
				+ "if header :contains \"Subject\" \"test\" { fileinto :copy \"Junk\"; }";
		try {
			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScript);
			String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
					+ "\n" + "Hello World.";
			final ParsedMessage pm = new ParsedMessage(raw.getBytes(), false);
			StoreManager.getInstance().storeIncoming(pm.getRawInputStream());
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, pm, 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);

			assertEquals(2, ids.size());

			Message msg = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals("Test", msg.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	@Test
	void testPlainFileInto() {
		String filterPlainFileintoScript = "require [\"fileinto\"];\n"
				+ "if header :contains \"Subject\" \"test\" { fileinto \"Junk\"; }";
		try {

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterPlainFileintoScript);
			String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
					+ "\n" + "Hello World.";
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
					new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());
			Message msg = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals("Test", msg.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	@Test
	void testPlainFileIntoNonExistingFolder() {
		String filterPlainFileintoScript = "require [\"fileinto\"];\n"
				+ "if header :contains \"Subject\" \"test\" { fileinto \"HelloWorld\"; }";
		try {

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterPlainFileintoScript);
			String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
					+ "\n" + "Hello World.";
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
					new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());
			Message msg = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals("Test", msg.getSubject());
			com.zimbra.cs.mailbox.Folder folder = mbox.getFolderById(null, msg.getFolderId());
			assertEquals("HelloWorld", folder.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	/*
	 * fileinto :copy foo; if header :contains "Subject" "test" { fileinto bar;
	 * }
	 *
	 * if message has "Subject: Test" ==> it should be stored in "foo" and "bar"
	 */
	@Test
	void testCopyFileIntoPattern1Test() throws Exception {
		String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
				+ "fileinto :copy \"foo\";\n" + "if header :contains \"Subject\" \"Test\" {\n"
				+ "fileinto \"bar\"; }";

		final Account account = createAccount().create();
		RuleManager.clearCachedRules(account);
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
		account.setMailSieveScript(filterScriptPattern1);
		String rawTest = getRawTest();
		final ParsedMessage pm = new ParsedMessage(rawTest.getBytes(), false);
		final DeliveryOptions dopt = new DeliveryOptions();
		dopt.setFolderId(Mailbox.ID_FOLDER_INBOX);
		StoreManager.getInstance().storeIncoming(pm.getRawInputStream());
		final DeliveryContext deliveryCtxt = fakeDeliveryContext();
		RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
				mbox, pm, 0, account.getName(),
				deliveryCtxt, Mailbox.ID_FOLDER_INBOX, true);
		// message should not be stored in inbox
		assertNull(
				mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE));
		// message should be stored in foo
		Integer item = Objects.requireNonNull(mbox
				.getItemIds(null,
						mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
				.getIds(Type.MESSAGE)).get(0);
		Message msg = mbox.getMessageById(null, item);
		assertEquals("Hello World", msg.getFragment());
		// message should be stored in bar
		item = Objects.requireNonNull(mbox
				.getItemIds(null,
						mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "bar").getId())
				.getIds(Type.MESSAGE)).get(0);
		msg = mbox.getMessageById(null, item);
		assertEquals("Hello World", msg.getFragment());
	}

	private DeliveryContext fakeDeliveryContext() throws IOException {
		final DeliveryContext deliveryCtxt = new DeliveryContext();
		final File file = new File(Paths.get(tempDir.toString(), "aaa").toString());
		file.createNewFile();
		deliveryCtxt.setIncomingBlob(new VolumeBlob(file, (short) 0));
		return deliveryCtxt;
	}

	private static String getRawTest() {
		return "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
				+ "Subject: Test\n" + "\n" + "Hello World";
	}

	/*
	 * fileinto :copy foo; if header :contains "Subject" "test" { fileinto bar;
	 * }
	 *
	 * if message has "Subject: real" ==> it should be stored in "foo" and INBOX
	 */

	@Test
	void testCopyFileIntoPattern1Real() throws Exception {
		String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
				+ "fileinto :copy \"foo\";\n" + "if header :contains \"Subject\" \"Test\" {\n"
				+ "fileinto \"bar\"; }";

		final Account account = createAccount().create();
		RuleManager.clearCachedRules(account);
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
		account.setMailSieveScript(filterScriptPattern1);
		String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
				+ "Subject: Real\n" + "\n" + "Hello World";
		RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
				new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
				fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
		// message should be stored in foo
		Integer item = Objects.requireNonNull(mbox
				.getItemIds(null,
						mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
				.getIds(Type.MESSAGE)).get(0);
		Message msg = mbox.getMessageById(null, item);
		assertEquals("Hello World", msg.getFragment());
		// message should be stored in inbox
		item = Objects
				.requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
				.get(0);
		msg = mbox.getMessageById(null, item);
		assertEquals("Hello World", msg.getFragment());
	}

	/*
	 * fileinto :copy foo; if header :contains "Subject" "Test" { fileinto :copy
	 * bar; }
	 *
	 * if message has "Subject: test" ==> it should be stored in "foo", "bar"
	 * and INBOX
	 */

	@Test
	void testCopyFileIntoPattern2Test() {
		try {
			String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
					+ "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
					+ "fileinto :copy \"bar\"; }";

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScriptPattern1);
			String rawReal = getRawTest();
			RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
					new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			// message should be stored in bar
			Integer item = Objects.requireNonNull(mbox
					.getItemIds(null,
							mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "bar").getId())
					.getIds(Type.MESSAGE)).get(0);
			Message msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
			// message should be stored in foo
			item = Objects.requireNonNull(mbox
					.getItemIds(null,
							mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
					.getIds(Type.MESSAGE)).get(0);
			msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
			// message should be stored in inbox
			item = Objects
					.requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
					.get(0);
			msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	/*
	 * fileinto :copy foo; if header :contains "Subject" "Test" { fileinto :copy
	 * bar; }
	 *
	 * if message has "Subject: Real" ==> it should be stored in "foo" and INBOX
	 */

	@Test
	void testCopyFileIntoPattern2Real() {
		try {
			String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
					+ "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
					+ "fileinto :copy \"bar\"; }";

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScriptPattern1);
			String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
					+ "Subject: Real\n" + "\n" + "Hello World";
			RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
					new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			// message should be stored in foo
			Integer item = Objects.requireNonNull(mbox
					.getItemIds(null,
							mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
					.getIds(Type.MESSAGE)).get(0);
			Message msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
			// message should be stored in inbox
			item = Objects
					.requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
					.get(0);
			msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	/*
	 * fileinto :copy foo; if header :contains "Subject" "Test" { discard; }
	 *
	 * if message has "Subject: Test" ==> it should be stored in "foo"
	 */

	@Test
	void testCopyFileIntoPattern3Test() {
		try {
			String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
					+ "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
					+ "discard; }";

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScriptPattern1);
			String rawReal = getRawTest();
			RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
					new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			// message should be stored in foo
			Integer item = Objects.requireNonNull(mbox
					.getItemIds(null,
							mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
					.getIds(Type.MESSAGE)).get(0);
			Message msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
			// message should not be stored in inbox
			assertNull(
					mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE));
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	/*
	 * fileinto :copy foo; if header :contains "Subject" "Test" { discard; }
	 *
	 * if message has "Subject: real" ==> it should be stored in "foo" and INBOX
	 */

	@Test
	void testCopyFileIntoPattern3Real() {
		try {
			String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
					+ "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
					+ "discard; }";

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScriptPattern1);
			String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
					+ "Subject: Real\n" + "\n" + "Hello World";
			RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
					new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			// message should be stored in foo
			Integer item = Objects.requireNonNull(mbox
					.getItemIds(null,
							mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
					.getIds(Type.MESSAGE)).get(0);
			Message msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
			// message should be stored in inbox
			item = Objects
					.requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
					.get(0);
			msg = mbox.getMessageById(null, item);
			assertEquals("Hello World", msg.getFragment());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	private Account createRandomAccount() throws ServiceException {

		return createAccount().create();
	}

	/*
	 * Only one copy of message should be delivered in INBOX
	 */
	@Test
	void testKeepAndFileInto() throws ServiceException {
		final Account randomAccount = createRandomAccount();
		doKeepAndFileInto(randomAccount, "require \"fileinto\"; keep; fileinto \"Inbox\";");
		doKeepAndFileInto(randomAccount, "require \"fileinto\"; keep; fileinto \"/Inbox\";");
		doKeepAndFileInto(randomAccount, "require \"fileinto\"; keep; fileinto \"Inbox/\";");
		doKeepAndFileInto(randomAccount, "require \"fileinto\"; keep; fileinto \"/Inbox/\";");
		doKeepAndFileInto(randomAccount, "require \"fileinto\"; keep; fileinto \"inbox\";");
		doKeepAndFileInto(randomAccount, "require \"fileinto\"; fileinto \"Inbox\"; keep;");
		doKeepAndFileInto(randomAccount,
				"require [\"fileinto\", \"copy\"]; fileinto :copy \"Inbox\"; keep;");

		doKeepAndFileIntoOutgoing(randomAccount, "require \"fileinto\"; keep; fileinto \"Sent\";");
		doKeepAndFileIntoOutgoing(randomAccount, "require \"fileinto\"; keep; fileinto \"/Sent\";");
		doKeepAndFileIntoOutgoing(randomAccount, "require \"fileinto\"; keep; fileinto \"Sent/\";");
		doKeepAndFileIntoOutgoing(randomAccount, "require \"fileinto\"; keep; fileinto \"/Sent/\";");
		doKeepAndFileIntoOutgoing(randomAccount, "require \"fileinto\"; keep; fileinto \"sent\";");
		doKeepAndFileIntoOutgoing(randomAccount, "require \"fileinto\"; fileinto \"Sent\"; keep;");
		doKeepAndFileIntoOutgoing(randomAccount,
				"require [\"fileinto\", \"copy\"]; fileinto :copy \"Sent\"; keep;");
	}

	private void doKeepAndFileInto(Account account, String filterScript) {
		doKeepAndFileIntoIncoming(account, filterScript);
		doKeepAndFileIntoExisting(account, filterScript);
	}

	private void doKeepAndFileIntoIncoming(Account account, String filterScript) {
		String body = "doKeepAndFileIntoIncoming" + filterScript.hashCode();
		String sampleMsg = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
				+ "\n" + body;
		try {
			// Incoming
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScript);

			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(sampleMsg.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());

			final Message foundMessage = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals(Mailbox.ID_FOLDER_INBOX, foundMessage.getFolderId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	private void doKeepAndFileIntoExisting(Account account, String filterScript) {
		String body = "doKeepAndFileIntoExisting" + filterScript.hashCode();
		String sampleMsg = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
				+ "\n" + body;
		try {
			// Existing
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
					.getIds(MailItem.Type.MESSAGE);
			OperationContext octx = new OperationContext(mbox);
			Message msg = mbox.addMessage(octx,
					new ParsedMessage(sampleMsg.getBytes(), false),
					new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX)
							.setFlags(Flag.BITMASK_PRIORITY),
					fakeDeliveryContext());
			boolean filtered = RuleManager.applyRulesToExistingMessage(new OperationContext(mbox), mbox,
					msg.getId(),
					RuleManager.parse(filterScript));
			assertFalse(filtered);
			final Message foundMessage = mbox.getMessageById(null, msg.getId());
			assertEquals(Mailbox.ID_FOLDER_INBOX, foundMessage.getFolderId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	private void doKeepAndFileIntoOutgoing(Account account, String filterScript) {
		String body = "doKeepAndFileIntoIncoming" + filterScript.hashCode();
		String sampleMsg = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
				+ "\n" + body;
		try {
			// Outgoing
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailOutgoingSieveScript(filterScript);

			RuleManager.applyRulesToOutgoingMessage(
					new OperationContext(mbox), mbox,
					new ParsedMessage(sampleMsg.getBytes(), false),
					5, /* sent folder */
					true, 0, null, Mailbox.ID_AUTO_INCREMENT);
			List<Integer> searchedIds = TestUtil.search(mbox, "in:sent " + body, MailItem.Type.MESSAGE);
			assertEquals(1, searchedIds.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	@Test
	void testNoCapability1() {
		// No "fileinto" declaration
		String filterPlainFileintoScript =
				"if header :contains \"Subject\" \"test\" { fileinto \"Junk\"; }";
		try {

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterPlainFileintoScript);
			String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
					+ "\n" + "Hello World.";
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());
			Message msg = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	@Test
	void testNoCapability2() {
		// declare "fileinto" without "copy"
		String filterPlainFileintoScript = "require \"fileinto\";\n"
				+ "if header :contains \"Subject\" \"test\" {\n"
				+ "  fileinto :copy \"copyAndJunk\";\n"
				+ "}";
		try {

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterPlainFileintoScript);
			String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
					+ "\n" + "Hello World.";

			// Capability string is mandatory ==> :copy extension will be failed
			account.setSieveRequireControlEnabled(true);
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(1, ids.size());
			Message msg = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());

			// Capability string is optional ==> :copy extension should be available
			account.setSieveRequireControlEnabled(false);
			ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(2, ids.size());
			msg = mbox.getMessageById(null, ids.get(0).getId());
			assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
			msg = mbox.getMessageById(null, ids.get(1).getId());
			Folder folder = mbox.getFolderById(null, msg.getFolderId());
			assertEquals("copyAndJunk", folder.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}

	@Test
	void testPlainFileIntoWithSpaces() {
		String filterScript = "require [\"fileinto\"];\n"
				+ "fileinto \" abc\";"   // final destination folder = " abc"
				+ "fileinto \"abc \";"   // final destination folder = "abc"
				+ "fileinto \" abc \";"; // final destination folder = " abc"
		try {

			final Account account = createAccount().create();
			RuleManager.clearCachedRules(account);
			Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
			account.setMailSieveScript(filterScript);
			String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
					+ "\n" + "Hello World.";
			List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
					mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
					fakeDeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
			assertEquals(2, ids.size());
			Message msg = mbox.getMessageById(null, ids.get(0).getId());
			Folder folder = mbox.getFolderById(null, msg.getFolderId());
			assertEquals(" abc", folder.getName());
			msg = mbox.getMessageById(null, ids.get(1).getId());
			folder = mbox.getFolderById(null, msg.getFolderId());
			assertEquals("abc", folder.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}
}
