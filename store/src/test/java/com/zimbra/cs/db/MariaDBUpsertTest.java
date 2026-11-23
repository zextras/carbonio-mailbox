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
import com.zextras.mailbox.testcontainers.MariaDBResource;
import com.zimbra.common.util.StringUtil;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MariaDBUpsertTest {

  @RegisterExtension static final MariaDBExtension mariadb = new MariaDBExtension();

  private static final MariaDB DB = new MariaDB();
  private static final String GROUP = "mboxgroup1";
  private static final Timestamp T1 = Timestamp.valueOf("2026-11-01 10:00:00");
  private static final Timestamp T2 = Timestamp.valueOf("2026-11-02 10:00:00");
  private static final Timestamp T3 = Timestamp.valueOf("2026-11-03 10:00:00");
  private static final Timestamp T4 = Timestamp.valueOf("2026-11-04 10:00:00");

  private Connection conn;

  @BeforeAll
  static void createSchema() throws Exception {
    try (Connection c = connect()) {
      if (!schemaExists(c, GROUP)) {
        runScript(c, "db.sql", Map.of());
        runScript(c, "create_database.sql", Map.of("DATABASE_NAME", GROUP));
      }
    }
  }

  @BeforeEach
  void emptyTables() throws SQLException {
    conn = connect();
    try (Statement s = conn.createStatement()) {
      s.execute("SET foreign_key_checks = 0");
      for (String table :
          List.of(
              GROUP + ".open_conversation",
              GROUP + ".tagged_item",
              GROUP + ".appointment",
              "zimbra.mailbox_metadata",
              "zimbra.out_of_office",
              "zimbra.config")) {
        s.execute("DELETE FROM " + table);
      }
    }
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
    return execute(DB.upsert(table, columns, keys, updates), values);
  }

  private int execute(String sql, Object... values) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (int i = 0; i < values.length; i++) {
        stmt.setObject(i + 1, values[i]);
      }
      return stmt.executeUpdate();
    }
  }

  private List<String> rows(String sql) throws SQLException {
    List<String> rows = new ArrayList<>();
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      int columns = rs.getMetaData().getColumnCount();
      while (rs.next()) {
        List<String> values = new ArrayList<>();
        for (int i = 1; i <= columns; i++) {
          values.add(rs.getString(i));
        }
        rows.add(String.join("|", values));
      }
    }
    return rows;
  }

  private static Connection connect() throws SQLException {
    return DriverManager.getConnection(mariadb.jdbcUrl(), "root", MariaDBResource.ROOT_PASSWORD);
  }

  private static boolean schemaExists(Connection c, String schema) throws SQLException {
    try (PreparedStatement stmt =
        c.prepareStatement("SELECT 1 FROM information_schema.schemata WHERE schema_name = ?")) {
      stmt.setString(1, schema);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static void runScript(Connection c, String file, Map<String, String> vars) throws Exception {
    String script = StringUtil.fillTemplate(Files.readString(Path.of("db", file)), vars);
    try (Statement stmt = c.createStatement()) {
      for (String sql : DbUtil.parseScript(new StringReader(script), DB)) {
        if (!sql.isEmpty()) {
          stmt.execute(sql);
        }
      }
    }
  }
}
