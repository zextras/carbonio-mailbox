// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.util.ItemData;

public class ArchiveFormatterTest {
    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void tagDecode() throws Exception {
        Account acct = Provisioning.getInstance().get(AccountBy.name, "test@zimbra.com");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

        ItemData id = new ItemData(mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX));

        id.tags = null;
        String[] tags = ArchiveFormatter.getTagNames(id);
        Assert.assertNotNull(tags);
        Assert.assertEquals("null -> no tags", 0, tags.length);

        tags = new String[] { "foo" };
        id.tags = ItemData.getTagString(tags);
        Assert.assertEquals("single tag encoding", "foo", id.tags);
        Assert.assertArrayEquals("single tag", tags, ArchiveFormatter.getTagNames(id));

        tags = new String[] { "fo:o" };
        id.tags = ItemData.getTagString(tags);
        Assert.assertEquals("single tag encoding w/colon", "fo\\:o", id.tags);
        Assert.assertArrayEquals("single tag w/colon", tags, ArchiveFormatter.getTagNames(id));

        tags = new String[] { "foo", "bar" };
        id.tags = ItemData.getTagString(tags);
        Assert.assertEquals("two tag encoding", "foo:bar", id.tags);
        Assert.assertArrayEquals("two tags", tags, ArchiveFormatter.getTagNames(id));

        tags = new String[] { "fo:o", "ba\\r" };
        id.tags = ItemData.getTagString(tags);
        Assert.assertEquals("two tag encoding w/colon, backslash", "fo\\:o:ba\\\\r", id.tags);
        Assert.assertArrayEquals("two tags w/colon, backslash", tags, ArchiveFormatter.getTagNames(id));

        tags = new String[] { "1-Tag", "2-Tag" };
        id.tags = ItemData.getTagString(tags);
        Assert.assertEquals("Tags starting with numerics", "1-Tag:2-Tag", id.tags);
        Assert.assertArrayEquals("Tags starting with numerics", tags, ArchiveFormatter.getTagNames(id));
    }
}
