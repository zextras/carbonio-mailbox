// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.util.ItemData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ArchiveFormatterTest extends MailboxTestSuite {

	private static Account acct;

	@BeforeAll
	public static void init() throws Exception {
		acct = createAccount().create();
	}

	@Test
	void tagDecode() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		ItemData id = new ItemData(mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX));

		id.tags = null;
		String[] tags = ArchiveFormatter.getTagNames(id);
		assertNotNull(tags);
		assertEquals(0, tags.length, "null -> no tags");

		tags = new String[]{"foo"};
		id.tags = ItemData.getTagString(tags);
		assertEquals("foo", id.tags, "single tag encoding");
		assertArrayEquals(tags, ArchiveFormatter.getTagNames(id), "single tag");

		tags = new String[]{"fo:o"};
		id.tags = ItemData.getTagString(tags);
		assertEquals("fo\\:o", id.tags, "single tag encoding w/colon");
		assertArrayEquals(tags, ArchiveFormatter.getTagNames(id), "single tag w/colon");

		tags = new String[]{"foo", "bar"};
		id.tags = ItemData.getTagString(tags);
		assertEquals("foo:bar", id.tags, "two tag encoding");
		assertArrayEquals(tags, ArchiveFormatter.getTagNames(id), "two tags");

		tags = new String[]{"fo:o", "ba\\r"};
		id.tags = ItemData.getTagString(tags);
		assertEquals("fo\\:o:ba\\\\r", id.tags, "two tag encoding w/colon, backslash");
		assertArrayEquals(tags, ArchiveFormatter.getTagNames(id), "two tags w/colon, backslash");

		tags = new String[]{"1-Tag", "2-Tag"};
		id.tags = ItemData.getTagString(tags);
		assertEquals("1-Tag:2-Tag", id.tags, "Tags starting with numerics");
		assertArrayEquals(tags, ArchiveFormatter.getTagNames(id), "Tags starting with numerics");
	}
}
