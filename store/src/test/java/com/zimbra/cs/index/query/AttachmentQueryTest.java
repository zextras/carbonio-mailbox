// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.index.LuceneQueryOperation;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link AttachmentQuery}.
 *
 * @author ysasaki
 */
public final class AttachmentQueryTest extends MailboxTestSuite {

	private static Account account;

	@BeforeAll
	public static void init() throws Exception {
		account = createAccount().create();
	}

	@BeforeEach
	public void setUp() throws Exception {
		MailboxTestUtil.clearData();
	}

	@Test
	void attachQueryToQueryString() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		Query query = AttachmentQuery.createQuery("any");
		assertEquals("(attachment:any)",
				((LuceneQueryOperation) query.compile(mbox, true)).toQueryString());
	}

	@Test
	void attachQueryNotAny() throws Exception {
		Query query = AttachmentQuery.createQuery("any");
		query.setModifier(Query.Modifier.MINUS);
		assertEquals("Q(attachment:none)", query.toString());
	}

	@Test
	void attachQueryUnknown() throws Exception {
		Query query = AttachmentQuery.createQuery("unknowncontenttype");
		assertEquals("Q(attachment:unknowncontenttype)", query.toString());
	}

	@Test
	void attachQueryMsWord() throws Exception {
		Query query = AttachmentQuery.createQuery("msword");
		assertEquals(
				"(Q(attachment:application/msword)" +
						" || Q(attachment:application/vnd.openxmlformats-officedocument.wordprocessingml.document)"
						+
						" || Q(attachment:application/vnd.openxmlformats-officedocument.wordprocessingml.template)"
						+
						" || Q(attachment:application/vnd.ms-word.document.macroenabled.12)" +
						" || Q(attachment:application/vnd.ms-word.template.macroenabled.12))",
				query.toString());
	}

	@Test
	void typeQueryMsWord() throws Exception {
		Query query = TypeQuery.createQuery("msword");
		assertEquals(
				"(Q(type:application/msword)" +
						" || Q(type:application/vnd.openxmlformats-officedocument.wordprocessingml.document)" +
						" || Q(type:application/vnd.openxmlformats-officedocument.wordprocessingml.template)" +
						" || Q(type:application/vnd.ms-word.document.macroenabled.12)" +
						" || Q(type:application/vnd.ms-word.template.macroenabled.12))",
				query.toString());
	}

	@Test
	void typeQueryToQueryString() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		Query query = TypeQuery.createQuery("any");
		assertEquals("(type:any)", ((LuceneQueryOperation) query.compile(mbox, true)).toQueryString());
	}

}
