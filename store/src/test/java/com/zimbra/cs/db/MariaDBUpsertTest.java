/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.testcontainers.MariaDBExtension;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MariaDBUpsertTest {

  @RegisterExtension static final MariaDBExtension mariadb = new MariaDBExtension();

  private static final String GROUP = MariaDBTestDatabase.GROUP;
  private static final Timestamp T1 = Timestamp.valueOf("2026-11-01 10:00:00");
  private static final Timestamp T2 = Timestamp.valueOf("2026-11-02 10:00:00");
  private static final Timestamp T3 = Timestamp.valueOf("2026-11-03 10:00:00");
  private static final Timestamp T4 = Timestamp.valueOf("2026-11-04 10:00:00");

  private Connection conn;

  @BeforeAll
  static void createSchema() throws Exception {
    MariaDBTestDatabase.createSchema(mariadb);
  }

  @BeforeEach
  void emptyTables() throws SQLException {
    conn = MariaDBTestDatabase.connect(mariadb);
    MariaDBTestDatabase.emptyTables(
        conn,
        List.of(
            GROUP + ".open_conversation",
            GROUP + ".tagged_item",
            GROUP + ".appointment",
            "zimbra.mailbox_metadata",
            "zimbra.out_of_office",
            "zimbra.config"));
  }

  @AfterEach
  void close() throws SQLException {
    conn.close();
  }

  @Test
  void openConversationInsertsNewHash() throws SQLException {
    openConversation(1, "h", 300);

    assertEquals(List.of("1|h|300"), rows("SELECT mailbox_id, hash, conv_id FROM " + GROUP + ".open_conversation"));
  }

  @Test
  void openConversationMovesHashToNewConversation() throws SQLException {
    openConversation(1, "h", 300);
    openConversation(1, "h", 301);

    assertEquals(List.of("1|h|301"), rows("SELECT mailbox_id, hash, conv_id FROM " + GROUP + ".open_conversation"));
  }

  @Test
  void taggingTwiceKeepsOneRow() throws SQLException {
    tagItem(1, 64, 300);
    tagItem(1, 64, 300);

    assertEquals(List.of("1|64|300"), rows("SELECT mailbox_id, tag_id, item_id FROM " + GROUP + ".tagged_item"));
  }

  @Test
  void saveMetadataReplacesOnlyTheGivenSection() throws SQLException {
    saveMetadata(1, "A", "a1");
    saveMetadata(1, "B", "b1");
    saveMetadata(1, "A", "a2");

    assertEquals(
        List.of("1|A|a2", "1|B|b1"),
        rows("SELECT mailbox_id, section, metadata FROM zimbra.mailbox_metadata ORDER BY section"));
  }

  @Test
  void setSentTimeUpdatesRecipient() throws SQLException {
    setSentTime(1, "x@example.com", T1);
    setSentTime(1, "x@example.com", T2);

    assertEquals(
        List.of("1|x@example.com|2026-11-02 10:00:00"),
        rows("SELECT mailbox_id, sent_to, CAST(sent_on AS CHAR) FROM zimbra.out_of_office"));
  }

  @Test
  void setSentTimeReportsARowWhenTimeIsUnchanged() throws SQLException {
    setSentTime(1, "x@example.com", T1);

    assertTrue(setSentTime(1, "x@example.com", T1) > 0);
  }

  @Test
  void indexingSameAppointmentAgainUpdatesTimes() throws SQLException {
    indexAppointment(1, "U1", 100, T1, T2);
    indexAppointment(1, "U1", 100, T3, T4);

    assertEquals(List.of("1|U1|100|2026-11-03 10:00:00|2026-11-04 10:00:00"), appointments());
  }

  @Test
  void indexingUidOnNewItemMovesIt() throws SQLException {
    indexAppointment(1, "U1", 100, T1, T2);
    indexAppointment(1, "U1", 200, T1, T2);

    assertEquals(List.of("1|U1|200|2026-11-01 10:00:00|2026-11-02 10:00:00"), appointments());
  }

  @Test
  void indexingNewUidOnSameItemReplacesIt() throws SQLException {
    indexAppointment(1, "U1", 100, T1, T2);
    indexAppointment(1, "U2", 100, T1, T2);

    assertEquals(List.of("1|U2|100|2026-11-01 10:00:00|2026-11-02 10:00:00"), appointments());
  }

  @Test
  void indexingAppointmentLeavesOtherMailboxesUntouched() throws SQLException {
    indexAppointment(1, "U1", 100, T1, T2);
    indexAppointment(2, "U1", 100, T3, T4);

    assertEquals(
        List.of(
            "1|U1|100|2026-11-01 10:00:00|2026-11-02 10:00:00",
            "2|U1|100|2026-11-03 10:00:00|2026-11-04 10:00:00"),
        appointments());
  }

  @Test
  void setConfigInsertsThenUpdatesValue() throws SQLException {
    setConfig("key", "v1", T1);
    setConfig("key", "v2", T2);

    assertEquals(
        List.of("key|v2|2026-11-02 10:00:00"),
        rows("SELECT name, value, CAST(modified AS CHAR) FROM zimbra.config"));
  }

  @Test
  void setConfigKeepsDescription() throws SQLException {
    execute("INSERT INTO zimbra.config (name, value, description, modified) VALUES ('key', 'v1', 'kept', ?)", T1);

    setConfig("key", "v2", T2);

    assertEquals(List.of("key|v2|kept"), rows("SELECT name, value, description FROM zimbra.config"));
  }

  @Test
  void appointmentMatchingTwoOtherAppointmentsIsRejected() throws SQLException {
    indexAppointment(1, "U2", 200, T1, T2);
    indexAppointment(1, "U1", 100, T1, T2);

    assertThrows(SQLException.class, () -> indexAppointment(1, "U2", 100, T3, T4));
  }

  private void openConversation(int mailboxId, String hash, int convId) throws SQLException {
    upsert(
        GROUP + ".open_conversation",
        List.of("mailbox_id", "hash", "conv_id"),
        List.of("mailbox_id", "hash"),
        List.of("conv_id"),
        mailboxId, hash, convId);
  }

  private void tagItem(int mailboxId, int tagId, int itemId) throws SQLException {
    upsert(
        GROUP + ".tagged_item",
        List.of("mailbox_id", "tag_id", "item_id"),
        List.of("mailbox_id", "tag_id", "item_id"),
        List.of(),
        mailboxId, tagId, itemId);
  }

  private void saveMetadata(int mailboxId, String section, String metadata) throws SQLException {
    upsert(
        "zimbra.mailbox_metadata",
        List.of("mailbox_id", "section", "metadata"),
        List.of("mailbox_id", "section"),
        List.of("metadata"),
        mailboxId, section, metadata);
  }

  private int setSentTime(int mailboxId, String sentTo, Timestamp sentOn) throws SQLException {
    return upsert(
        "zimbra.out_of_office",
        List.of("mailbox_id", "sent_to", "sent_on"),
        List.of("mailbox_id", "sent_to"),
        List.of("sent_on"),
        mailboxId, sentTo, sentOn);
  }

  private void indexAppointment(int mailboxId, String uid, int itemId, Timestamp start, Timestamp end)
      throws SQLException {
    upsert(
        GROUP + ".appointment",
        List.of("mailbox_id", "uid", "item_id", "start_time", "end_time"),
        List.of("mailbox_id", "uid"),
        List.of("uid", "item_id", "start_time", "end_time"),
        mailboxId, uid, itemId, start, end);
  }

  private void setConfig(String name, String value, Timestamp modified) throws SQLException {
    upsert(
        "zimbra.config",
        List.of("name", "value", "modified"),
        List.of("name"),
        List.of("value", "modified"),
        name, value, modified);
  }

  private List<String> appointments() throws SQLException {
    return rows(
        "SELECT mailbox_id, uid, item_id, CAST(start_time AS CHAR), CAST(end_time AS CHAR) FROM "
            + GROUP
            + ".appointment ORDER BY mailbox_id, uid");
  }

  private int upsert(
      String table, List<String> columns, List<String> keys, List<String> updates, Object... values)
      throws SQLException {
    return execute(DbUtil.upsert(table, columns, keys, updates), values);
  }

  private int execute(String sql, Object... values) throws SQLException {
    return MariaDBTestDatabase.execute(conn, sql, values);
  }

  private List<String> rows(String sql) throws SQLException {
    return MariaDBTestDatabase.rows(conn, sql);
  }
}
