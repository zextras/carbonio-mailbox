// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.index.DbSearchConstraints;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link DbSearch}.
 *
 * @author ysasaki
 */
public final class DbSearchTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    private DbConnection conn = null;
    private Mailbox mbox = null;

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
        conn = DbPool.getConnection(mbox);
    }

    @AfterEach
    public void tearDown() {
        conn.closeQuietly();
    }

 @Test
 void sortByAttachment() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 100, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_REPLIED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_ATTACHED | Flag.BITMASK_REPLIED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_REPLIED);

  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, new DbSearchConstraints.Leaf(),
    SortBy.ATTACHMENT_ASC, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(100, result.get(0).getId());
  assertEquals("00000000100", result.get(0).getSortValue());
  assertEquals(102, result.get(1).getId());
  assertEquals("00000000102", result.get(1).getSortValue());
  assertEquals(101, result.get(2).getId());
  assertEquals("10000000101", result.get(2).getSortValue());

  result = new DbSearch(mbox).search(conn, new DbSearchConstraints.Leaf(), SortBy.ATTACHMENT_DESC,
    0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(101, result.get(0).getId());
  assertEquals("10000000101", result.get(0).getSortValue());
  assertEquals(102, result.get(1).getId());
  assertEquals("00000000102", result.get(1).getSortValue());
  assertEquals(100, result.get(2).getId());
  assertEquals("00000000100", result.get(2).getSortValue());
 }

 @Test
 void sortByFlag() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 100, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_REPLIED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_FLAGGED | Flag.BITMASK_REPLIED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_REPLIED);

  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, new DbSearchConstraints.Leaf(), SortBy.FLAG_ASC,
    0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(100, result.get(0).getId());
  assertEquals("00000000100", result.get(0).getSortValue());
  assertEquals(102, result.get(1).getId());
  assertEquals("00000000102", result.get(1).getSortValue());
  assertEquals(101, result.get(2).getId());
  assertEquals("10000000101", result.get(2).getSortValue());

  result = new DbSearch(mbox).search(conn, new DbSearchConstraints.Leaf(), SortBy.FLAG_DESC,
    0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(101, result.get(0).getId());
  assertEquals("10000000101", result.get(0).getSortValue());
  assertEquals(102, result.get(1).getId());
  assertEquals("00000000102", result.get(1).getSortValue());
  assertEquals(100, result.get(2).getId());
  assertEquals("00000000100", result.get(2).getSortValue());
  ;
 }

 @Test
 void sortByPriority() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 100, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_LOW_PRIORITY | Flag.BITMASK_REPLIED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_HIGH_PRIORITY | Flag.BITMASK_REPLIED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0)", mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(),
    Flag.BITMASK_REPLIED);

  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, new DbSearchConstraints.Leaf(),
    SortBy.PRIORITY_ASC, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(100, result.get(0).getId());
  assertEquals("00000000100", result.get(0).getSortValue());
  assertEquals(102, result.get(1).getId());
  assertEquals("10000000102", result.get(1).getSortValue());
  assertEquals(101, result.get(2).getId());
  assertEquals("20000000101", result.get(2).getSortValue());

  result = new DbSearch(mbox).search(conn, new DbSearchConstraints.Leaf(), SortBy.PRIORITY_DESC,
    0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(101, result.get(0).getId());
  assertEquals("20000000101", result.get(0).getSortValue());
  assertEquals(102, result.get(1).getId());
  assertEquals("10000000102", result.get(1).getSortValue());
  assertEquals(100, result.get(2).getId());
  assertEquals("00000000100", result.get(2).getSortValue());
 }

 @Test
 void subjectCursor() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, subject) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 100, MailItem.Type.MESSAGE.toByte(), "subject");
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, subject) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(), "subject");
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, subject) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(), "subject");
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, subject) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 103, MailItem.Type.MESSAGE.toByte(), "subject");
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, subject) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 104, MailItem.Type.MESSAGE.toByte(), "subject");

  DbSearchConstraints.Leaf constraints = new DbSearchConstraints.Leaf();
  constraints.cursorRange = new DbSearchConstraints.CursorRange("SUBJECT0000000102", true, null, false,
    SortBy.SUBJ_ASC);
  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, constraints, SortBy.SUBJ_ASC, 0, 100,
    DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(102, result.get(0).getId());
  assertEquals("SUBJECT0000000102", result.get(0).getSortValue());
  assertEquals(103, result.get(1).getId());
  assertEquals("SUBJECT0000000103", result.get(1).getSortValue());
  assertEquals(104, result.get(2).getId());
  assertEquals("SUBJECT0000000104", result.get(2).getSortValue());
 }

 @Test
 void mdate() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(), 100, 1000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(), 200, 2000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 103, MailItem.Type.MESSAGE.toByte(), 300, 3000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 104, MailItem.Type.MESSAGE.toByte(), 400, 4000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 105, MailItem.Type.MESSAGE.toByte(), 500, 5000);

  DbSearchConstraints.Leaf constraints = new DbSearchConstraints.Leaf();
  constraints.addDateRange(200000, true, 400000, false, true);
  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, constraints, SortBy.DATE_ASC, 0, 100,
    DbSearch.FetchMode.ID);
  assertEquals(2, result.size());
  assertEquals(102, result.get(0).getId());
  assertEquals(103, result.get(1).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addMDateRange(2000000, true, 4000000, false, true);
  result = new DbSearch(mbox).search(conn, constraints, SortBy.DATE_ASC, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(2, result.size());
  assertEquals(102, result.get(0).getId());
  assertEquals(103, result.get(1).getId());
 }

 @Test
 void tag() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(), 100, 1000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(), 200, 2000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 103, MailItem.Type.MESSAGE.toByte(), 300, 3000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 104, MailItem.Type.MESSAGE.toByte(), 400, 4000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, change_date, size, tags, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, 0, ?, ?, 0, 0, 0, 0)", mbox.getId(), 105, MailItem.Type.MESSAGE.toByte(), 500, 5000);

  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.tagged_item (mailbox_id, item_id, tag_id) " +
    "VALUES(?, ?, ?)", mbox.getId(), 101, Flag.ID_UNREAD);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.tagged_item (mailbox_id, item_id, tag_id) " +
    "VALUES(?, ?, ?)", mbox.getId(), 102, Flag.ID_FLAGGED);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.tagged_item (mailbox_id, item_id, tag_id) " +
    "VALUES(?, ?, ?)", mbox.getId(), 103, Flag.ID_UNREAD);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.tagged_item (mailbox_id, item_id, tag_id) " +
    "VALUES(?, ?, ?)", mbox.getId(), 103, Flag.ID_FLAGGED);

  DbSearchConstraints.Leaf constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, constraints, SortBy.NONE, 0, 100,
    DbSearch.FetchMode.ID);
  assertEquals(2, result.size());
  assertEquals(101, result.get(0).getId());
  assertEquals(103, result.get(1).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), false);
  result = new DbSearch(mbox).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(102, result.get(0).getId());
  assertEquals(104, result.get(1).getId());
  assertEquals(105, result.get(2).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), false);
  result = new DbSearch(mbox).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(0, result.size());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  constraints.addTag(mbox.getTagByName(null, "\\Flagged"), true);
  result = new DbSearch(mbox).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(1, result.size());
  assertEquals(103, result.get(0).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  constraints.addTag(mbox.getTagByName(null, "\\Flagged"), false);
  result = new DbSearch(mbox).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(1, result.size());
  assertEquals(101, result.get(0).getId());
 }

 @Test
 void tagInDumpster() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item_dumpster " +
    "(mailbox_id, id, type, flags, unread, tag_names, date, size, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 0, 0)",
    mbox.getId(), 101, MailItem.Type.MESSAGE.toByte(), 0, 1, null, 100, 1000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item_dumpster " +
    "(mailbox_id, id, type, flags, unread, tag_names, date, size, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 0, 0)",
    mbox.getId(), 102, MailItem.Type.MESSAGE.toByte(), Flag.BITMASK_FLAGGED, 0, null, 200, 2000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item_dumpster " +
    "(mailbox_id, id, type, flags, unread, tag_names, date, size, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 0, 0)",
    mbox.getId(), 103, MailItem.Type.MESSAGE.toByte(), Flag.BITMASK_FLAGGED, 1, null, 300, 3000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item_dumpster " +
    "(mailbox_id, id, type, flags, unread, tag_names, date, size, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 0, 0)",
    mbox.getId(), 104, MailItem.Type.MESSAGE.toByte(), 0, 0, "\0test\0", 400, 4000);
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item_dumpster " +
    "(mailbox_id, id, type, flags, unread, tag_names, date, size, mod_metadata, mod_content) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 0, 0)",
    mbox.getId(), 105, MailItem.Type.MESSAGE.toByte(), 0, 0, "\0test\0", 500, 5000);
  mbox.createTag(null, "test", (byte) 0);

  DbSearchConstraints.Leaf constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  List<DbSearch.Result> result = new DbSearch(mbox, true).search(conn, constraints, SortBy.NONE, 0, 100,
    DbSearch.FetchMode.ID);
  assertEquals(2, result.size());
  assertEquals(101, result.get(0).getId());
  assertEquals(103, result.get(1).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), false);
  result = new DbSearch(mbox, true).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(102, result.get(0).getId());
  assertEquals(104, result.get(1).getId());
  assertEquals(105, result.get(2).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), false);
  result = new DbSearch(mbox, true).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(0, result.size());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  constraints.addTag(mbox.getTagByName(null, "\\Flagged"), true);
  result = new DbSearch(mbox, true).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(1, result.size());
  assertEquals(103, result.get(0).getId());

  constraints = new DbSearchConstraints.Leaf();
  constraints.addTag(mbox.getTagByName(null, "\\Unread"), true);
  constraints.addTag(mbox.getTagByName(null, "\\Flagged"), false);
  result = new DbSearch(mbox, true).search(conn, constraints, SortBy.NONE, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(1, result.size());
  assertEquals(101, result.get(0).getId());

  //TODO Can't test tag_names because HSQLDB's LIKE doesn't match NUL.
 }

 @Test
 void caseInsensitiveSort() throws Exception {
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, sender) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 101, MailItem.Type.CONTACT.toByte(), "Zimbra");
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, sender) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 102, MailItem.Type.CONTACT.toByte(), "AAA");
  DbUtil.executeUpdate(conn, "INSERT INTO mboxgroup1.mail_item " +
    "(mailbox_id, id, type, flags, date, size, tags, mod_metadata, mod_content, sender) " +
    "VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, ?)", mbox.getId(), 103, MailItem.Type.CONTACT.toByte(), "aaa");

  DbSearchConstraints.Leaf constraints = new DbSearchConstraints.Leaf();
  List<DbSearch.Result> result = new DbSearch(mbox).search(conn, constraints, SortBy.NAME_ASC, 0, 100,
    DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(102, result.get(0).getId());
  assertEquals(103, result.get(1).getId());
  assertEquals(101, result.get(2).getId());

  constraints = new DbSearchConstraints.Leaf();
  result = new DbSearch(mbox).search(conn, constraints, SortBy.NAME_DESC, 0, 100, DbSearch.FetchMode.ID);
  assertEquals(3, result.size());
  assertEquals(101, result.get(0).getId());
  assertEquals(103, result.get(1).getId());
  assertEquals(102, result.get(2).getId());
 }
}
