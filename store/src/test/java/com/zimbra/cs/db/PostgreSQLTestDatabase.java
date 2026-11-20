// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.zimbra.common.localconfig.LC;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL in a container, loaded with the production schema from {@code store/db/postgresql}.
 * One container per JVM, shared by every test like the in-memory HSQLDB.
 */
final class PostgreSQLTestDatabase {

  private static final Path SCHEMA_DIR = Path.of("db", "postgresql");
  private static final String GROUP = DbMailbox.getDatabaseName(1);

  private static PostgreSQLContainer container;

  private PostgreSQLTestDatabase() {}

  static synchronized void start() {
    if (container == null) {
      container = new PostgreSQLContainer("postgres:17-alpine");
      container.start();
    }
    LC.postgres_host.setDefault(container.getHost());
    LC.postgres_port.setDefault(container.getFirstMappedPort());
    LC.postgres_database.setDefault(container.getDatabaseName());
    LC.postgres_user.setDefault(container.getUsername());
    LC.postgres_password.setDefault(container.getPassword());
  }

  static void createDatabase(String volumeDirectory) throws Exception {
    start();
    try (Connection conn = connect()) {
      try (Statement stmt = conn.createStatement();
          ResultSet rs =
              stmt.executeQuery(
                  "SELECT 1 FROM information_schema.schemata WHERE schema_name = 'zimbra'")) {
        if (rs.next()) {
          return;
        }
      }
      run(conn, read("db.sql"));
      run(conn, read("versions-init.sql"));
      run(conn, read("create_database.sql").replace("${DATABASE_NAME}", GROUP));
      try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate(
            "UPDATE zimbra.volume SET path = '" + volumeDirectory + "/store' WHERE id = 1");
        stmt.executeUpdate(
            "UPDATE zimbra.volume SET path = '" + volumeDirectory + "/index' WHERE id = 2");
      }
      conn.commit();
    }
  }

  /** Same scope as the HSQLDB {@code clear.sql}: every row except the volumes. */
  static void clearDatabase() throws Exception {
    try (Connection conn = connect();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          "TRUNCATE "
              + String.join(
                  ", ",
                  GROUP + ".tagged_item",
                  GROUP + ".tag",
                  GROUP + ".mail_item",
                  GROUP + ".mail_item_dumpster",
                  GROUP + ".revision",
                  GROUP + ".revision_dumpster",
                  GROUP + ".open_conversation",
                  GROUP + ".appointment",
                  GROUP + ".appointment_dumpster",
                  GROUP + ".tombstone",
                  GROUP + ".pop3_message",
                  GROUP + ".imap_folder",
                  GROUP + ".imap_message",
                  GROUP + ".data_source_item",
                  "zimbra.mailbox",
                  "zimbra.current_volumes",
                  "zimbra.deleted_account",
                  "zimbra.mailbox_metadata",
                  "zimbra.out_of_office",
                  "zimbra.config",
                  "zimbra.table_maintenance",
                  "zimbra.service_status",
                  "zimbra.scheduled_task",
                  "zimbra.pending_acl_push")
              + " CASCADE");
      stmt.execute(
          "INSERT INTO zimbra.current_volumes (message_volume_id, index_volume_id,"
              + " next_mailbox_id) VALUES (1, 2, 1)");
      conn.commit();
    }
  }

  private static Connection connect() throws SQLException {
    Connection conn =
        DriverManager.getConnection(
            container.getJdbcUrl(), container.getUsername(), container.getPassword());
    conn.setAutoCommit(false);
    return conn;
  }

  private static String read(String file) throws Exception {
    return Files.readString(SCHEMA_DIR.resolve(file), StandardCharsets.UTF_8);
  }

  private static void run(Connection conn, String script) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(script);
    }
  }
}
