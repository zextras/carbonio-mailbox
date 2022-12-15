// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbResults;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.formatter.HeadersOnlyInputStream;

public class TestMailItem {

    @Rule
    public TestName testInfo = new TestName();
    private String USER_NAME = null;

    @Before
    public void setUp() throws Exception {
        USER_NAME = "testmailitem-" + testInfo.getMethodName() + "-user";
        tearDown();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.deleteAccountIfExists(USER_NAME);
    }

    @Test
    public void testListItemIds()
    throws Exception {
        Account account = TestUtil.createAccount(USER_NAME);
        // Mailbox.ID_FOLDER_INBOX;
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
        TestUtil.addMessage(mbox, testInfo.getMethodName() + "missive 1");
        TestUtil.addMessage(mbox, testInfo.getMethodName() + "missive 2");
        TestUtil.addMessage(mbox, testInfo.getMethodName() + "missive 3");
        TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_DRAFTS, testInfo.getMethodName() + "draft 1",
                System.currentTimeMillis());
        TestUtil.addMessage(mbox, Mailbox.ID_FOLDER_DRAFTS, testInfo.getMethodName() + "draft 2",
                System.currentTimeMillis());

        // Get item count per folder/type
        String sql = "SELECT folder_id, type, count(*) AS item_count " +
            "FROM " + DbMailItem.getMailItemTableName(mbox) +
            " WHERE mailbox_id = " + mbox.getId() +
            " GROUP BY folder_id, type";
        DbResults results = DbUtil.executeQuery(sql);
        Assert.assertTrue("No results returned", results.size() > 0);

        boolean foundInDrafts = false;
        boolean foundInInbox = false;
        // Confirm that listItemIds() returns the right count for each folder/type
        while (results.next()) {
            int folderId = results.getInt("folder_id");
            MailItem.Type type = MailItem.Type.of((byte) results.getInt("type"));
            // XXX bburtin: Work around incompatibility between JDBC driver version
            // 5.0.3 and MySQL 5.0.67, where the column name returned for an alias
            // is an empty string.
            // int count = results.getInt("item_count");
            int count = results.getInt(3);
            ZimbraLog.test.debug("Confirming that folder '%s' has %s items of type %s", folderId, count, type);
            Folder folder = mbox.getFolderById(null, folderId);
            Assert.assertNotNull(String.format("Folder with ID='%s' not found", folderId), folder);

            List<Integer> ids = mbox.listItemIds(null, type, folderId);
            Assert.assertEquals(String.format("Item count does not match for Folder with ID='%s'", folderId),
                    count, ids.size());
            if (Mailbox.ID_FOLDER_INBOX == folderId) {
                foundInInbox = true;
                Assert.assertEquals("Item count does not match for INBOX", 3, count);
            }
            if (Mailbox.ID_FOLDER_DRAFTS == folderId) {
                foundInDrafts = true;
                Assert.assertEquals("Item count does not match for DRAFTS", 2, count);
            }
        }
        Assert.assertTrue("No items reported for INBOX", foundInInbox);
        Assert.assertTrue("No items reported for DRAFTS", foundInDrafts);
    }

    @Test
    public void testHeadersOnlyInputStream()
    throws Exception {
        // Test no CRLFCRLF.
        String s = "test";
        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        HeadersOnlyInputStream headerStream = new HeadersOnlyInputStream(in);
        String read = new String(ByteUtil.getContent(headerStream, s.length()));
        Assert.assertEquals(s, read);

        // Test CRLFCRLF in the beginning.
        s = "\r\n\r\ntest";
        in = new ByteArrayInputStream(s.getBytes());
        headerStream = new HeadersOnlyInputStream(in);
        read = new String(ByteUtil.getContent(headerStream, s.length()));
        Assert.assertEquals(0, read.length());

        // Test CRLFCRLF in the middle.
        s = "te\r\n\r\nst";
        in = new ByteArrayInputStream(s.getBytes());
        headerStream = new HeadersOnlyInputStream(in);
        read = new String(ByteUtil.getContent(headerStream, s.length()));
        Assert.assertEquals("te", read);

        // Test CRLFCRLF in the end.
        s = "test\r\n\r\n";
        in = new ByteArrayInputStream(s.getBytes());
        headerStream = new HeadersOnlyInputStream(in);
        read = new String(ByteUtil.getContent(headerStream, s.length()));
        Assert.assertEquals("test", read);

        // Test CRLFCR without the last LF.
        s = "te\r\n\rst";
        in = new ByteArrayInputStream(s.getBytes());
        headerStream = new HeadersOnlyInputStream(in);
        read = new String(ByteUtil.getContent(headerStream, s.length()));
        Assert.assertEquals(s, read);
    }
}
