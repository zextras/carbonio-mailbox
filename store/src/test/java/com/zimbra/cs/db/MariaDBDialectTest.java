/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import static com.zimbra.cs.db.MariaDBTestDatabase.GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Joiner;
import com.zextras.mailbox.testcontainers.MariaDBExtension;
import com.zimbra.cs.mailbox.MailItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MariaDBDialectTest {

  @RegisterExtension static final MariaDBExtension mariadb = new MariaDBExtension();

  private static final String MAIL_ITEM = GROUP + ".mail_item";
  private static final String TAGGED_ITEM = GROUP + ".tagged_item";
  private static final byte CONVERSATION = MailItem.Type.CONVERSATION.toByte();
  private static final byte MESSAGE = MailItem.Type.MESSAGE.toByte();
  private static final int INBOX = 2;
  private static final int TRASH = 3;
  private static final int CONV = 10;
  private static final int TAG = 64;

  private Connection conn;

  @BeforeAll
  static void createSchema() throws Exception {
    MariaDBTestDatabase.createSchema(mariadb);
  }

  @BeforeEach
  void emptyTables() throws SQLException {
    conn = MariaDBTestDatabase.connect(mariadb);
    MariaDBTestDatabase.emptyTables(conn, List.of(TAGGED_ITEM, MAIL_ITEM));
  }

  @AfterEach
  void close() throws SQLException {
    conn.close();
  }

  @Test
  void deletingFolderShrinksItsConversations() throws SQLException {
    conversationWithThreeMessages(1);

    decrementConversationSizes("mailbox_id = ? AND folder_id = ?", 1, INBOX, 7, 1000, 1);

    assertEquals(List.of("1|1|null|7|1000"), conversations());
  }

  @Test
  void deletingItemsShrinksTheirConversations() throws SQLException {
    conversationWithThreeMessages(1);

    decrementConversationSizes(
        "mailbox_id = ? AND " + DbUtil.whereIn("id", 2), 1, CONV + 1, CONV + 3, 7, 1000, 1);

    assertEquals(List.of("1|1|null|7|1000"), conversations());
  }

  @Test
  void deletingFolderLeavesOtherMailboxesUntouched() throws SQLException {
    conversationWithThreeMessages(1);
    conversationWithThreeMessages(2);

    decrementConversationSizes("mailbox_id = ? AND folder_id = ?", 1, INBOX, 7, 1000, 1);

    assertEquals(List.of("1|1|null|7|1000", "2|3|meta|1|null"), conversations());
  }

  @Test
  void renamingTagUpdatesOnlyTaggedItems() throws SQLException {
    message(1, 11, INBOX, "\0old\0", "s");
    message(1, 12, INBOX, "\0old\0", "s");
    tag(1, 11);

    execute(
        tagUpdate("tag_names = REPLACE(tag_names, ?, ?), mod_metadata = ?, change_date = ?"),
        "\0old\0", "\0new\0", 7, 1000, 1, TAG);

    assertEquals(List.of("11|\0new\0|7", "12|\0old\0|1"), tagNames());
  }

  @Test
  void deletingLastTagClearsTagNames() throws SQLException {
    message(1, 11, INBOX, "\0old\0", "s");
    message(1, 12, INBOX, "\0old\0other\0", "s");
    tag(1, 11);
    tag(1, 12);

    execute(
        tagUpdate(
            "mi.tag_names = CASE mi.tag_names WHEN ? THEN NULL ELSE REPLACE(mi.tag_names, ?, '\\0') END,"
                + " mod_metadata = ?, change_date = ?"),
        "\0old\0", "\0old\0", 7, 1000, 1, TAG);

    assertEquals(List.of("11|null|7", "12|\0other\0|7"), tagNames());
  }

  @Test
  void bitOrAddsFlagAndKeepsOthers() throws SQLException {
    message(1, 11, INBOX, null, "s");
    execute("UPDATE " + MAIL_ITEM + " SET flags = 1");

    execute("UPDATE " + MAIL_ITEM + " SET flags = " + "flags" + " | " + "4");

    assertEquals(List.of("5"), MariaDBTestDatabase.rows(conn, "SELECT flags FROM " + MAIL_ITEM));
  }

  @Test
  void foreignKeyChecksCanBeTurnedOffAndOn() throws SQLException {
    execute("SET FOREIGN_KEY_CHECKS=" + 1);
    assertThrows(SQLException.class, () -> tag(1, 99));

    execute("SET FOREIGN_KEY_CHECKS=" + 0);
    tag(1, 99);
  }

  @Test
  void subjectSortValueIsSubjectWithPaddedId() throws SQLException {
    message(1, 102, INBOX, null, "subject");
    message(1, 99, INBOX, null, "Subject");

    String[] fieldsToConcat = new String[]{"mi.subject",
        "LPAD(" + "mi.id" + ", " + 10 + ", '" + "0" + "')"};
    Joiner joiner = Joiner.on(", ").skipNulls();
    String sortField = "CONCAT(" + joiner.join(fieldsToConcat) + ")";

    assertEquals(
        List.of("Subject0000000099", "subject0000000102"),
        MariaDBTestDatabase.rows(
            conn, "SELECT " + sortField + " AS s FROM " + MAIL_ITEM + " AS mi ORDER BY s"));
  }

  private void conversationWithThreeMessages(int mailboxId) throws SQLException {
    execute(
        "INSERT INTO " + MAIL_ITEM
            + " (mailbox_id, id, type, date, size, metadata, mod_metadata, mod_content)"
            + " VALUES (?, ?, ?, 0, 3, 'meta', 1, 1)",
        mailboxId, CONV, CONVERSATION);
    messageInConversation(mailboxId, CONV + 1, INBOX);
    messageInConversation(mailboxId, CONV + 2, INBOX);
    messageInConversation(mailboxId, CONV + 3, TRASH);
  }

  private void messageInConversation(int mailboxId, int id, int folderId) throws SQLException {
    execute(
        "INSERT INTO " + MAIL_ITEM
            + " (mailbox_id, id, type, parent_id, folder_id, date, size, mod_metadata, mod_content)"
            + " VALUES (?, ?, ?, ?, ?, 0, 1, 1, 1)",
        mailboxId, id, MESSAGE, CONV, folderId);
  }

  private void message(int mailboxId, int id, int folderId, String tagNames, String subject)
      throws SQLException {
    execute(
        "INSERT INTO " + MAIL_ITEM
            + " (mailbox_id, id, type, folder_id, date, size, tag_names, subject, mod_metadata, mod_content)"
            + " VALUES (?, ?, ?, ?, 0, 1, ?, ?, 1, 1)",
        mailboxId, id, MESSAGE, folderId, tagNames, subject);
  }

  private void tag(int mailboxId, int itemId) throws SQLException {
    execute("INSERT INTO " + TAGGED_ITEM + " (mailbox_id, tag_id, item_id) VALUES (?, ?, ?)", mailboxId, TAG, itemId);
  }

  private void decrementConversationSizes(String sourceWhere, Object... values) throws SQLException {
    execute(DbUtil.decrementConversationSizes(MAIL_ITEM, sourceWhere, "mailbox_id = ? AND ", CONVERSATION), values);
  }

  private String tagUpdate(String set) {
    return DbUtil.updateInnerJoin(
        MAIL_ITEM + " AS mi",
        TAGGED_ITEM + " AS ti",
        "mi.mailbox_id = ti.mailbox_id AND mi.id = ti.item_id",
        set,
        "ti.mailbox_id = ? AND ti.tag_id = ?");
  }

  private List<String> conversations() throws SQLException {
    return MariaDBTestDatabase.rows(
        conn,
        "SELECT mailbox_id, size, metadata, mod_metadata, change_date FROM " + MAIL_ITEM
            + " WHERE type = ? ORDER BY mailbox_id",
        CONVERSATION);
  }

  private List<String> tagNames() throws SQLException {
    return MariaDBTestDatabase.rows(
        conn, "SELECT id, tag_names, mod_metadata FROM " + MAIL_ITEM + " ORDER BY id");
  }

  private void execute(String sql, Object... values) throws SQLException {
    MariaDBTestDatabase.execute(conn, sql, values);
  }
}
